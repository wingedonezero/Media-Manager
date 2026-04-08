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
 * The class {@link ImdbApiDevCountry} represents a country as returned by the imdbapi.dev API.
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevCountry {

  /** The ISO 3166-1 alpha-2 country code (e.g. "US", "DE") */
  @SerializedName("code")
  public String code;

  /** The country name in English */
  @SerializedName("name")
  public String name;
}
