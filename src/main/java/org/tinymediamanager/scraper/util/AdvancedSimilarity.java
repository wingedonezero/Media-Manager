/*
 * Copyright 2012 - 2025 Manuel Laggner
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

import java.util.Locale;

/**
 * The Class {@link AdvancedSimilarity} provides string similarity computation using the Jaro-Winkler algorithm. It is intended for fuzzy matching of
 * media titles and other short text comparisons used by scrapers and metadata matchers.
 * <p>
 * The comparison is case-insensitive and performs basic normalization (trim, collapse whitespace, uppercase using Locale.ROOT). The returned
 * similarity value is in the range {@code 0.0} (no similarity) to {@code 1.0} (exact match).
 * </p>
 *
 * @author Myron Boyle (gpt-5 mini)
 */
public final class AdvancedSimilarity {
  private AdvancedSimilarity() {
  }

  /**
   * Compares two strings and returns a similarity score using Jaro-Winkler.
   * <p>
   * Null inputs are treated as no similarity (returns {@code 0.0f}). Both inputs are normalized before comparison. If the normalized values are equal
   * (case-insensitive), the method returns {@code 1.0f} immediately.
   * </p>
   *
   * @param s1
   *          the first string to compare, may be {@code null}
   * @param s2
   *          the second string to compare, may be {@code null}
   * @return a float similarity score between {@code 0.0f} and {@code 1.0f}
   */
  public static float compareStrings(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return 0.0f;
    }
    String a = normalize(s1);
    String b = normalize(s2);
    if (a.equalsIgnoreCase(b)) {
      return 1.0f;
    }
    double jw = jaroWinkler(a, b);
    if (Double.isNaN(jw) || jw < 0.0) {
      return 0.0f;
    }
    return (float) jw;
  }

  /**
   * Normalizes the input string for comparison.
   * <p>
   * Normalization steps:
   * <ul>
   * <li>Null becomes an empty string</li>
   * <li>Leading and trailing whitespace is trimmed</li>
   * <li>Consecutive whitespace characters are collapsed into a single space</li>
   * <li>Converted to upper case using {@link Locale#ROOT} for case-insensitive comparison</li>
   * </ul>
   * </p>
   *
   * @param s
   *          the input string, may be {@code null}
   * @return the normalized string, never {@code null}
   */
  private static String normalize(String s) {
    if (s == null) {
      return "";
    }
    // trim, collapse whitespace, uppercase for case-insensitive comparison
    return s.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
  }

  /**
   * Computes the Jaro-Winkler similarity between two normalized strings.
   * <p>
   * This method computes the Jaro distance and applies the Winkler prefix boost with a scaling factor of {@code 0.1} and a maximum prefix length of
   * {@code 4}.
   * </p>
   *
   * @param s1
   *          the first normalized string, must not be {@code null}
   * @param s2
   *          the second normalized string, must not be {@code null}
   * @return the Jaro-Winkler similarity as a double in range {@code 0.0}..{@code 1.0}
   */
  private static double jaroWinkler(String s1, String s2) {
    double jaro = jaroDistance(s1, s2);
    // standard Winkler boost parameters
    final double scalingFactor = 0.1;
    // compute common prefix up to 4 chars
    int prefix = 0;
    int maxPrefix = 4;
    int lim = Math.min(Math.min(s1.length(), s2.length()), maxPrefix);
    for (int i = 0; i < lim; i++) {
      if (s1.charAt(i) == s2.charAt(i)) {
        prefix++;
      }
      else {
        break;
      }
    }
    return jaro + prefix * scalingFactor * (1.0 - jaro);
  }

  /**
   * Computes the Jaro distance between two strings.
   * <p>
   * The Jaro distance is based on the number of matching characters and transpositions. Matching characters are those within a matching window
   * defined as {@code floor(max(len1,len2)/2)-1}. Transpositions are half the number of matched characters that are in different positions.
   * </p>
   *
   * @param s1
   *          the first string, must not be {@code null}
   * @param s2
   *          the second string, must not be {@code null}
   * @return the Jaro distance as a double in range {@code 0.0}..{@code 1.0}
   */
  private static double jaroDistance(String s1, String s2) {
    if (s1.isEmpty() && s2.isEmpty()) {
      return 1.0;
    }
    if (s1.isEmpty() || s2.isEmpty()) {
      return 0.0;
    }
    int len1 = s1.length();
    int len2 = s2.length();
    int matchDistance = Math.max(len1, len2) / 2 - 1;
    if (matchDistance < 0) {
      matchDistance = 0;
    }

    boolean[] s1Matches = new boolean[len1];
    boolean[] s2Matches = new boolean[len2];
    int matches = 0;

    for (int i = 0; i < len1; i++) {
      int start = Math.max(0, i - matchDistance);
      int end = Math.min(len2 - 1, i + matchDistance);
      for (int j = start; j <= end; j++) {
        if (!s2Matches[j] && s1.charAt(i) == s2.charAt(j)) {
          s1Matches[i] = true;
          s2Matches[j] = true;
          matches++;
          break;
        }
      }
    }

    if (matches == 0) {
      return 0.0;
    }

    int t = 0;
    int k = 0;
    for (int i = 0; i < len1; i++) {
      if (!s1Matches[i])
        continue;
      while (!s2Matches[k]) {
        k++;
      }
      if (s1.charAt(i) != s2.charAt(k)) {
        t++;
      }
      k++;
    }
    double transpositions = t / 2.0;

    return ((matches / (double) len1) + (matches / (double) len2) + ((matches - transpositions) / matches)) / 3.0;
  }
}
