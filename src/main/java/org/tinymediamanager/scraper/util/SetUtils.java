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
package org.tinymediamanager.scraper.util;

import java.util.Set;

/**
 * The Class {@link SetUtils} provides utility methods for comparing sets.
 * <p>
 * This class contains static helpers and is not intended to be instantiated.
 * </p>
 *
 * @author Manuel Laggner
 */
public class SetUtils {

  private SetUtils() {
    throw new IllegalAccessError();
  }

  /**
   * Compares two sets for equality, treating {@code null} as a valid value.
   * <p>
   * This method returns {@code true} when both sets are {@code null}, and {@code false} when only one set is {@code null}. For non-null sets, it
   * checks size and membership equivalence.
   * </p>
   *
   * @param set1
   *          the first set to compare
   * @param set2
   *          the second set to compare
   * @param <E>
   *          the element type
   * @return {@code true} if both sets are equal, otherwise {@code false}
   */
  public static <E> boolean equals(Set<E> set1, Set<E> set2) {
    if (set1 == null && set2 == null) {
      return true;
    }

    if (set1 == null || set2 == null) {
      return false;
    }

    return set1.equals(set2);
  }
}
