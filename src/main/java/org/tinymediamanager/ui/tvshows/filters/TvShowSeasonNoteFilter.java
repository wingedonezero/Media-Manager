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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.List;
import java.util.regex.Matcher;

import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * the class {@link TvShowSeasonNoteFilter} is used to filter seasons for their note
 *
 * @author Wolfgang Janes
 */
public class TvShowSeasonNoteFilter extends AbstractTextTvShowUIFilter {

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.note") + " (" + TmmResourceBundle.getString("metatag.season") + ")");
  }

  @Override
  public String getId() {
    return "TvShowSeasonNote";
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    if (StringUtils.isBlank(normalizedFilterText)) {
      return true;
    }

    try {

      // need to search bottom up, or otherwise all season match if one does
      for (TvShowEpisode episode : episodes) {
        TvShowSeason season = episode.getTvShowSeason();

        boolean foundSeason = false;

        Matcher matcher = filterPattern.matcher(StrgUtils.normalizeString(season.getNote()));
        if (matcher.find()) {
          foundSeason = true;
        }

        if (invert && !foundSeason) {
          return true;
        }
        else if (!invert && foundSeason) {
          return true;
        }
      }
    }
    catch (Exception e) {
      return true;
    }

    return false;
  }
}
