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
import java.util.Locale;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * This class implements a video codec filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowVideoCodecFilter extends AbstractCheckComboBoxTvShowUIFilter<String> {

  public TvShowVideoCodecFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));

    buildAndInstallVideoCodecArray();
    EventBus.registerListener(EventBus.TOPIC_TV_SHOWS_UI, event -> buildAndInstallVideoCodecArray());
  }

  @Override
  public String getId() {
    return "tvShowVideoCodec";
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<String> codecs = checkComboBox.getSelectedItems();

    // search codec in the episodes
    for (TvShowEpisode episode : episodes) {
      List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
      for (MediaFile mf : mfs) {
        if (invert ^ codecs.contains(mf.getVideoCodec())) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.videocodec"));
  }

  private void buildAndInstallVideoCodecArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<String> videoCodecsInEpisodes = new HashSet<>(tvShowList.getVideoCodecsInEpisodes());

    if (!SetUtils.equals(oldValues, videoCodecsInEpisodes)) {
      oldValues.clear();
      oldValues.addAll(videoCodecsInEpisodes);

      List<String> sortedVideoCodecs = ListUtils.asSortedList(videoCodecsInEpisodes);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedVideoCodecs));
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
