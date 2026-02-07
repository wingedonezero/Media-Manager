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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.entities.MediaCertification;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * the class {@link TvShowCertificationFilter} is used to provide a filter for the certifications of a TV show
 * 
 * @author Wolfgang Janes
 */
public class TvShowCertificationFilter extends AbstractCheckComboBoxTvShowUIFilter<MediaCertification> {

  public TvShowCertificationFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toString().toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));

    buildAndInstallCertificationArray();
    EventBus.registerListener(EventBus.TOPIC_TV_SHOWS_UI, event -> buildAndInstallCertificationArray());
  }

  @Override
  protected String parseTypeToString(MediaCertification type) throws Exception {
    return type.name();
  }

  @Override
  protected MediaCertification parseStringToType(String string) throws Exception {
    return MediaCertification.valueOf(string);
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<MediaCertification> selectedItems = checkComboBox.getSelectedItems();
    if (invert) {
      return !selectedItems.contains(tvShow.getCertification());
    }
    else {
      return selectedItems.contains(tvShow.getCertification());
    }

  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.certification"));
  }

  @Override
  public String getId() {
    return "tvShowCertification";
  }

  private void buildAndInstallCertificationArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<MediaCertification> certificationsInTvShows = new HashSet<>(tvShowList.getCertification());

    if (!SetUtils.equals(oldValues, certificationsInTvShows)) {
      oldValues.clear();
      oldValues.addAll(certificationsInTvShows);

      List<MediaCertification> sortedCertifications = ListUtils.asSortedList(certificationsInTvShows);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedCertifications));
    }
  }
}
