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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.tvshow.TvShowEpisodeEdition;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * This class implements an edition filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowEditionFilter extends AbstractCheckComboBoxTvShowUIFilter<TvShowEpisodeEdition> {
  private final TvShowEpisodeEdition.TvShowEditionComparator comparator;

  public TvShowEditionFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toString().toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));
    comparator = new TvShowEpisodeEdition.TvShowEditionComparator();
    buildAndInstallEditionArray();
    TvShowEpisodeEdition.addListener(evt -> SwingUtilities.invokeLater(this::buildAndInstallEditionArray));
  }

  @Override
  public String getId() {
    return "tvShowEdition";
  }

  @Override
  public boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<TvShowEpisodeEdition> selectedItems = checkComboBox.getSelectedItems();

    // search for media source in episodes
    for (TvShowEpisode episode : episodes) {
      if (invert ^ selectedItems.contains(episode.getEdition())) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.edition"));
  }

  private void buildAndInstallEditionArray() {
    List<TvShowEpisodeEdition> editions = new ArrayList<>(Arrays.asList(TvShowEpisodeEdition.values()));

    editions.sort(comparator);
    setValues(editions);
  }

  @Override
  protected String parseTypeToString(TvShowEpisodeEdition type) throws Exception {
    return type.name();
  }

  @Override
  protected TvShowEpisodeEdition parseStringToType(String string) throws Exception {
    return TvShowEpisodeEdition.getTvShowEpisodeEdition(string);
  }
}
