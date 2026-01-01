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
package org.tinymediamanager.scraper.subdl.model;

import java.util.Date;

import org.tinymediamanager.scraper.entities.BaseJsonEntity;

public class Results extends BaseJsonEntity {
  public int    subdlId;
  public String type;
  public String name;
  public String imdbId;
  public int    tmdbId;
  public Date   firstAirDate;
  public String slug;
  public Date   releaseDate;
  public int    year;
}
