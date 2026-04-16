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

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevSearchTitlesResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevTitle;
import org.tinymediamanager.scraper.interfaces.IMovieImdbMetadataProvider;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MediaIdUtil;
import org.tinymediamanager.scraper.util.MetadataUtil;

/**
 * The class {@link ImdbApiDevMovieMetadataProvider} provides movie metadata from the imdbapi.dev service.
 * <p>
 * It supports searching by title or IMDb ID and scraping full metadata for movies including cast/crew, ratings, certifications, and poster artwork.
 * </p>
 *
 * @author Manuel Laggner
 */
public final class ImdbApiDevMovieMetadataProvider extends ImdbApiDevMetadataProvider implements IMovieMetadataProvider, IMovieImdbMetadataProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImdbApiDevMovieMetadataProvider.class);

  @Override
  protected String getSubId() {
    return "movie";
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * Search for movies by title or IMDb ID.
   * <p>
   * If the search string is a valid IMDb ID, a direct title lookup is performed. Otherwise the /search/titles endpoint is queried and results are
   * filtered to movies only.
   * </p>
   *
   * @param options
   *          the search options
   * @return a sorted set of search results
   * @throws ScrapeException
   *           if the API call fails
   */
  @Override
  public SortedSet<MediaSearchResult> search(@NotNull MovieSearchAndScrapeOptions options) throws ScrapeException {
    LOGGER.debug("search(): {}", options);

    // lazy initialization
    initAPI();

    SortedSet<MediaSearchResult> results = new TreeSet<>();

    String imdbId = options.getImdbId();
    String searchString = options.getSearchQuery();

    if (StringUtils.isBlank(searchString) && !MediaIdUtil.isValidImdbId(imdbId)) {
      LOGGER.debug("cannot search without a search string or IMDb ID");
      return results;
    }

    searchString = MetadataUtil.removeNonSearchCharacters(searchString);

    // if the search string itself is an IMDb ID, use it directly
    if (MediaIdUtil.isValidImdbId(searchString)) {
      imdbId = searchString;
    }

    Exception savedException = null;

    // 1. direct lookup via IMDb ID
    if (MediaIdUtil.isValidImdbId(imdbId)) {
      LOGGER.debug("searching with IMDb ID: {}", imdbId);
      try {
        ImdbApiDevTitle title = api.titleService().getTitle(imdbId).execute().body();
        if (title != null && isMovieType(title.type)) {
          results.add(morphTitleToSearchResult(title));
          LOGGER.debug("found result via IMDb ID lookup");
        }
      }
      catch (Exception e) {
        LOGGER.debug("error searching by IMDb ID: {}", e.getMessage());
        savedException = e;
      }
    }

    // 2. search by title string
    if (results.isEmpty() && StringUtils.isNotBlank(searchString)) {
      LOGGER.debug("searching with query: '{}'", searchString);
      try {
        ImdbApiDevSearchTitlesResponse response = api.titleService().searchTitles(searchString, 50).execute().body();
        if (response != null && ListUtils.isNotEmpty(response.titles)) {
          for (ImdbApiDevTitle title : response.titles) {
            if (isMovieType(title.type)) {
              MediaSearchResult sr = morphTitleToSearchResult(title);
              sr.calculateScore(options);
              results.add(sr);
            }
          }
          LOGGER.debug("found {} movie results", results.size());
        }
      }
      catch (Exception e) {
        LOGGER.debug("error searching: {}", e.getMessage());
        savedException = e;
      }
    }

    if (results.isEmpty() && savedException != null) {
      throw new ScrapeException(savedException);
    }

    return results;
  }

  /**
   * Fetch full metadata for a movie.
   * <p>
   * Requires an IMDb ID to be present in the scrape options. After fetching the base title data, full credits and certifications are also fetched.
   * </p>
   *
   * @param options
   *          the scrape options containing the IMDb ID
   * @return the scraped {@link MediaMetadata}
   * @throws ScrapeException
   *           if the API call fails
   * @throws MissingIdException
   *           if no usable IMDb ID is found
   * @throws NothingFoundException
   *           if no matching title is found
   */
  @Override
  public MediaMetadata getMetadata(@NotNull MovieSearchAndScrapeOptions options) throws ScrapeException {
    LOGGER.debug("getMetadata(): {}", options);

    // return cached metadata from search result if available
    if (options.getMetadata() != null && getId().equals(options.getMetadata().getProviderId())) {
      LOGGER.debug("returning cached metadata");
      return options.getMetadata();
    }

    // lazy initialization
    initAPI();

    // resolve the IMDb ID to use
    String imdbId = options.getImdbId();
    if (!MediaIdUtil.isValidImdbId(imdbId)) {
      // try to get the imdb id from the result
      if (options.getSearchResult() != null) {
        imdbId = options.getSearchResult().getIMDBId();
      }
    }

    if (!MediaIdUtil.isValidImdbId(imdbId)) {
      LOGGER.debug("no IMDb ID found - cannot scrape");
      throw new MissingIdException(MediaMetadata.IMDB);
    }

    LOGGER.debug("scraping movie with IMDb ID: {}", imdbId);

    ImdbApiDevTitle title = null;
    Exception savedException = null;

    try {
      title = api.titleService().getTitle(imdbId).execute().body();
    }
    catch (Exception e) {
      LOGGER.debug("error fetching title data: {}", e.getMessage());
      savedException = e;
    }

    if (title == null && savedException != null) {
      throw new ScrapeException(savedException);
    }

    if (title == null) {
      LOGGER.debug("no result found for IMDb ID: {}", imdbId);
      throw new NothingFoundException();
    }

    MediaMetadata md = new MediaMetadata(getProviderInfo().getId());
    md.setScrapeOptions(options);

    fillMetadataFromTitle(md, title);

    // fetch release info and set main/lowest release date
    fetchAndMergeReleaseDate(md, imdbId, options.getReleaseDateCountry());

    // fetch production companies from company credits
    fetchAndMergeProductionCompanies(md, imdbId);

    // fetch full credits (replaces cast from title summary)
    fetchAndMergeCredits(md, imdbId);

    // fetch certifications - use the certification country from options
    String certCountry = options.getCertificationCountry() != null ? options.getCertificationCountry().getAlpha2() : "US";
    fetchAndMergeCertifications(md, imdbId, certCountry);

    return md;
  }

  /**
   * Checks whether the given title type string represents a movie type.
   *
   * @param type
   *          the type string from the API (e.g. "MOVIE", "TV_SERIES")
   * @return {@code true} if this is a movie type
   */
  private boolean isMovieType(String type) {
    if (StringUtils.isBlank(type)) {
      return false;
    }
    return switch (type.toUpperCase()) {
      case "MOVIE", "TV_MOVIE", "SHORT", "VIDEO" -> true;
      default -> false;
    };
  }

  /**
   * Converts an {@link ImdbApiDevTitle} to a {@link MediaSearchResult}.
   *
   * @param title
   *          the title data from the API
   * @return a new {@link MediaSearchResult}
   */
  private MediaSearchResult morphTitleToSearchResult(ImdbApiDevTitle title) {
    MediaSearchResult sr = new MediaSearchResult(getProviderInfo().getId(), MediaType.MOVIE);

    if (StringUtils.isNotBlank(title.id)) {
      sr.setIMDBId(title.id);
    }
    if (StringUtils.isNotBlank(title.primaryTitle)) {
      sr.setTitle(title.primaryTitle);
    }
    if (title.startYear != null && title.startYear > 0) {
      sr.setYear(title.startYear);
    }
    if (title.primaryImage != null && StringUtils.isNotBlank(title.primaryImage.url)) {
      sr.setPosterUrl(title.primaryImage.url);
    }

    sr.setScore(1f);
    return sr;
  }
}
