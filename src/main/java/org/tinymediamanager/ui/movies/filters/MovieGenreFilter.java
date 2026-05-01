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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * this class is used for a genre movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieGenreFilter extends AbstractCheckComboBoxMovieUIFilter<MediaGenres> {

  public MovieGenreFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toString().toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));

    buildAndInstallMediaGenres();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallMediaGenres());
  }

  @Override
  public String getId() {
    return "movieGenre";
  }

  @Override
  protected JComboBox<FilterOption> createOptionComboBox() {
    JComboBox<FilterOption> comboBox = new JComboBox<>(new FilterOption[] { FilterOption.ANY, FilterOption.ALL });
    comboBox.setSelectedItem(FilterOption.ANY);

    return comboBox;
  }

  @Override
  public boolean accept(Movie movie) {
    List<MediaGenres> selectedItems = checkComboBox.getSelectedItems();

    // check for explicit empty search
    if (selectedItems.isEmpty() && movie.getGenres().isEmpty()) {
      return true;
    }

    // check for all values
    if (getFilterOption() == FilterOption.ALL) {
      return ListUtils.containsAll(movie.getGenres(), selectedItems);
    }
    else {
      return ListUtils.containsAny(movie.getGenres(), selectedItems);
    }
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("movieextendedsearch.genre"));
  }

  private void buildAndInstallMediaGenres() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<MediaGenres> genres = new HashSet<>(movieList.getUsedGenres());

    if (!SetUtils.equals(oldValues, genres)) {
      oldValues.clear();
      oldValues.addAll(genres);

      List<MediaGenres> sortedGenres = ListUtils.asSortedList(genres);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedGenres));
    }
  }

  @Override
  protected String parseTypeToString(MediaGenres type) throws Exception {
    return type.name();
  }

  @Override
  protected MediaGenres parseStringToType(String string) throws Exception {
    return MediaGenres.getGenre(string);
  }
}
