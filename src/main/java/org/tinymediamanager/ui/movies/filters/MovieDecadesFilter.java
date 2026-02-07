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
 * this class is used for filtering movies by decades
 *
 * @author Wolfgang Janes
 */
public class MovieDecadesFilter extends AbstractCheckComboBoxMovieUIFilter<String> {

  public MovieDecadesFilter() {
    super();
    checkComboBox.enableFilter(String::startsWith);

    buildAndInstallDecadeArray();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallDecadeArray());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.decade"));
  }

  @Override
  public String getId() {
    return "movieDecades";
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> selectedItems = checkComboBox.getSelectedItems();
    return selectedItems.contains(movie.getDecadeShort());
  }

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }

  private void buildAndInstallDecadeArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<String> decadesInMovies = new HashSet<>(movieList.getDecadesInMovies());

    if (!SetUtils.equals(oldValues, decadesInMovies)) {
      oldValues.clear();
      oldValues.addAll(decadesInMovies);

      List<String> sortedDecades = ListUtils.asSortedList(decadesInMovies);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedDecades));
    }
  }
}
