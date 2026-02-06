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
package org.tinymediamanager.ui.movies.filters;

import static org.tinymediamanager.core.MediaFileType.AUDIO;
import static org.tinymediamanager.core.MediaFileType.VIDEO;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * this class is used for a audio codec movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieAudioCodecFilter extends AbstractCheckComboBoxMovieUIFilter<String> {

  public MovieAudioCodecFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));

    buildAndInstallAudioCodecArray();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallAudioCodecArray());
  }

  @Override
  public String getId() {
    return "movieAudioCodec";
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> audioCodecs = checkComboBox.getSelectedItems();

    // check all audio codecs of all VIDEO and AUDIO files
    for (MediaFile mf : movie.getMediaFiles(VIDEO, AUDIO)) {
      for (String audioCodec : mf.getAudioCodecList()) {
        if (audioCodecs.contains(audioCodec)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.audiocodec"));
  }

  private void buildAndInstallAudioCodecArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<String> audioCodecsInMovies = new HashSet<>(movieList.getAudioCodecsInMovies());

    if (!SetUtils.equals(oldValues, audioCodecsInMovies)) {
      oldValues.clear();
      oldValues.addAll(audioCodecsInMovies);

      List<String> sortedAudioCodecs = ListUtils.asSortedList(audioCodecsInMovies);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedAudioCodecs));
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
