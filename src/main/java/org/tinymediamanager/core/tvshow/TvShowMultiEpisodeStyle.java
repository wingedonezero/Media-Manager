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
package org.tinymediamanager.core.tvshow;

import org.tinymediamanager.core.TmmResourceBundle;

/**
 * The enum {@link TvShowMultiEpisodeStyle} describes how multi-episode TV show files should be rendered by the renamer.
 * <p>
 * The repeat style keeps the current behavior and renders every episode number separately. The range style renders the first episode fully and
 * shortens the trailing episodes to a range when this is possible without losing information.
 * </p>
 *
 * @author Manuel Laggner
 */
public enum TvShowMultiEpisodeStyle {
  /** Repeat the full season/episode marker for every episode in the file. */
  REPEAT,
  /** Render contiguous, same-season episodes as a range. */
  RANGE;

  /**
   * Returns the localized display text for this style.
   *
   * @return the localized display text
   */
  @Override
  public String toString() {
    return TmmResourceBundle.getString("Settings.tvshow.renamer.multiepisodestyle." + name().toLowerCase());
  }
}
