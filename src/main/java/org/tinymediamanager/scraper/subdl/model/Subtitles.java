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

import org.tinymediamanager.scraper.entities.BaseJsonEntity;

public class Subtitles extends BaseJsonEntity {
  public String  releaseName;
  public String  name;
  public String  lang;
  public String  author;
  public String  url;
  public String  subtitlePage;
  public int     season;
  public int     episode;
  public String  language;
  public boolean hi;
  public int     episodeFrom;
  public int     episodeEnd;
  public boolean fullSeason;
}
