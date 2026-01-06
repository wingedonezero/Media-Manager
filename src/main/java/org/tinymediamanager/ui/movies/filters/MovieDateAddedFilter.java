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

import javax.swing.JLabel;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * The class {@link MovieDateAddedFilter} is used for a date added movie filter with comparison options.
 * <p>
 * Supports filtering by date added with options: less than, less than or equal, equal, greater than, greater than or equal, and between.
 * </p>
 *
 * @author Manuel Laggner
 */
public class MovieDateAddedFilter extends AbstractDateMovieFilter {

  @Override
  public String getId() {
    return "movieDateAdded";
  }

  @Override
  public boolean accept(Movie movie) {
    return matchDate(movie.getDateAddedForUi());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.dateadded"));
  }
}
