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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTableFormat;

/**
 * This class implements a tag filter for movies
 * 
 * @author Manuel Laggner
 */
public class MovieTagFilter extends AbstractCheckComboBoxMovieUIFilter<String> {
  private final Comparator<String> comparator;

  public MovieTagFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));
    comparator = new TmmTableFormat.StringComparator();

    buildAndInstallTagsArray();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallTagsArray());
  }

  @Override
  public String getId() {
    return "movieTag";
  }

  @Override
  protected JComboBox<FilterOption> createOptionComboBox() {
    JComboBox<FilterOption> comboBox = new JComboBox<>(new FilterOption[] { FilterOption.ANY, FilterOption.ALL });
    comboBox.setSelectedItem(FilterOption.ANY);

    return comboBox;
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> selectedItems = checkComboBox.getSelectedItems();

    // check for explicit empty search
    if (selectedItems.isEmpty() && movie.getTags().isEmpty()) {
      return true;
    }

    // check for all values
    if (getFilterOption() == FilterOption.ALL) {
      return ListUtils.containsAll(movie.getTags(), selectedItems);
    }
    else {
      return ListUtils.containsAny(movie.getTags(), selectedItems);
    }
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("movieextendedsearch.tag"));
  }

  private void buildAndInstallTagsArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<String> tags = new HashSet<>(movieList.getTagsInMovies());

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
