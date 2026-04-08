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
 * The class {@link ImdbApiDevCertificate} represents a content rating certificate as returned by the imdbapi.dev API.
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevCertificate {

  /** The rating string (e.g. "PG-13", "R", "FSK 12") */
  @SerializedName("rating")
  public String            rating;

  /** The country this certificate was issued in */
  @SerializedName("country")
  public ImdbApiDevCountry country;
}
