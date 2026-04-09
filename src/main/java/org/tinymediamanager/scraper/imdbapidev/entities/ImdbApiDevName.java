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
package org.tinymediamanager.scraper.imdbapidev.entities;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The class {@link ImdbApiDevName} represents a person (actor, director, writer, etc.) as returned by the imdbapi.dev API.
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevName {

  /** The unique IMDb name identifier (e.g. "nm0000093") */
  @SerializedName("id")
  public String          id;

  /** The display name of the person */
  @SerializedName("displayName")
  public String          displayName;

  /** The primary image associated with the person */
  @SerializedName("primaryImage")
  public ImdbApiDevImage primaryImage;

  /** A list of primary professions (e.g. "Actor", "Director") */
  @SerializedName("primaryProfessions")
  public List<String>    primaryProfessions;

  /** A brief biography of the person */
  @SerializedName("biography")
  public String          biography;
}
