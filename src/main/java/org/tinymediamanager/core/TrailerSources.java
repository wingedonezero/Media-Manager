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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * The enum TrailerSources
 *
 * @author Manuel Laggner
 */
@JsonDeserialize(using = TrailerSourcesDeserializer.class)
public enum TrailerSources {

  //@formatter:off
  YOUTUBE("Youtube", Collections.singletonList("youtube")),
  IMDB("IMDb", Collections.singletonList("imdb"));  // @formatter:on

  private final String       displayText;
  private final List<String> possibleSources;

  TrailerSources(String text, List<String> sources) {
    this.displayText = text;
    this.possibleSources = sources;
  }

  public boolean containsSource(String source) {
    if (StringUtils.isBlank(source)) {
      return false;
    }
    for (String s : possibleSources) {
      if (source.equalsIgnoreCase(s)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the active trailer sources. Currently only YouTube and IMDb are supported.
   *
   * @return the active trailer sources
   */
  public static TrailerSources[] getActiveTrailerSources() {
    return new TrailerSources[] { YOUTUBE, IMDB };
  }

  @Override
  public String toString() {
    return this.displayText;
  }
}

/**
 * Custom deserializer for {@link TrailerSources} enum that provides a fallback to {@link TrailerSources#YOUTUBE} for unknown or deprecated enum
 * values.
 * <p>
 * This ensures backward compatibility when enum values are removed or renamed.
 * </p>
 *
 * @author Manuel Laggner
 */
class TrailerSourcesDeserializer extends JsonDeserializer<TrailerSources> {

  /**
   * Deserializes a TrailerSources enum value from JSON, falling back to YOUTUBE if the value is unknown.
   *
   * @param jsonParser
   *          the JSON parser
   * @param deserializationContext
   *          the deserialization context
   * @return the deserialized TrailerSources enum value, or YOUTUBE as fallback
   * @throws IOException
   *           if an I/O error occurs
   */
  @Override
  public TrailerSources deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    String value = jsonParser.getValueAsString();

    if (StringUtils.isBlank(value)) {
      return TrailerSources.YOUTUBE;
    }

    try {
      return TrailerSources.valueOf(value);
    }
    catch (IllegalArgumentException e) {
      // Fallback to YOUTUBE for unknown or deleted enum values
      return TrailerSources.YOUTUBE;
    }
  }
}
