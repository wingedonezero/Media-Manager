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
package org.tinymediamanager.core.movie;

import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.scraper.DynaEnum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The Class {@link MovieEdition} models different editions/versions of a movie (e.g., Director's Cut, Extended Edition).
 * <p>
 * This class extends {@link DynaEnum} to allow dynamic addition of new editions at runtime while preserving enum-like behavior, ordering, and
 * comparison semantics. It provides helper methods to parse edition information from user-provided strings, filenames, and localized titles.
 * </p>
 *
 * <p>
 * Instances are globally registered when constructed via {@link #addElement()} to make them discoverable through {@link #values()}.
 * </p>
 *
 * @author Manuel Laggner
 */
public class MovieEdition extends DynaEnum<MovieEdition> {
  private static final Comparator<MovieEdition> COMPARATOR           = new MovieEditionComparator();

  public static final MovieEdition              NONE                 = new MovieEdition("NONE", 0, "", "");
  public static final MovieEdition              DIRECTORS_CUT        = new MovieEdition("DIRECTORS_CUT", 1, "Director's Cut",
      ".Director.?s.(Cut|Edition|Version)");
  public static final MovieEdition              EXTENDED_EDITION     = new MovieEdition("EXTENDED_EDITION", 2, "Extended Edition",
      ".Extended.(Cut|Edition|Version)?");
  public static final MovieEdition              THEATRICAL_EDITION   = new MovieEdition("THEATRICAL_EDITION", 3, "Theatrical Edition",
      ".Theatrical.(Cut|Edition|Version)?");
  public static final MovieEdition              UNRATED              = new MovieEdition("UNRATED", 4, "Unrated", ".Unrated.(Cut|Edition|Version)?");
  public static final MovieEdition              UNCUT                = new MovieEdition("UNCUT", 5, "Uncut", ".Uncut.(Cut|Edition|Version)?");
  public static final MovieEdition              IMAX                 = new MovieEdition("IMAX", 6, "IMAX", "^(IMAX|.*?.IMAX).(Cut|Edition|Version)?");
  public static final MovieEdition              REMASTERED           = new MovieEdition("REMASTERED", 7, "Remastered",
      ".Remastered.(Cut|Edition|Version)?");
  public static final MovieEdition              COLLECTORS_EDITION   = new MovieEdition("COLLECTORS_EDITION", 8, "Collectors Edition",
      ".Collectors.(Cut|Edition|Version)");
  public static final MovieEdition              ULTIMATE_EDITION     = new MovieEdition("ULTIMATE_EDITION", 9, "Ultimate Edition",
      ".Ultimate.(Cut|Edition|Version)");
  public static final MovieEdition              FINAL_CUT            = new MovieEdition("FINAL_CUT", 10, "Final Edition",
      ".Final.(Cut|Edition|Version)");
  public static final MovieEdition              SPECIAL_EDITION      = new MovieEdition("SPECIAL_EDITION", 11, "Special Edition",
      ".Special.(Cut|Edition|Version)");
  public static final MovieEdition              CRITERION_COLLECTION = new MovieEdition("CRITERION_COLLECTION", 12, "Criterion Collection",
      ".Criterion.(Collection|Edition)?");
  public static final MovieEdition              OPEN_MATTE           = new MovieEdition("OPEN_MATTE", 13, "Open Matte",
      ".Open.Matte.(Cut|Edition|Version)?");

  private static final Pattern                  FILENAME_PATTERN     = Pattern.compile("\\{edition\\-(.*?)\\}", Pattern.CASE_INSENSITIVE);
  private final String                          title;
  private final Pattern                         pattern;

  /**
   * Creates a new {@link MovieEdition} with the provided metadata and registers it for global discovery.
   * <p>
   * This constructor is intentionally private to control creation via predefined constants and dynamic parsing paths.
   * </p>
   *
   * @param enumName
   *          the internal enum-style name used for identification and persistence (e.g., "DIRECTORS_CUT")
   * @param ordinal
   *          the ordinal used for ordering and stable comparison
   * @param title
   *          the human-readable title (localized label shown to users)
   * @param pattern
   *          a case-insensitive regular expression that matches occurrences of this edition in file names or free text; if blank, no regex matching
   *          is performed for this edition
   */
  private MovieEdition(String enumName, int ordinal, String title, String pattern) {
    super(enumName, ordinal);
    this.title = title;
    if (StringUtils.isBlank(pattern)) {
      this.pattern = null;
    }
    else {
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    addElement();
  }

  /**
   * Returns the human-readable title of this edition.
   *
   * @return the localized title string; may be empty for {@link #NONE}
   */
  @Override
  public String toString() {
    return title;
  }

  /**
   * Gets the human-readable title associated with this edition.
   *
   * @return the title string
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the case-insensitive regular expression used to detect this edition in filenames or text.
   *
   * @return the compiled {@link Pattern}, or {@code null} if no regex is defined
   */
  public Pattern getPattern() {
    return pattern;
  }

  /**
   * Gets the enum-style name used for serialization and persistence.
   * <p>
   * Annotated with {@link JsonValue} to serialize this edition by its internal name.
   * </p>
   *
   * @return the internal enum-style name (e.g., "DIRECTORS_CUT")
   */
  @JsonValue
  public String getName() {
    return name();
  }

  /**
   * Gets all known {@link MovieEdition} values, sorted in a localized fashion via {@link #COMPARATOR}.
   *
   * @return an array of all editions currently registered
   */
  public static MovieEdition[] values() {
    MovieEdition[] movieEditions = values(MovieEdition.class);
    Arrays.sort(movieEditions, COMPARATOR);
    return movieEditions;
  }

  /**
   * Parses the given input string to resolve the best matching {@link MovieEdition}.
   * <p>
   * Matching is performed in multiple phases for performance and accuracy:
   * </p>
   * <ul>
   * <li>Exact match against the internal {@link #name()} (enum-style name).</li>
   * <li>Case-insensitive match against the human-readable {@link #getTitle()}.</li>
   * <li>Regex match using {@link #getPattern()} for each known edition.</li>
   * <li>Special filename pattern parsing: {@code {edition-...}} yielding a dynamic edition if not found.</li>
   * </ul>
   *
   * @param name
   *          the input string, typically a free text or filename snippet
   * @return the resolved edition; returns {@link #NONE} when no match is found
   */
  public static MovieEdition getMovieEditionFromString(String name) {
    // sort editions by ordinal
    List<MovieEdition> editions = Arrays.asList(values(MovieEdition.class)); // avoid sorting via text
    editions.sort(Comparator.comparingInt(o -> o.ordinal));

    // split checks for performance
    for (MovieEdition edition : editions) {
      // check if the "enum" name matches
      if (edition.name().equals(name)) {
        return edition;
      }
    }

    // check if the printable name matches
    for (MovieEdition edition : editions) {
      if (edition.title.equalsIgnoreCase(name)) {
        return edition;
      }
    }

    // check if any regular expression matches
    for (MovieEdition edition : editions) {
      if (edition.pattern != null) {
        Matcher matcher = edition.pattern.matcher(name);
        if (matcher.find()) {
          return edition;
        }
      }
    }

    // try parsing filename pattern, and create a new one
    Matcher matcher = FILENAME_PATTERN.matcher(name);
    if (matcher.find()) {
      MovieEdition me = getMovieEditionFromString(matcher.group(1)); // call ourself
      if (me == NONE) {
        return new MovieEdition(matcher.group(1), values().length, matcher.group(1), "");
      }
      else {
        return me;
      }
    }
    return NONE;
  }

  /**
   * Resolves or creates a {@link MovieEdition} from the provided name in a non-strict manner.
   * <p>
   * This method delegates to {@link #getMovieEditionFromString(String)} and, if the resolved edition is {@link #NONE}, it dynamically creates and
   * returns a new edition using the provided name.
   * </p>
   *
   * @param name
   *          the input edition name (free text or enum-style name)
   * @return a matching or dynamically created edition; never {@code null}
   */
  public static MovieEdition getMovieEdition(String name) {
    // empty or strict loading of NONE
    if (StringUtils.isBlank(name) || "NONE".equals(name)) {
      return NONE;
    }

    MovieEdition edition = getMovieEditionFromString(name);
    if (edition == NONE) {
      // dynamically create new one
      edition = new MovieEdition(name, values().length, name, "");
    }

    return edition;
  }

  /**
   * Resolves or creates a {@link MovieEdition} from the provided name in a strict manner.
   * <p>
   * Only exact matches against the internal enum-style {@link #name()} are considered a hit. When no predefined edition matches, a new dynamic
   * edition is created with the provided name.
   * </p>
   *
   * @param name
   *          the enum-style name to resolve (e.g., "DIRECTORS_CUT"); blank or "NONE" returns {@link #NONE}
   * @return the matching or dynamically created edition; never {@code null}
   */
  @JsonCreator
  public static MovieEdition getMovieEditionStrict(String name) {
    // empty or strict loading of NONE
    if (StringUtils.isBlank(name) || "NONE".equals(name)) {
      return NONE;
    }

    MovieEdition edition = NONE;

    for (MovieEdition movieEdition : values()) {
      // check if the "enum" name matches
      if (movieEdition.name().equals(name)) {
        edition = movieEdition;
      }
    }

    if (edition == NONE) {
      // dynamically create new one
      edition = new MovieEdition(name, values().length, name, "");
    }

    return edition;
  }

  /**
   * Comparator for sorting {@link MovieEdition} values by their localized titles using a {@link RuleBasedCollator}.
   */
  public static class MovieEditionComparator implements Comparator<MovieEdition> {
    private final RuleBasedCollator stringCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();

    /**
     * Compares two {@link MovieEdition} instances by their {@link #toString()} values (case-insensitive, locale-aware).
     * <p>
     * Null titles are handled by placing non-null titles first.
     * </p>
     *
     * @param o1
     *          the first edition
     * @param o2
     *          the second edition
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second
     */
    @Override
    public int compare(MovieEdition o1, MovieEdition o2) {
      // toString is localized name
      if (o1.toString() == null && o2.toString() == null) {
        return 0;
      }
      if (o1.toString() == null) {
        return 1;
      }
      if (o2.toString() == null) {
        return -1;
      }
      return stringCollator.compare(o1.toString().toLowerCase(Locale.ROOT), o2.toString().toLowerCase(Locale.ROOT));
    }
  }

  /**
   * Adds a new {@link DynaEnumEventListener} to be notified when new {@link MovieEdition} values are added dynamically.
   *
   * @param listener
   *          the listener to register; must not be {@code null}
   */
  public static void addListener(DynaEnumEventListener<MovieEdition> listener) {
    addListener(MovieEdition.class, listener);
  }

  /**
   * Removes a previously registered {@link DynaEnumEventListener}.
   *
   * @param listener
   *          the listener to unregister; must not be {@code null}
   */
  public static void removeListener(DynaEnumEventListener<MovieEdition> listener) {
    removeListener(MovieEdition.class, listener);
  }
}
