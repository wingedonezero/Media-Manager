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
 * The class {@link ImdbApiDevTitle} represents a title (movie, TV series, etc.) as returned by the imdbapi.dev API.
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevTitle {

  /** The unique IMDb title identifier (e.g. "tt1234567") */
  @SerializedName("id")
  public String                   id;

  /** The type of the title (e.g. "MOVIE", "TV_SERIES") */
  @SerializedName("type")
  public String                   type;

  /** Whether this title is intended for adult audiences */
  @SerializedName("isAdult")
  public Boolean                  isAdult;

  /** The primary (most recognized) title */
  @SerializedName("primaryTitle")
  public String                   primaryTitle;

  /** The original title as originally released */
  @SerializedName("originalTitle")
  public String                   originalTitle;

  /** The primary image (poster) associated with the title */
  @SerializedName("primaryImage")
  public ImdbApiDevImage          primaryImage;

  /** The start year (release year for movies, first air year for series) */
  @SerializedName("startYear")
  public Integer                  startYear;

  /** The end year (for TV series that have ended) */
  @SerializedName("endYear")
  public Integer                  endYear;

  /** The runtime in seconds */
  @SerializedName("runtimeSeconds")
  public Integer                  runtimeSeconds;

  /** A list of genre strings */
  @SerializedName("genres")
  public List<String>             genres;

  /** The IMDb rating information */
  @SerializedName("rating")
  public ImdbApiDevRating         rating;

  /** A brief plot summary */
  @SerializedName("plot")
  public String                   plot;

  /** A list of directors associated with the title */
  @SerializedName("directors")
  public List<ImdbApiDevName>     directors;

  /** A list of writers associated with the title */
  @SerializedName("writers")
  public List<ImdbApiDevName>     writers;

  /** A list of stars/actors associated with the title */
  @SerializedName("stars")
  public List<ImdbApiDevName>     stars;

  /** A list of countries of origin */
  @SerializedName("originCountries")
  public List<ImdbApiDevCountry>  originCountries;

  /** A list of spoken languages */
  @SerializedName("spokenLanguages")
  public List<ImdbApiDevLanguage> spokenLanguages;
}
