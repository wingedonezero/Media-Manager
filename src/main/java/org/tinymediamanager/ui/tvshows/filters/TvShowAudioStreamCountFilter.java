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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

public class TvShowAudioStreamCountFilter extends AbstractCheckComboBoxTvShowUIFilter<Integer> {

  public TvShowAudioStreamCountFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> String.valueOf(s).startsWith(s2));

    buildAndInstallAudioStreamCountArray();
    EventBus.registerListener(EventBus.TOPIC_TV_SHOWS_UI, event -> buildAndInstallAudioStreamCountArray());
  }

  @Override
  protected String parseTypeToString(Integer type) throws Exception {
    return type.toString();
  }

  @Override
  protected Integer parseStringToType(String string) throws Exception {
    return Integer.parseInt(string);
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {

    List<Integer> selectedItems = checkComboBox.getSelectedItems();

    for (TvShowEpisode episode : episodes) {
      if (invert ^ selectedItems.contains(episode.getMediaInfoAudioStreamCount())) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.countAudioStreams"));
  }

  @Override
  public String getId() {
    return "countAudioStreamsTvShow";
  }

  private void buildAndInstallAudioStreamCountArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<Integer> audioStreamCountInEpisodes = new HashSet<>(tvShowList.getAudioStreamsInEpisodes());

    if (!SetUtils.equals(oldValues, audioStreamCountInEpisodes)) {
      oldValues.clear();
      oldValues.addAll(audioStreamCountInEpisodes);

      List<Integer> sortedAudioStreamCount = ListUtils.asSortedList(audioStreamCountInEpisodes);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedAudioStreamCount));
    }
  }
}
