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

import static org.tinymediamanager.scraper.entities.MediaEpisodeGroup.DEFAULT_AIRED;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaEpisodeGroup;
import org.tinymediamanager.scraper.entities.MediaEpisodeNumber;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevEpisode;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevListTitleEpisodesResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevSearchTitlesResponse;
import org.tinymediamanager.scraper.imdbapidev.entities.ImdbApiDevTitle;
import org.tinymediamanager.scraper.interfaces.ITvShowImdbMetadataProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MediaIdUtil;
import org.tinymediamanager.scraper.util.MetadataUtil;

/**
 * The class {@link ImdbApiDevTvShowMetadataProvider} provides TV show and episode metadata from the imdbapi.dev service.
 * <p>
 * It supports searching by title or IMDb ID, scraping full TV show metadata and fetching the full episode list with season/episode numbers, plot and
 * ratings for each episode.
 * </p>
 *
 * @author Manuel Laggner
 */
public class ImdbApiDevTvShowMetadataProvider extends ImdbApiDevMetadataProvider implements ITvShowMetadataProvider, ITvShowImdbMetadataProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImdbApiDevTvShowMetadataProvider.class);

  @Override
  protected String getSubId() {
    return "tvshow";
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * Search for TV shows by title or IMDb ID.
   * <p>
   * If the search string is a valid IMDb ID, a direct title lookup is performed. Otherwise the /search/titles endpoint is queried and results are
   * filtered to TV show types only.
   * </p>
   *
   * @param options
   *          the search options
   * @return a sorted set of search results
   * @throws ScrapeException
   *           if the API call fails
   */
  @Override
  public SortedSet<MediaSearchResult> search(@NotNull TvShowSearchAndScrapeOptions options) throws ScrapeException {
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
        if (title != null && isTvShowType(title.type)) {
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
            if (isTvShowType(title.type)) {
              MediaSearchResult sr = morphTitleToSearchResult(title);
              sr.calculateScore(options);
              results.add(sr);
            }
          }
          LOGGER.debug("found {} TV show results", results.size());
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
   * Fetch full metadata for a TV show.
   * <p>
   * Requires an IMDb ID to be present in the scrape options. After fetching the base title data, full credits and certifications are also fetched.
   * The end year is also set if available.
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
  public MediaMetadata getMetadata(@NotNull TvShowSearchAndScrapeOptions options) throws ScrapeException {
    LOGGER.debug("getMetadata(TvShowSearchAndScrapeOptions): {}", options);

    // lazy initialization
    initAPI();

    // resolve the IMDb ID to use
    String imdbId = options.getImdbId();
    if (!MediaIdUtil.isValidImdbId(imdbId)) {
      if (options.getSearchResult() != null) {
        imdbId = options.getSearchResult().getIMDBId();
      }
    }

    if (!MediaIdUtil.isValidImdbId(imdbId)) {
      LOGGER.debug("no IMDb ID found - cannot scrape");
      throw new MissingIdException(MediaMetadata.IMDB);
    }

    LOGGER.debug("scraping TV show with IMDb ID: {}", imdbId);

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

    // add the default aired episode group
    md.addEpisodeGroup(MediaEpisodeGroup.DEFAULT_AIRED);

    // fetch certifications
    String certCountry = options.getCertificationCountry() != null ? options.getCertificationCountry().getAlpha2() : "US";
    fetchAndMergeCertifications(md, imdbId, certCountry);

    return md;
  }

  /**
   * Fetch metadata for a single TV episode.
   * <p>
   * Resolves the episode by fetching the full episode list for the TV show and matching by season/episode number. Requires the TV show's IMDb ID and
   * the episode's season+episode number to be set in the scrape options. Credits are fetched lazily only for the matched episode.
   * </p>
   *
   * @param options
   *          the scrape options containing the TV show IMDb ID and episode/season numbers
   * @return the scraped episode {@link MediaMetadata}
   * @throws ScrapeException
   *           if the API call fails
   * @throws MissingIdException
   *           if no usable IMDb ID is found
   * @throws NothingFoundException
   *           if the episode is not found
   */
  @Override
  public MediaMetadata getMetadata(@NotNull TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException {
    LOGGER.debug("getMetadata(TvShowEpisodeSearchAndScrapeOptions): {}", options);

    // lazy initialization
    initAPI();

    // get the TV show IMDb ID from the parent TV show
    String tvShowImdbId = (String) options.getTvShowIds().get(MediaMetadata.IMDB);
    if (!MediaIdUtil.isValidImdbId(tvShowImdbId)) {
      LOGGER.debug("no TV show IMDb ID found - cannot scrape episode");
      throw new MissingIdException(MediaMetadata.IMDB);
    }

    // get the desired S/E number
    MediaEpisodeGroup episodeGroup = options.getEpisodeGroup();

    int seasonNr = options.getIdAsIntOrDefault("seasonNr", -1);
    int episodeNr = options.getIdAsIntOrDefault("episodeNr", -1);

    if (seasonNr < 0 || episodeNr < 0) {
      LOGGER.debug("no season/episode number found");
      throw new MissingIdException("season", "episode");
    }

    // fetch the episode list and find the matching episode
    List<MediaMetadata> episodes = getEpisodeList(options.createTvShowSearchAndScrapeOptions());

    for (MediaMetadata episode : episodes) {
      MediaEpisodeNumber epNumber = episode.getEpisodeNumber(episodeGroup);
      if (epNumber == null) {
        epNumber = episode.getEpisodeNumber(DEFAULT_AIRED);
      }
      if (epNumber != null && epNumber.season() == seasonNr && epNumber.episode() == episodeNr) {
        Object episodeImdbId = episode.getId(MediaMetadata.IMDB);
        if (episodeImdbId instanceof String imdbEpisodeId && MediaIdUtil.isValidImdbId(imdbEpisodeId)) {
          fetchAndMergeCredits(episode, imdbEpisodeId);
        }
        episode.setScrapeOptions(options);
        return episode;
      }
    }

    LOGGER.debug("episode S{}E{} not found for show {}", seasonNr, episodeNr, tvShowImdbId);
    throw new NothingFoundException();
  }

  /**
   * Fetch the full episode list for a TV show.
   * <p>
   * Fetches all episodes in one paginated flow from the episodes endpoint. Each episode is mapped to a {@link MediaMetadata} object with
   * season/episode number, title, plot, rating and air date set.
   * </p>
   *
   * @param options
   *          the scrape options containing the TV show IMDb ID
   * @return a list of episode {@link MediaMetadata}
   * @throws ScrapeException
   *           if the API call fails
   */
  @Override
  public List<MediaMetadata> getEpisodeList(@NotNull TvShowSearchAndScrapeOptions options) throws ScrapeException {
    LOGGER.debug("getEpisodeList(): {}", options);

    // lazy initialization
    initAPI();

    // get the TV show IMDb ID
    String imdbId = options.getImdbId();
    if (!MediaIdUtil.isValidImdbId(imdbId)) {
      LOGGER.debug("no IMDb ID found - cannot fetch episode list");
      throw new MissingIdException(MediaMetadata.IMDB);
    }

    List<MediaMetadata> episodes = new ArrayList<>();

    try {
      String pageToken = null;
      do {
        ImdbApiDevListTitleEpisodesResponse episodesResponse = api.titleService().getEpisodes(imdbId, 50, pageToken).execute().body();

        if (episodesResponse == null || ListUtils.isEmpty(episodesResponse.episodes)) {
          break;
        }

        for (ImdbApiDevEpisode ep : episodesResponse.episodes) {
          MediaMetadata episodeMd = morphEpisodeToMetadata(ep);
          if (episodeMd != null) {
            episodes.add(episodeMd);
          }
        }

        pageToken = episodesResponse.nextPageToken;
      } while (StringUtils.isNotBlank(pageToken));
    }
    catch (Exception e) {
      LOGGER.debug("could not fetch episodes for {}: {}", imdbId, e.getMessage());
    }

    LOGGER.debug("fetched {} episodes for {}", episodes.size(), imdbId);
    return episodes;
  }

  /**
   * Maps an {@link ImdbApiDevEpisode} to a tinyMediaManager {@link MediaMetadata} episode object.
   * <p>
   * </p>
   *
   * @param ep
   *          the API episode object
   * @return the mapped {@link MediaMetadata}, or {@code null} if the episode has no valid numbers
   */
  private MediaMetadata morphEpisodeToMetadata(ImdbApiDevEpisode ep) {
    if (ep == null) {
      return null;
    }

    // we need at least a season and episode number
    int seasonNr = -1;
    int episodeNr = MetadataUtil.unboxInteger(ep.episodeNumber, -1);

    if (StringUtils.isNotBlank(ep.season)) {
      try {
        seasonNr = Integer.parseInt(ep.season);
      }
      catch (NumberFormatException ignored) {
        LOGGER.trace("could not parse season number: {}", ep.season);
      }
    }

    if (seasonNr < 0 || episodeNr < 0) {
      return null;
    }

    MediaMetadata md = new MediaMetadata(getProviderInfo().getId());

    // set episode number using the AIRED episode group
    md.setEpisodeNumber(DEFAULT_AIRED, seasonNr, episodeNr);

    // set IMDb ID
    if (StringUtils.isNotBlank(ep.id)) {
      md.setId(MediaMetadata.IMDB, ep.id);
    }

    // set title
    if (StringUtils.isNotBlank(ep.title)) {
      md.setTitle(ep.title);
    }

    // set plot
    if (StringUtils.isNotBlank(ep.plot)) {
      md.setPlot(ep.plot);
    }

    // set runtime (convert seconds to minutes)
    if (ep.runtimeSeconds != null && ep.runtimeSeconds > 0) {
      md.setRuntime(ep.runtimeSeconds / 60);
    }

    // set rating
    if (ep.rating != null && ep.rating.aggregateRating != null && ep.rating.aggregateRating > 0) {
      org.tinymediamanager.core.entities.MediaRating rating = new org.tinymediamanager.core.entities.MediaRating(MediaMetadata.IMDB);
      rating.setRating(ep.rating.aggregateRating);
      rating.setMaxValue(10);
      if (ep.rating.voteCount != null) {
        rating.setVotes(ep.rating.voteCount);
      }
      md.addRating(rating);
    }

    // set air date
    if (ep.releaseDate != null && ep.releaseDate.year != null && ep.releaseDate.year > 0) {
      try {
        Calendar cal = Calendar.getInstance();
        // Calendar.JANUARY is 0, so subtract 1 from month
        int month = ep.releaseDate.month != null ? ep.releaseDate.month - 1 : 0;
        int day = ep.releaseDate.day != null ? ep.releaseDate.day : 1;
        cal.set(ep.releaseDate.year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        md.setReleaseDate(cal.getTime());
      }
      catch (Exception e) {
        LOGGER.trace("could not create date for episode: {}", e.getMessage());
      }
    }

    // set thumbnail from episode primary image
    if (ep.primaryImage != null && StringUtils.isNotBlank(ep.primaryImage.url)) {
      MediaArtwork thumb = new MediaArtwork(ID, MediaArtwork.MediaArtworkType.THUMB);
      thumb.setOriginalUrl(ep.primaryImage.url);
      thumb.setPreviewUrl(ep.primaryImage.url);
      md.addMediaArt(thumb);
    }

    return md;
  }

  /**
   * Checks whether the given title type string represents a TV show type.
   *
   * @param type
   *          the type string from the API (e.g. "TV_SERIES", "MOVIE")
   * @return {@code true} if this is a TV show type
   */
  private boolean isTvShowType(String type) {
    if (StringUtils.isBlank(type)) {
      return false;
    }
    return switch (normalizeTitleType(type)) {
      case "TVSERIES", "TVMINISERIES", "TVSPECIAL" -> true;
      default -> false;
    };
  }

  /**
   * Normalizes external title types to a comparable internal representation.
   * <p>
   * imdbapi.dev may return values like {@code TV_SERIES}, {@code tvSeries}, or {@code tv_series}. This helper removes non-alphanumeric characters and
   * upper-cases the value so type checks stay robust.
   * </p>
   *
   * @param type
   *          the raw type string from the API
   * @return the normalized type string
   */
  private String normalizeTitleType(String type) {
    return type.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
  }

  /**
   * Converts an {@link ImdbApiDevTitle} to a {@link MediaSearchResult}.
   *
   * @param title
   *          the title data from the API
   * @return a new {@link MediaSearchResult}
   */
  private MediaSearchResult morphTitleToSearchResult(ImdbApiDevTitle title) {
    MediaSearchResult sr = new MediaSearchResult(getProviderInfo().getId(), MediaType.TV_SHOW);

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
