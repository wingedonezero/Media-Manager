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
package org.tinymediamanager.scraper.interfaces;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.scraper.MediaProviderInfo;

/**
 * The interface {@link IMediaProvider}. This is the root interface for all tinyMediaManager metadata provider interfaces You should not implement
 * this interface by a class, since plugin examination relies on the concrete interface
 *
 * @author Manuel Laggner
 * @since 1.0
 */
public interface IMediaProvider {
  /**
   * Gets general information about the metadata provider
   *
   * @return the provider info containing metadata of the provider
   */
  MediaProviderInfo getProviderInfo();

  /**
   * Gets the user-configured API key for this scraper. Replaces the former license-provided key: scrapers now run entirely on the user's own keys.
   *
   * @return the configured API key, or an empty string if none is set
   */
  default String getApiKey() {
    return getProviderInfo().getUserApiKey();
  }

  /**
   * Checks whether an API key is configured/available for this scraper.
   *
   * @param key
   *          the key to check (typically {@link MediaProviderInfo#getUserApiKey()})
   * @return true if a non-blank key is present
   */
  default boolean isApiKeyAvailable(String key) {
    return StringUtils.isNotBlank(key);
  }

  /**
   * get the id from this scraper
   * 
   * @return the scraper id
   */
  default String getId() {
    return getProviderInfo().getId();
  }

  /**
   * indicates whether this scraper is active or not (private and valid API key OR public to be active)
   * 
   * @return true/false
   */
  boolean isActive();
}
