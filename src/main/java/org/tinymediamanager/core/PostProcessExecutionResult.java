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

import java.util.ArrayList;
import java.util.List;

/**
 * The record {@link PostProcessExecutionResult} stores the command invocation and output of one post-processing execution.
 *
 * @param postProcess
 *          the post-process configuration which triggered this execution
 * @param entityName
 *          the name of the processed entity (e.g. movie title or movie set name)
 * @param command
 *          the executed command line as passed to {@link ProcessBuilder}
 * @param output
 *          the collected process output (stdout/stderr)
 * @author Manuel Laggner
 */
public record PostProcessExecutionResult(PostProcess postProcess, String entityName, List<String> command, String output) {

  /**
   * Creates an immutable execution result by copying mutable input arguments.
   *
   * @param postProcess
   *          the post-process configuration
   * @param command
   *          the command list
   * @param output
   *          the process output
   */
  public PostProcessExecutionResult {
    command = command != null ? List.copyOf(new ArrayList<>(command)) : List.of();
    output = output != null ? output : "";
  }
}
