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

import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevListTitleCertificatesResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevListTitleCompanyCreditsResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevListTitleCreditsResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevListTitleEpisodesResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevListTitleSeasonsResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevSearchTitlesResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevTitle;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevTitleReleaseDatesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * The interface {@link ImdbApiDevTitleService} defines all Retrofit service calls for title-related endpoints of the imdbapi.dev API.
 *
 * @author Manuel Laggner
 */
interface ImdbApiDevTitleService {

  /**
   * Search for titles by query string.
   *
   * @param query
   *          the search query
   * @param limit
   *          the maximum number of results to return (max 50)
   * @return a {@link Call} containing the search results
   */
  @GET("search/titles")
  Call<ImdbApiDevSearchTitlesResponse> searchTitles(@Query("query") String query, @Query("limit") int limit);

  /**
   * Get full title details by IMDb ID.
   *
   * @param titleId
   *          the IMDb title ID (e.g. "tt1234567")
   * @return a {@link Call} containing the title details
   */
  @GET("titles/{titleId}")
  Call<ImdbApiDevTitle> getTitle(@Path("titleId") String titleId);

  /**
   * Get credits (cast and crew) for a title.
   *
   * @param titleId
   *          the IMDb title ID (e.g. "tt1234567")
   * @param pageSize
   *          the maximum number of credits to return per page (max 50)
   * @param pageToken
   *          optional token for pagination
   * @return a {@link Call} containing the credits response
   */
  @GET("titles/{titleId}/credits")
  Call<ImdbApiDevListTitleCreditsResponse> getCredits(@Path("titleId") String titleId, @Query("pageSize") int pageSize,
      @Query("pageToken") String pageToken);

  /**
   * Get all seasons for a TV series.
   *
   * @param titleId
   *          the IMDb title ID (e.g. "tt1234567")
   * @return a {@link Call} containing the seasons response
   */
  @GET("titles/{titleId}/seasons")
  Call<ImdbApiDevListTitleSeasonsResponse> getSeasons(@Path("titleId") String titleId);

  /**
   * Get all episodes for a title.
   * <p>
   * The endpoint returns episodes from all seasons and supports pagination.
   * </p>
   *
   * @param titleId
   *          the IMDb title ID (e.g. "tt1234567")
   * @param pageSize
   *          the maximum number of episodes to return per page (max 50)
   * @param pageToken
   *          optional token for pagination
   * @return a {@link Call} containing the episodes response
   */
  @GET("titles/{titleId}/episodes")
  Call<ImdbApiDevListTitleEpisodesResponse> getEpisodes(@Path("titleId") String titleId, @Query("pageSize") int pageSize,
      @Query("pageToken") String pageToken);

  /**
   * Get content rating certificates for a title.
   *
   * @param titleId
   *          the IMDb title ID (e.g. "tt1234567")
   * @return a {@link Call} containing the certificates response
   */
  @GET("titles/{titleId}/certificates")
  Call<ImdbApiDevListTitleCertificatesResponse> getCertificates(@Path("titleId") String titleId);

  /**
   * Get release information for a title.
   *
   * @param titleId
   *          the IMDb title ID (e.g. "tt1234567")
   * @return a {@link Call} containing the release information response
   */
  @GET("titles/{titleId}/releaseDates")
  Call<ImdbApiDevTitleReleaseDatesResponse> getReleaseDates(@Path("titleId") String titleId);

  /**
   * Get company credits for a title.
   *
   * @param titleId
   *          the IMDb title ID (e.g. "tt1234567")
   * @param categories
   *          the categories filter (e.g. "production")
   * @param pageSize
   *          the maximum number of companies to return per page
   * @param pageToken
   *          optional token for pagination
   * @return a {@link Call} containing the company credits response
   */
  @GET("titles/{titleId}/companyCredits")
  Call<ImdbApiDevListTitleCompanyCreditsResponse> getCompanyCredits(@Path("titleId") String titleId, @Query("categories") String categories,
      @Query("pageSize") int pageSize, @Query("pageToken") String pageToken);
}
