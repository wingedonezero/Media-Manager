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
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTableFormat;

/**
 * This class implements a tag filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowTagFilter extends AbstractCheckComboBoxTvShowUIFilter<String> {
  private final TmmTableFormat.StringComparator comparator;

  public TvShowTagFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));
    comparator = new TmmTableFormat.StringComparator();

    buildAndInstallTagsArray();
    EventBus.registerListener(EventBus.TOPIC_TV_SHOWS_UI, event -> buildAndInstallTagsArray());
  }

  @Override
  public String getId() {
    return "tvShowTag";
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<String> tags = checkComboBox.getSelectedItems();

    // check for explicit empty search
    if (!invert && (tags.isEmpty() && tvShow.getTags().isEmpty())) {
      return true;
    }
    else if (invert && (tags.isEmpty() && !tvShow.getTags().isEmpty())) {
      return true;
    }

    for (TvShowEpisode episode : episodes) {
      if (!invert && (tags.isEmpty() && episode.getTags().isEmpty())) {
        return true;
      }
      else if (invert && (tags.isEmpty() && !episode.getTags().isEmpty())) {
        return true;
      }
    }

    // search tags of the show
    for (String tag : tags) {
      boolean containsTags = tvShow.getTags().contains(tag);
      if (!invert && containsTags) {
        return true;
      }
      else if (invert && containsTags) {
        return false;
      }

      for (TvShowEpisode episode : episodes) {
        if (invert ^ episode.getTags().contains(tag)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("movieextendedsearch.tag"));
  }

  private void buildAndInstallTagsArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<String> tags = new TreeSet<>(tvShowList.getTagsInTvShows());
    tags.addAll(tvShowList.getTagsInEpisodes());
    Utils.removeDuplicateStringFromCollectionIgnoreCase(tags);

    if (!SetUtils.equals(oldValues, tags)) {
      oldValues.clear();
      oldValues.addAll(tags);

      List<String> sortedTags = ListUtils.asSortedList(tags, comparator);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedTags));
    }
  }

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }
}
