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

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * the class {@link MovieSubtitleLanguageFilter} is used to provide a filter for the movie subtitle language
 * 
 * @author Wolfgang Janes
 */
public class MovieSubtitleLanguageFilter extends AbstractCheckComboBoxMovieUIFilter<String> {

  public MovieSubtitleLanguageFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));

    buildAndInstallSubtitleLanguageArray();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallSubtitleLanguageArray());
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
    return new TmmLabel(TmmResourceBundle.getString("metatag.subtitlelanguage"));
  }

  @Override
  public String getId() {
    return "movieSubtitleLanguage";
  }

  @Override
  public boolean accept(Movie movie) {

    List<String> selectedItems = checkComboBox.getSelectedItems();
    List<String> lang = movie.getMediaInfoSubtitleLanguageList();
    for (String sel : selectedItems) {
      if (lang.contains(sel)) {
        return true;
      }
    }

    return false;
  }

  public void buildAndInstallSubtitleLanguageArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<String> subtitleLanguagesInMovies = new HashSet<>(movieList.getSubtitleLanguagesInMovies());

    if (!SetUtils.equals(oldValues, subtitleLanguagesInMovies)) {
      oldValues.clear();
      oldValues.addAll(subtitleLanguagesInMovies);

      List<String> sortedSubtitleLanguages = ListUtils.asSortedList(subtitleLanguagesInMovies);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedSubtitleLanguages));
    }
  }
}
