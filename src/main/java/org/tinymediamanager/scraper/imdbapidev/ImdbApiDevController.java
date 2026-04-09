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
package org.tinymediamanager.scraper.imdbapidev;

import org.tinymediamanager.scraper.http.TmmHttpClient;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The class {@link ImdbApiDevController} manages the Retrofit REST adapter for the imdbapi.dev API.
 * <p>
 * The API base URL is {@code https://api.imdbapi.dev/}. No authentication is required.
 * </p>
 *
 * @author Manuel Laggner
 */
class ImdbApiDevController {

  private final String apikey;
  private Retrofit     restAdapter;

  /**
   * Creates a new controller.
   */
  ImdbApiDevController(String apikey) {
    this.apikey = apikey;
  }

  /**
   * Returns the lazily-initialized Retrofit instance, creating it on first access.
   *
   * @return the {@link Retrofit} instance
   */
  private Retrofit getRestAdapter() {
    if (restAdapter == null) {
      GsonBuilder gsonBuilder = new GsonBuilder();
      // allow lenient parsing for numbers
      gsonBuilder.registerTypeAdapter(Integer.class, (com.google.gson.JsonDeserializer<Integer>) (json, typeOfT, context) -> {
        try {
          return json.getAsInt();
        }
        catch (NumberFormatException e) {
          return 0;
        }
      });

      restAdapter = new Retrofit.Builder().baseUrl(apikey)
          .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
          .client(TmmHttpClient.newBuilder(true).build())
          .build();
    }
    return restAdapter;
  }

  /**
   * Get the title service for interacting with title-related API endpoints.
   *
   * @return the {@link ImdbApiDevTitleService}
   */
  ImdbApiDevTitleService titleService() {
    return getRestAdapter().create(ImdbApiDevTitleService.class);
  }
}
