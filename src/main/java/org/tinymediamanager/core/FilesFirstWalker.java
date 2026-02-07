/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.core;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class {@link FilesFirstWalker} provides a custom file system traversal mechanism that processes files before recursing into subdirectories.
 * <p>
 * Unlike {@link Files}, this walker visits all files in a directory before descending into subdirectories. This "files-first" approach is useful for
 * processing media libraries where files should be handled before their containing directories are fully explored.
 * </p>
 * <p>
 * The walker respects the {@link FileVisitor} contract, allowing callers to control traversal through return values:
 * <ul>
 * <li>{@link FileVisitResult#CONTINUE} - continue traversal normally</li>
 * <li>{@link FileVisitResult#SKIP_SUBTREE} - skip remaining entries in current directory</li>
 * <li>{@link FileVisitResult#TERMINATE} - stop all traversal immediately</li>
 * </ul>
 * </p>
 * <p>
 * Symbolic links are handled as files and not followed unless configured during construction. File access errors are reported to the visitor but do
 * not stop traversal.
 * </p>
 *
 * @author Manuel Laggner
 * @since 1.0
 */
public class FilesFirstWalker {

  /** Link options for file attribute reading (follow or don't follow symbolic links) */
  private final LinkOption[] linkOptions;

  /**
   * Constructs a new {@link FilesFirstWalker} with the specified symbolic link handling policy.
   *
   * @param followLinks
   *          {@code true} to follow symbolic links during traversal, {@code false} to treat them as regular files
   */
  public FilesFirstWalker(boolean followLinks) {
    this.linkOptions = followLinks ? new LinkOption[0] : new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
  }

  /**
   * Walks the file tree rooted at the given starting path, invoking the provided visitor for each file and directory encountered.
   * <p>
   * If the starting path is a regular file, the visitor is invoked for that file and the walk completes. If the starting path is a directory, it is
   * recursively walked with all files processed before subdirectories are explored.
   * </p>
   *
   * @param start
   *          the starting path for the walk (file or directory)
   * @param visitor
   *          the visitor to invoke for each file and directory encountered
   * @throws IOException
   *           if an I/O error occurs while reading file attributes or enumerating directory contents
   */
  public void walk(Path start, FileVisitor<? super Path> visitor) throws IOException {
    // Attempt to read attributes of the starting path
    BasicFileAttributes attrs;
    try {
      attrs = Files.readAttributes(start, BasicFileAttributes.class, linkOptions);
    }
    catch (IOException e) {
      // If the starting path is unreadable, notify the visitor and exit gracefully
      visitor.visitFileFailed(start, e);
      return;
    }

    // If the starting path is a regular file, process it directly and return
    if (!attrs.isDirectory()) {
      visitor.visitFile(start, attrs);
      return;
    }

    // Otherwise, start the directory walk
    walkDir(start, visitor);
  }

  /**
   * Recursively walks a directory, processing all files before descending into subdirectories.
   * <p>
   * This method implements the core traversal logic:
   * <ol>
   * <li>Invokes {@code preVisitDirectory} on the visitor for the current directory</li>
   * <li>Reads all entries in the directory</li>
   * <li>Processes files, symbolic links, and "other" file types immediately</li>
   * <li>Collects subdirectories for later processing</li>
   * <li>Recursively walks all collected subdirectories</li>
   * <li>Invokes {@code postVisitDirectory} on the visitor after processing is complete</li>
   * </ol>
   * </p>
   * <p>
   * The visitor controls traversal behavior via its return values. If any method returns {@link FileVisitResult#TERMINATE}, a
   * {@link WalkTerminatedException} is thrown to abort the entire walk.
   * </p>
   *
   * @param dir
   *          the directory path to walk
   * @param visitor
   *          the visitor to invoke for each entry
   * @throws IOException
   *           if an I/O error occurs while reading attributes or directory contents
   * @throws WalkTerminatedException
   *           if the visitor returns {@link FileVisitResult#TERMINATE} at any point
   */
  private void walkDir(Path dir, FileVisitor<? super Path> visitor) throws IOException {
    // Read attributes of the directory itself
    BasicFileAttributes dirAttrs;
    try {
      dirAttrs = Files.readAttributes(dir, BasicFileAttributes.class, linkOptions);
    }
    catch (IOException e) {
      // If directory attributes are unreadable, notify the visitor and return
      visitor.visitFileFailed(dir, e);
      return;
    }

    // Allow the visitor to perform pre-processing and potentially skip this directory tree
    FileVisitResult pre = visitor.preVisitDirectory(dir, dirAttrs);
    if (pre == FileVisitResult.SKIP_SUBTREE) {
      return;
    }
    if (pre == FileVisitResult.TERMINATE) {
      throw new WalkTerminatedException();
    }

    // Collect subdirectories for later processing (after all files in this directory are handled)
    List<Path> subdirs = new ArrayList<>();

    // Process all entries in the current directory
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
      for (Path entry : ds) {
        // Read attributes of each entry
        BasicFileAttributes a;
        try {
          a = Files.readAttributes(entry, BasicFileAttributes.class, linkOptions);
        }
        catch (IOException e) {
          // Notify the visitor of failed file attribute read and continue with next entry
          FileVisitResult vr = visitor.visitFileFailed(entry, e);
          if (vr == FileVisitResult.TERMINATE) {
            throw new WalkTerminatedException();
          }
          // Continue to next entry on other errors
          continue;
        }

        // Handle regular files and "other" file types (e.g., special files)
        if (a.isRegularFile() || a.isOther()) {
          FileVisitResult vr = visitor.visitFile(entry, a);
          if (vr == FileVisitResult.TERMINATE) {
            throw new WalkTerminatedException();
          }
          else if (vr == FileVisitResult.SKIP_SUBTREE) {
            return;
          }
        }
        // Collect directories for later processing (this ensures files-first processing)
        else if (a.isDirectory()) {
          subdirs.add(entry);
        }
        // Handle symbolic links as files (by default, do not follow them)
        else if (a.isSymbolicLink()) {
          // Treat symlink as a file and notify visitor; do not follow it
          FileVisitResult vr = visitor.visitFile(entry, a);
          if (vr == FileVisitResult.TERMINATE) {
            throw new WalkTerminatedException();
          }
          else if (vr == FileVisitResult.SKIP_SUBTREE) {
            return;
          }
        }
      }
    }
    catch (IOException e) {
      // Directory enumeration failed; notify visitor and return
      FileVisitResult vr = visitor.postVisitDirectory(dir, e);
      if (vr == FileVisitResult.TERMINATE)
        throw new WalkTerminatedException();
      return;
    }

    // Now descend into subdirectories after all files in this directory have been processed
    for (Path sub : subdirs) {
      try {
        walkDir(sub, visitor);
      }
      catch (WalkTerminatedException t) {
        // Propagate termination exception to stop all traversal
        throw t;
      }
      catch (IOException e) {
        // If a subdirectory fails to process, notify visitor and continue with next subdirectory
        FileVisitResult vr = visitor.postVisitDirectory(sub, e);
        if (vr == FileVisitResult.TERMINATE) {
          throw new WalkTerminatedException();
        }
      }
    }

    // Notify visitor after processing this directory and all its subdirectories
    FileVisitResult vr = visitor.postVisitDirectory(dir, null);
    if (vr == FileVisitResult.TERMINATE) {
      throw new WalkTerminatedException();
    }
  }

  /**
   * A private exception class used internally to signal termination of the directory walk.
   * <p>
   * This exception is thrown when the visitor returns {@link FileVisitResult#TERMINATE}, indicating that the entire file tree traversal should be
   * stopped immediately.
   * </p>
   */
  private static class WalkTerminatedException extends RuntimeException {
  }
}
