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
 * the class {@link MovieCountAudioStreamFilter} is used to provide a filter for the count of audio streams
 * 
 * @author Wolfgang Janes
 */
public class MovieCountAudioStreamFilter extends AbstractCheckComboBoxMovieUIFilter<Integer> {

  public MovieCountAudioStreamFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> String.valueOf(s).startsWith(s2));

    buildAndInstallAudioStreamCountArray();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallAudioStreamCountArray());
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
    return new TmmLabel(TmmResourceBundle.getString("metatag.countAudioStreams"));
  }

  @Override
  public String getId() {
    return "movieCountAudioStream";
  }

  @Override
  public boolean accept(Movie movie) {
    List<Integer> selectedItems = checkComboBox.getSelectedItems();
    return selectedItems.contains(movie.getMediaInfoAudioStreamCount());
  }

  private void buildAndInstallAudioStreamCountArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<Integer> audioStreamCountInMovies = new HashSet<>(movieList.getAudioStreamsInMovies());

    if (!SetUtils.equals(oldValues, audioStreamCountInMovies)) {
      oldValues.clear();
      oldValues.addAll(audioStreamCountInMovies);

      List<Integer> sortedAudioStreamCount = ListUtils.asSortedList(audioStreamCountInMovies);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedAudioStreamCount));
    }
  }
}
