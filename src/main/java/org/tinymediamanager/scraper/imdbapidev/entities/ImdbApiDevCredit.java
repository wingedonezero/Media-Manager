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
 * The class {@link ImdbApiDevCredit} represents a credit (cast or crew member) associated with a title.
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevCredit {

  /** The person associated with this credit */
  @SerializedName("name")
  public ImdbApiDevName name;

  /** The credit category (e.g. "actor", "director", "writer", "producer") */
  @SerializedName("category")
  public String         category;

  /** A list of character names played by the actor in this title */
  @SerializedName("characters")
  public List<String>   characters;

  /** The number of episodes the person appeared in (for TV shows) */
  @SerializedName("episodeCount")
  public Integer        episodeCount;
}
