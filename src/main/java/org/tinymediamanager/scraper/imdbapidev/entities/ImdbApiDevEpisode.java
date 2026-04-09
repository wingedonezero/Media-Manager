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

import com.google.gson.annotations.SerializedName;

/**
 * The class {@link ImdbApiDevEpisode} represents a TV episode as returned by the imdbapi.dev API.
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevEpisode {

  /** The unique IMDb identifier for this episode (e.g. "tt1234567") */
  @SerializedName("id")
  public String                  id;

  /** The episode title */
  @SerializedName("title")
  public String                  title;

  /** The primary image associated with this episode */
  @SerializedName("primaryImage")
  public ImdbApiDevImage         primaryImage;

  /** The season number as a string */
  @SerializedName("season")
  public String                  season;

  /** The episode number within its season */
  @SerializedName("episodeNumber")
  public Integer                 episodeNumber;

  /** The runtime in seconds */
  @SerializedName("runtimeSeconds")
  public Integer                 runtimeSeconds;

  /** A brief plot summary */
  @SerializedName("plot")
  public String                  plot;

  /** The IMDb rating for this episode */
  @SerializedName("rating")
  public ImdbApiDevRating        rating;

  /** The release/air date of this episode */
  @SerializedName("releaseDate")
  public ImdbApiDevPrecisionDate releaseDate;
}
