/*
 * Copyright 2012 - 2024 Manuel Laggner
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
package org.tinymediamanager.scraper.subdl;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.tinymediamanager.core.FeatureNotEnabledException;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.exceptions.HttpException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.scraper.subdl.model.SubdlModel;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * The base class for the Subdl scraper
 * 
 * @author Wolfgang Janes
 */
abstract class SubdlSubtitleProvider implements IMediaProvider {
  public static final String    ID          = "subdl";
  public static final String    API_KEY     = "apiKey";
  protected static final String BASE_URL_DL = "https://dl.subdl.com";

  protected SubdlController     controller  = null;

  protected abstract String getSubId();

  protected abstract Logger getLogger();

  protected final MediaProviderInfo providerInfo;

  SubdlSubtitleProvider() {
    providerInfo = createMediaProviderInfo();
  }

  protected MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo info = new MediaProviderInfo(ID, getSubId(), "Subdl", "<html><h3>Subdl.com</h3><br />A subtitle scraper for Subdl.com</html>",
        SubdlSubtitleProvider.class.getResource("/org/tinymediamanager/scraper/subdl.png"));
    info.getConfig().addText(API_KEY, "", true);
    info.getConfig().load();
    return info;
  }

  @Override
  public boolean isActive() {
    return isFeatureEnabled() && StringUtils.isNotBlank(getUserApiKey()) && isApiKeyAvailable(null);
  }

  // thread safe initialization of the API
  protected void initAPI() throws ScrapeException {
    if (controller == null) {
      if (!isActive()) {
        throw new ScrapeException(new FeatureNotEnabledException(this));
      }
      controller = new SubdlController(getApiKey());
      controller.setUserApiKey(getUserApiKey());
    }
  }

  protected @Nullable SubdlModel processResponse(Response<SubdlModel> response) throws IOException {
    SubdlModel searchResult;

    if (!response.isSuccessful()) {
      String message = response.message();
      try (ResponseBody body = response.errorBody()) {
        message = body.string();
      }
      catch (Exception e) {
        // ignore
      }

      throw new HttpException(response.code(), message);
    }
    searchResult = response.body();
    return searchResult;
  }

  protected String getUserApiKey() {
    return providerInfo.getConfig().getValue(SubdlSubtitleProvider.API_KEY);
  }
}
