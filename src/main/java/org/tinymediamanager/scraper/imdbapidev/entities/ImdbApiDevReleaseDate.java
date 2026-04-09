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
 * The class {@link ImdbApiDevReleaseDate} represents one release-info entry as returned by the imdbapi.dev API.
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevReleaseDate {

  /** The release country object */
  @SerializedName(value = "country", alternate = { "releaseCountry", "region" })
  public ImdbApiDevCountry       country;

  /** The release country code if exposed as plain string */
  @SerializedName(value = "countryCode", alternate = { "regionCode", "isoCode" })
  public String                  countryCode;

  /** The release date object */
  @SerializedName(value = "releaseDate", alternate = { "date" })
  public ImdbApiDevPrecisionDate releaseDate;

  /** Flag indicating whether this entry is marked as the primary/main release */
  @SerializedName(value = "isMain", alternate = { "main", "primary" })
  public Boolean                 isMain;
}
