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

import static org.tinymediamanager.core.entities.MediaStreamInfo.Flags.FLAG_DEFAULT;
import static org.tinymediamanager.core.entities.MediaStreamInfo.Flags.FLAG_FORCED;
import static org.tinymediamanager.core.entities.MediaStreamInfo.Flags.FLAG_HEARING_IMPAIRED;

import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.entities.MediaStreamInfo;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.label.TmmLabel;

/**
 * the class {@link MovieSubtitleTypeFilter} is used to provide a filter for the movie subtitle formats
 * 
 * @author Manuel Laggner
 */
public class MovieSubtitleTypeFilter extends AbstractCheckComboBoxMovieUIFilter<MediaStreamInfo.Flags> {

  public MovieSubtitleTypeFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toString().toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT)));
    checkComboBox.setItems(List.of(FLAG_DEFAULT, FLAG_FORCED, FLAG_HEARING_IMPAIRED));
  }

  @Override
  protected String parseTypeToString(MediaStreamInfo.Flags type) throws Exception {
    return type.name();
  }

  @Override
  protected MediaStreamInfo.Flags parseStringToType(String string) throws Exception {
    try {
      return MediaStreamInfo.Flags.valueOf(string);
    }
    catch (Exception ignored) {
      return null;
    }
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(TmmResourceBundle.getString("metatag.subtitletype"));
  }

  @Override
  public String getId() {
    return "movieSubtitleType";
  }

  @Override
  public boolean accept(Movie movie) {

    List<MediaStreamInfo.Flags> selectedItems = checkComboBox.getSelectedItems();
    List<MediaStreamInfo.Flags> types = movie.getMediaInfoSubtitleTypeList();
    for (MediaStreamInfo.Flags sel : selectedItems) {
      if (types.contains(sel)) {
        return true;
      }
    }

    return false;
  }
}
