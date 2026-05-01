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
package org.tinymediamanager.scraper.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The class {@link CookieFileParser} extracts cookie values from different cookie export formats.
 * <p>
 * The parser uses filename/content heuristics first and falls back to all known formats to handle unexpected user files.
 * </p>
 *
 * @author Manuel Laggner
 */
public final class CookieFileParser {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private enum CookieFileFormat {
    HEADER,
    JSON,
    NETSCAPE
  }

  private CookieFileParser() {
    throw new IllegalAccessError();
  }

  /**
   * Parse the value of a cookie from a cookie file.
   *
   * @param cookieFile
   *          the cookie file path
   * @param cookieName
   *          the cookie name to look up
   * @return the parsed cookie value or an empty {@link Optional} if not found/readable
   */
  public static Optional<String> parseCookieValue(Path cookieFile, String cookieName) {
    if (cookieFile == null || StringUtils.isBlank(cookieName) || !Files.isReadable(cookieFile)) {
      return Optional.empty();
    }

    String content;
    try {
      content = Files.readString(cookieFile, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      return Optional.empty();
    }

    if (StringUtils.isBlank(content)) {
      return Optional.empty();
    }

    List<CookieFileFormat> parseOrder = getParseOrder(cookieFile.getFileName().toString(), content);
    for (CookieFileFormat cookieFileFormat : parseOrder) {
      Optional<String> value = parseByFormat(cookieFileFormat, content, cookieName);
      if (value.isPresent()) {
        return value;
      }
    }

    return Optional.empty();
  }

  /**
   * Parse the value of a cookie from a cookie file.
   *
   * @param cookieFile
   *          the cookie file path
   * @param cookieName
   *          the cookie name to look up
   * @return the parsed cookie value or an empty {@link Optional} if not found/readable
   */
  public static Optional<String> parseCookieValue(String cookieFile, String cookieName) {
    if (StringUtils.isBlank(cookieFile)) {
      return Optional.empty();
    }

    return parseCookieValue(Path.of(cookieFile), cookieName);
  }

  /**
   * Guess the cookie format from filename and content and return the parser order.
   *
   * @param filename
   *          the cookie filename
   * @param content
   *          the cookie file content
   * @return ordered list of formats to try
   */
  private static List<CookieFileFormat> getParseOrder(String filename, String content) {
    List<CookieFileFormat> parseOrder = new ArrayList<>();
    CookieFileFormat guessedFormat = guessFormat(filename, content);

    if (guessedFormat != null) {
      parseOrder.add(guessedFormat);
    }

    for (CookieFileFormat cookieFileFormat : CookieFileFormat.values()) {
      if (!parseOrder.contains(cookieFileFormat)) {
        parseOrder.add(cookieFileFormat);
      }
    }

    return parseOrder;
  }

  /**
   * Guess the cookie file format from filename and content.
   *
   * @param filename
   *          the cookie filename
   * @param content
   *          the cookie file content
   * @return the guessed format
   */
  private static CookieFileFormat guessFormat(String filename, String content) {
    String lowerFilename = StringUtils.defaultString(filename).toLowerCase(Locale.ROOT);
    String trimmedContent = StringUtils.trimToEmpty(content);

    if (lowerFilename.endsWith(".json") || trimmedContent.startsWith("[") || trimmedContent.startsWith("{")) {
      return CookieFileFormat.JSON;
    }

    if (trimmedContent.startsWith("# Netscape HTTP Cookie File") || trimmedContent.contains("\tTRUE\t") || trimmedContent.contains("\tFALSE\t")) {
      return CookieFileFormat.NETSCAPE;
    }

    if (lowerFilename.endsWith(".txt")) {
      return CookieFileFormat.HEADER;
    }

    return null;
  }

  /**
   * Parse a cookie value by the given format.
   *
   * @param cookieFileFormat
   *          the format
   * @param content
   *          the file content
   * @param cookieName
   *          the cookie name
   * @return optional cookie value
   */
  private static Optional<String> parseByFormat(CookieFileFormat cookieFileFormat, String content, String cookieName) {
    return switch (cookieFileFormat) {
      case HEADER -> parseHeaderCookie(content, cookieName);
      case JSON -> parseJsonCookie(content, cookieName);
      case NETSCAPE -> parseNetscapeCookie(content, cookieName);
    };
  }

  /**
   * Parse from a Netscape cookie file format.
   *
   * @param content
   *          file content
   * @param cookieName
   *          cookie name
   * @return optional cookie value
   */
  private static Optional<String> parseNetscapeCookie(String content, String cookieName) {
    for (String line : content.split("\\R")) {
      if (StringUtils.isBlank(line) || line.startsWith("#")) {
        continue;
      }

      String[] fields = line.split("\\t");
      if (fields.length < 7) {
        continue;
      }

      if (Objects.equals(fields[5], cookieName)) {
        return Optional.of(fields[6]);
      }
    }

    return Optional.empty();
  }

  /**
   * Parse from a browser JSON cookie export.
   *
   * @param content
   *          file content
   * @param cookieName
   *          cookie name
   * @return optional cookie value
   */
  private static Optional<String> parseJsonCookie(String content, String cookieName) {
    try {
      JsonNode rootNode = OBJECT_MAPPER.readTree(content);
      return findCookieInJson(rootNode, cookieName);
    }
    catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Parse from a cookie header style format ("a=b; c=d").
   *
   * @param content
   *          file content
   * @param cookieName
   *          cookie name
   * @return optional cookie value
   */
  private static Optional<String> parseHeaderCookie(String content, String cookieName) {
    for (String line : content.split("\\R")) {
      String normalizedLine = StringUtils.trimToEmpty(line);
      if (StringUtils.isBlank(normalizedLine)) {
        continue;
      }

      if (normalizedLine.regionMatches(true, 0, "cookie:", 0, "cookie:".length())) {
        normalizedLine = StringUtils.trim(normalizedLine.substring("cookie:".length()));
      }

      String[] cookiePairs = normalizedLine.split(";");
      for (String cookiePair : cookiePairs) {
        String pair = StringUtils.trimToEmpty(cookiePair);
        int separatorIndex = pair.indexOf('=');
        if (separatorIndex <= 0) {
          continue;
        }

        String candidateName = StringUtils.trim(pair.substring(0, separatorIndex));
        if (Objects.equals(candidateName, cookieName)) {
          return Optional.of(pair.substring(separatorIndex + 1));
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Recursively find a cookie object with matching name/value fields.
   *
   * @param node
   *          current node
   * @param cookieName
   *          cookie name
   * @return optional cookie value
   */
  private static Optional<String> findCookieInJson(JsonNode node, String cookieName) {
    if (node == null || node.isNull()) {
      return Optional.empty();
    }

    if (node.isObject()) {
      JsonNode nameNode = node.get("name");
      JsonNode valueNode = node.get("value");
      if (nameNode != null && valueNode != null && Objects.equals(nameNode.asText(), cookieName)) {
        return Optional.of(valueNode.asText());
      }

      for (JsonNode child : node) {
        Optional<String> value = findCookieInJson(child, cookieName);
        if (value.isPresent()) {
          return value;
        }
      }
    }
    else if (node.isArray()) {
      for (JsonNode child : node) {
        Optional<String> value = findCookieInJson(child, cookieName);
        if (value.isPresent()) {
          return value;
        }
      }
    }

    return Optional.empty();
  }
}
