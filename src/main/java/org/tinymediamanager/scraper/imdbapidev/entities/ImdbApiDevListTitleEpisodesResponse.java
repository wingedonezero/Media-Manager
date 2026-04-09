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
 * The class {@link ImdbApiDevListTitleEpisodesResponse} represents the response from the /titles/{titleId}/episodes endpoint.
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevListTitleEpisodesResponse {

  /** The list of episodes */
  @SerializedName("episodes")
  public List<ImdbApiDevEpisode> episodes;

  /** The total count of episodes that have aired */
  @SerializedName("totalCount")
  public Integer                 totalCount;

  /** A token for the next page of results */
  @SerializedName("nextPageToken")
  public String                  nextPageToken;
}
