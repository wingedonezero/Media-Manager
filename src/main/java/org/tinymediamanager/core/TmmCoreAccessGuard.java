/*
 * Copyright 2012 - 2026 Manuel Laggner
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

import java.lang.StackWalker.Option;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class {@link TmmCoreAccessGuard} provides a runtime guard that prevents externally-loaded SPI addon classes from directly accessing restricted
 * TMM core singletons such as {@link Settings}, {@link org.tinymediamanager.core.movie.MovieModuleManager} and
 * {@link org.tinymediamanager.core.tvshow.TvShowModuleManager}.
 *
 * <p>
 * Addon providers discovered via {@link java.util.ServiceLoader} must register their classes through {@link #registerAddonClass(Class)}. Once
 * registered, any call originating from such a class — or from a class loaded by a different {@link ClassLoader} than the TMM core — will cause
 * {@link #checkAccess()} to throw a {@link SecurityException}.
 * </p>
 *
 * <h2>Performance design</h2>
 * <p>
 * The guard is optimised for the common, non-addon call path:
 * </p>
 * <ol>
 * <li><b>Volatile boolean flag</b> – {@code guardActive} is a single volatile boolean that is set to {@code true} only when the first addon class is
 * registered. Reading a volatile boolean costs roughly 2–5 ns; the guard returns immediately while no addon has been loaded (typically for the entire
 * lifetime of a non-addon installation).</li>
 * <li><b>O(1) class-name lookup</b> – Registered class names are stored in an immutable {@link HashSet} published via a volatile reference. Each
 * {@code contains()} check is O(1) on average, compared to the O(n) iteration of a {@link java.util.concurrent.CopyOnWriteArrayList}-backed set.
 * Writes use a copy-on-write pattern under {@code synchronized} so that the volatile read always sees a fully-constructed set.</li>
 * <li><b>Bounded stack walk</b> – The {@link StackWalker} walk is capped at {@value #MAX_FRAMES} frames after skipping the two guard/singleton
 * frames. Addon code calling {@code getInstance()} is always near the top of the call stack; walking a 60-frame JDK/Swing deep stack in full is
 * unnecessary and expensive.</li>
 * </ol>
 *
 * <h2>Detection strategy</h2>
 * <ol>
 * <li><b>Class-name registry</b> – The exact binary name of every class provided by an {@code IAddonProvider} is stored. Covers the common
 * flat-classpath deployment where all JARs share the same {@link ClassLoader}.</li>
 * <li><b>ClassLoader registry</b> – When an addon class is loaded by a {@link ClassLoader} that differs from the one that loaded
 * {@code TmmCoreAccessGuard}, that foreign loader is also stored. Covers future plugin architectures that isolate each addon in its own
 * {@link java.net.URLClassLoader}.</li>
 * </ol>
 *
 * @author Manuel Laggner
 * @since 5.2.11
 */
public final class TmmCoreAccessGuard {

  private static final Logger           LOGGER            = LoggerFactory.getLogger(TmmCoreAccessGuard.class);

  /**
   * Maximum number of stack frames inspected after skipping the two guard frames (checkAccess + getInstance). Addon code is always shallow in the
   * call stack relative to the guarded method; capping at this value prevents walking an entire 60+ frame Swing/JDK stack on every innocent
   * {@code getInstance()} call.
   */
  private static final int              MAX_FRAMES        = 30;

  /** the {@link ClassLoader} used to load the TMM core itself; every class loaded by this loader is considered trusted */
  private static final ClassLoader      CORE_CLASS_LOADER = TmmCoreAccessGuard.class.getClassLoader();

  /** the pre-built {@link StackWalker} instance; reused across all invocations */
  private static final StackWalker      WALKER            = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

  /**
   * Primary fast-path flag. Remains {@code false} until the first addon class is registered so that the common non-addon path pays only the cost of a
   * single volatile read (~2–5 ns) instead of entering the StackWalker.
   */
  private static volatile boolean       guardActive       = false;

  /**
   * Immutable snapshot of registered addon class binary names, published via a volatile write so that readers always observe a fully-constructed
   * {@link HashSet} (O(1) {@code contains}). Writes replace the entire set under {@code synchronized (TmmCoreAccessGuard.class)}.
   */
  private static volatile Set<String>   restrictedClasses = Collections.emptySet();

  /**
   * Set of {@link ClassLoader}s that loaded at least one registered SPI addon class and that are different from {@link #CORE_CLASS_LOADER}. Entries
   * are only ever added (never removed), so a plain {@link HashSet} with synchronised writes is sufficient; the volatile {@link #guardActive} flag
   * guarantees visibility.
   */
  private static final Set<ClassLoader> addonClassLoaders = new HashSet<>();

  private TmmCoreAccessGuard() {
    throw new IllegalAccessError("TmmCoreAccessGuard is a utility class and must not be instantiated");
  }

  /**
   * Registers an SPI addon class so that any future call originating from it will be rejected by {@link #checkAccess()}.
   *
   * <p>
   * This method is thread-safe and idempotent. It is intended to be called once per addon class at startup, from
   * {@code MediaProviders.loadMediaProviders()}, before any scraping begins.
   * </p>
   *
   * @param addonClass
   *          the addon class to register; silently ignored when {@code null}
   */
  public static void registerAddonClass(Class<?> addonClass) {
    if (addonClass == null) {
      return;
    }

    synchronized (TmmCoreAccessGuard.class) {
      // copy-on-write: build a new HashSet from the existing snapshot, add the new entry, publish atomically
      Set<String> updated = new HashSet<>(restrictedClasses);
      updated.add(addonClass.getName());
      restrictedClasses = Collections.unmodifiableSet(updated); // volatile write – safely publishes the new set

      // track the classloader when it differs from the TMM core loader (future isolated-classloader scenario)
      ClassLoader cl = addonClass.getClassLoader();
      if (cl != null && cl != CORE_CLASS_LOADER) {
        addonClassLoaders.add(cl);
      }

      // activate the guard after the sets are fully updated
      guardActive = true;
    }

    LOGGER.debug("Registered SPI addon class as restricted: {}", addonClass.getName());
  }

  /**
   * Checks whether the current call originates (directly or transitively) from a registered SPI addon class.
   *
   * <p>
   * If a restricted class or a class loaded by a registered foreign {@link ClassLoader} is found within the first {@value #MAX_FRAMES} frames of the
   * call stack, a {@link SecurityException} is thrown and the offending class name is logged at ERROR level.
   * </p>
   *
   * <p>
   * The method returns in roughly 2–5 ns (single volatile read) when no addon class has been registered. Once addons are present the
   * {@link StackWalker} walk is bounded to {@value #MAX_FRAMES} frames and each frame check performs an O(1) {@link HashSet#contains} lookup.
   * </p>
   *
   * @throws SecurityException
   *           if the call originates from a registered SPI addon class or from a class loaded by a foreign {@link ClassLoader}
   */
  public static void checkAccess() {
    // fast path: single volatile read – returns immediately when no addon has ever been registered
    if (!guardActive) {
      return;
    }

    // capture volatile references once to avoid repeated volatile reads inside the lambda
    final Set<String> classes = restrictedClasses;
    final Set<ClassLoader> loaders = addonClassLoaders;

    Optional<StackWalker.StackFrame> offender = WALKER.walk(stream -> stream
        // skip this method itself and the guarded getInstance() frame
        .skip(2)
        // cap the walk: addon code is always near the top of the stack
        .limit(MAX_FRAMES)
        .filter(frame -> {
          Class<?> declaring = frame.getDeclaringClass();

          // primary check: O(1) HashSet lookup on the binary class name
          if (classes.contains(declaring.getName())) {
            return true;
          }

          // secondary check: class loaded by a known foreign ClassLoader
          ClassLoader cl = declaring.getClassLoader();
          return cl != null && loaders.contains(cl);
        })
        .findFirst());

    offender.ifPresent(frame -> {
      String offenderClass = frame.getClassName();
      LOGGER.error("Illegal access to TMM core singleton from SPI addon class '{}'", offenderClass);
      throw new SecurityException("SPI addon class '" + offenderClass + "' is not permitted to directly access TMM core singletons "
          + "(Settings, MovieModuleManager, TvShowModuleManager). " + "Use the scraper options passed to your provider method instead.");
    });
  }
}
