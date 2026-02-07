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
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * this class is used for a movie languages from mediainfo
 * 
 * @author Wolfgang Janes
 */
public class MovieAudioLanguageFilter extends AbstractCheckComboBoxMovieUIFilter<String> {

  public MovieAudioLanguageFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.contains(s2.toLowerCase(Locale.ROOT)));

    buildAndInstallAudioLanguageArray();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallAudioLanguageArray());
  }

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.language"));
  }

  @Override
  public String getId() {
    return "movieAudioLanguage";
  }

  @Override
  public boolean accept(Movie movie) {

    List<String> selectedItems = checkComboBox.getSelectedItems();
    List<MediaFile> mediaFileList = movie.getMediaFiles(MediaFileType.VIDEO);

    for (MediaFile mf : mediaFileList) {
      // check for explicit empty search
      if (selectedItems.isEmpty() && mf.getAudioLanguagesList().isEmpty()) {
        return true;
      }
      for (String lang : mf.getAudioLanguagesList()) {
        if (selectedItems.contains(lang)) {
          return true;
        }
      }
    }

    return false;
  }

  private void buildAndInstallAudioLanguageArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<String> audioLanguagesInMovies = new HashSet<>(movieList.getAudioLanguagesInMovies());

    if (!SetUtils.equals(oldValues, audioLanguagesInMovies)) {
      oldValues.clear();
      oldValues.addAll(audioLanguagesInMovies);

      List<String> sortedAudioLanguages = ListUtils.asSortedList(audioLanguagesInMovies);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedAudioLanguages));
    }
  }
}
