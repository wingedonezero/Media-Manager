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
import org.tinymediamanager.scraper.entities.MediaCertification;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * this class is used for a watched movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieCertificationFilter extends AbstractCheckComboBoxMovieUIFilter<MediaCertification> {

  public MovieCertificationFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toString().toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));

    buildAndInstallCertificationArray();
    EventBus.registerListener(EventBus.TOPIC_MOVIES_UI, event -> buildAndInstallCertificationArray());
  }

  @Override
  public String getId() {
    return "movieCertification";
  }

  @Override
  public boolean accept(Movie movie) {
    List<MediaCertification> selectedItems = checkComboBox.getSelectedItems();
    return selectedItems.contains(movie.getCertification());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.certification"));
  }

  private void buildAndInstallCertificationArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<MediaCertification> certificationsInMovies = new HashSet<>(movieList.getCertificationsInMovies());

    if (!SetUtils.equals(oldValues, certificationsInMovies)) {
      oldValues.clear();
      oldValues.addAll(certificationsInMovies);

      List<MediaCertification> sortedCertifications = ListUtils.asSortedList(certificationsInMovies);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedCertifications));
    }
  }

  @Override
  protected String parseTypeToString(MediaCertification type) throws Exception {
    return type.name();
  }

  @Override
  protected MediaCertification parseStringToType(String string) throws Exception {
    return MediaCertification.valueOf(string);
  }
}
