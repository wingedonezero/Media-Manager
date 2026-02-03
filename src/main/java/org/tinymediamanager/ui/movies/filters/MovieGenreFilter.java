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

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.Event;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * this class is used for a genre movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieGenreFilter extends AbstractCheckComboBoxMovieUIFilter<MediaGenres> {
  private final Comparator<MediaGenres> comparator;
  private final MovieList               movieList;
  private final Set<MediaGenres>        oldGenres;

  public MovieGenreFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toString().toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));
    movieList = MovieModuleManager.getInstance().getMovieList();
    comparator = new MediaGenres.MediaGenresComparator();
    oldGenres = new HashSet<>();

    buildAndInstallMediaGenres();
    EventBus.registerListener(EventBus.TOPIC_MOVIES, event -> {
      if (event.sender() instanceof Movie) {
        if (event.eventType().equals(Event.TYPE_SAVE)) {
          buildAndInstallMediaGenres();
        }
      }
    });
  }

  @Override
  public String getId() {
    return "movieGenre";
  }

  @Override
  public boolean accept(Movie movie) {
    List<MediaGenres> selectedItems = checkComboBox.getSelectedItems();

    // check for explicit empty search
    if (selectedItems.isEmpty() && movie.getGenres().isEmpty()) {
      return true;
    }

    // check for all values
    for (MediaGenres genre : movie.getGenres()) {
      if (selectedItems.contains(genre)) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("movieextendedsearch.genre"));
  }

  private void buildAndInstallMediaGenres() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    boolean dirty = false;

    Set<MediaGenres> genres = new HashSet<>(movieList.getUsedGenres());

    if (oldGenres.size() != genres.size()) {
      dirty = true;
    }
    else if (!oldGenres.containsAll(genres) || !genres.containsAll(oldGenres)) {
      dirty = true;
    }

    if (dirty) {
      oldGenres.clear();
      oldGenres.addAll(genres);

      SwingUtilities.invokeLater(() -> setValues(ListUtils.asSortedList(genres, comparator)));
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
