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
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.bus.EventBus;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.SetUtils;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * This class implements a frame rate filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowFrameRateFilter extends AbstractCheckComboBoxTvShowUIFilter<Double> {

  public TvShowFrameRateFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> String.valueOf(s).startsWith(s2));

    buildAndInstallFrameRateArray();
    EventBus.registerListener(EventBus.TOPIC_TV_SHOWS_UI, event -> buildAndInstallFrameRateArray());
  }

  @Override
  public String getId() {
    return "tvShowFrameRate";
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<Double> frameRates = checkComboBox.getSelectedItems();

    for (Double frameRate : frameRates) {
      if (invert ^ frameRate == 0) {
        return true;
      }

      // search codec in the episodes
      for (TvShowEpisode episode : episodes) {
        if (invert ^ frameRate == episode.getMediaInfoFrameRate()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.framerate"));
  }

  private void buildAndInstallFrameRateArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    Set<Double> frameRateInEpisodes = new HashSet<>(tvShowList.getFrameRatesInEpisodes());

    if (!SetUtils.equals(oldValues, frameRateInEpisodes)) {
      oldValues.clear();
      oldValues.addAll(frameRateInEpisodes);

      List<Double> sortedFrameRates = ListUtils.asSortedList(frameRateInEpisodes);

      // update the combobox in the EDT
      SwingUtilities.invokeLater(() -> setValues(sortedFrameRates));
    }
  }

  @Override
  protected String parseTypeToString(Double type) throws Exception {
    return type.toString();
  }

  @Override
  protected Double parseStringToType(String string) throws Exception {
    return Double.parseDouble(string);
  }
}
