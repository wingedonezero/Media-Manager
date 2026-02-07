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
 * the class {@link MovieCountSubtitleFilter} is used to provide a filter for subtitle count of movies
 * 
 * @author Wolfgang Janes
 */
public class MovieCountSubtitleFilter extends AbstractCheckComboBoxMovieUIFilter<Integer> {

  public MovieCountSubtitleFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> String.valueOf(s).startsWith(s2));

    buildAndInstallSubtitleCountArray();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallSubtitleCountArray());
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
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.subtitles"));
  }

  @Override
  public String getId() {
    return "movieCountSubtitle";
  }

  @Override
  public boolean accept(Movie movie) {
    List<Integer> selectedItems = checkComboBox.getSelectedItems();
    return selectedItems.contains(movie.getMediaInfoSubtitleStreamCount());
  }

  private void buildAndInstallSubtitleCountArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<Integer> subtitleCountInMovies = new HashSet<>(movieList.getSubtitlesInMovies());

    if (!SetUtils.equals(oldValues, subtitleCountInMovies)) {
      oldValues.clear();
      oldValues.addAll(subtitleCountInMovies);

      List<Integer> sortedSubtilteCount = ListUtils.asSortedList(subtitleCountInMovies);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedSubtilteCount));
    }
  }
}
