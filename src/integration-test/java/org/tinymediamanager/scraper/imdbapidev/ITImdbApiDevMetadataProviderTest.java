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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.tinymediamanager.core.entities.Person.Type.ACTOR;
import static org.tinymediamanager.core.entities.Person.Type.DIRECTOR;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.tinymediamanager.core.BasicITest;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviders;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;

public class ITImdbApiDevMetadataProviderTest extends BasicITest {

  @Before
  public void setup() throws Exception {
    super.setup();
    MediaProviders.loadMediaProviders();
  }

  @Test
  public void testMovieSearch() {
    IMovieMetadataProvider mp = null;
    List<MediaSearchResult> results = null;

    /*
     * test on akas.imdb.com - "9"
     */
    try {
      mp = new ImdbApiDevMovieMetadataProvider();
      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setSearchQuery("9");
      options.setSearchYear(2016);
      options.setLanguage(MediaLanguages.en);
      options.setCertificationCountry(CountryCode.US);
      options.setReleaseDateCountry("US");

      results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertThat(results.size()).isGreaterThanOrEqualTo(15);

      // check if the result is found (9 - 2016 - tt5719388)
      checkSearchResult("9", 2016, "tt5719388", results);

      // check second result (9 - 2009 - tt0472033)
      checkSearchResult("9", 2009, "tt0472033", results);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    /*
     * test on www.imdb.com - "Inglorious Basterds"
     */
    try {
      mp = new ImdbApiDevMovieMetadataProvider();
      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setSearchQuery("Inglorious Basterds");
      options.setLanguage(MediaLanguages.en);
      options.setCertificationCountry(CountryCode.US);
      options.setReleaseDateCountry("US");

      results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertThat(results.size()).isGreaterThanOrEqualTo(1);

      // check first result (Inglourious Basterds - 2009 - tt0361748)
      checkSearchResult("Inglourious Basterds", 2009, "tt0361748", results);

      // check second result (The Real Inglorious Bastards - 2012 - tt3320110)
      checkSearchResult("The Real Inglorious Bastards", 2012, "tt3320110", results);

    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    /*
     * test on www.imdb.com - "Asterix der Gallier" in de
     */
    try {
      mp = new ImdbApiDevMovieMetadataProvider();
      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setSearchQuery("Asterix der Gallier");
      options.setLanguage(MediaLanguages.de);
      options.setCertificationCountry(CountryCode.DE);
      options.setReleaseDateCountry("DE");

      results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertThat(results.size()).isGreaterThanOrEqualTo(1);

      // check first result (Asterix der Gallier - 1967 - tt0061369)
      checkSearchResult("Asterix der Gallier", 1967, "tt0061369", results);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowSearch() {
    ITvShowMetadataProvider mp = null;
    List<MediaSearchResult> results = null;

    /*
     * test on akas.imdb.com - "Psych"
     */
    try {
      mp = new ImdbApiDevTvShowMetadataProvider();
      TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
      options.setSearchQuery("Psych");
      options.setLanguage(MediaLanguages.de);
      options.setCertificationCountry(CountryCode.US);
      options.setReleaseDateCountry("US");

      results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertThat(results.size()).isGreaterThanOrEqualTo(1);

      // check first result (Psych - 2006 - tt0491738)
      checkSearchResult("Psych", 2006, "tt0491738", results);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodeListing() {
    ITvShowMetadataProvider mp = null;
    List<MediaMetadata> episodes = null;

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    try {
      mp = new ImdbApiDevTvShowMetadataProvider();
      TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
      options.setId(MediaMetadata.IMDB, "tt0491738");
      options.setLanguage(MediaLanguages.en);
      options.setCertificationCountry(CountryCode.US);
      options.setReleaseDateCountry("US");

      episodes = mp.getEpisodeList(options);

      // did we get a result?
      assertNotNull("Episodes", episodes);

      // result count
      assertThat(episodes.size()).isGreaterThanOrEqualTo(120);

    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowScrape() {
    ITvShowMetadataProvider mp = null;
    TvShowSearchAndScrapeOptions options = null;
    MediaMetadata md = null;

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    try {
      mp = new ImdbApiDevTvShowMetadataProvider();
      options = new TvShowSearchAndScrapeOptions();
      options.setImdbId("tt0491738");
      options.setLanguage(MediaLanguages.en);
      options.setCertificationCountry(CountryCode.US);
      options.setReleaseDateCountry("US");

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Psych", md.getTitle());
      assertThat(md.getPlot()).containsIgnoringCase("police");
      assertThat(md.getCastMembers(DIRECTOR)).isNotEmpty();
      assertThat(md.getCastMembers(ACTOR)).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    /*
     * test on akas.imdb.com - Firefly (tt0303461)
     */
    try {
      mp = new ImdbApiDevTvShowMetadataProvider();
      options = new TvShowSearchAndScrapeOptions();
      options.setImdbId("tt0303461");
      options.setLanguage(MediaLanguages.de);
      options.setCertificationCountry(CountryCode.DE);
      options.setReleaseDateCountry("DE");

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertThat(md.getTitle()).contains("Firefly");
      assertEquals("Firefly", md.getOriginalTitle());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodeScrape() {
    ITvShowMetadataProvider mp = null;
    TvShowEpisodeSearchAndScrapeOptions options = null;
    MediaMetadata md = null;
    SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    // S1E1
    try {
      mp = new ImdbApiDevTvShowMetadataProvider();
      options = new TvShowEpisodeSearchAndScrapeOptions();
      options.setTvShowIds(Collections.singletonMap(MediaMetadata.IMDB, "tt0491738"));
      TvShowModuleManager.getInstance().getSettings().setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      options.setId(MediaMetadata.SEASON_NR, "1");
      options.setId(MediaMetadata.EPISODE_NR, "1");
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Pilot", md.getTitle());
      assertThat(md.getPlot()).contains("police", "Santa Barbara");
      assertEquals("7 July 2006", sdf.format(md.getReleaseDate()));
      assertThat(md.getCastMembers(ACTOR)).isNotEmpty();
      assertThat(md.getCastMembers(DIRECTOR)).isNotEmpty();
      assertEquals("Michael Engler", md.getCastMembers(DIRECTOR).get(0).getName());
      assertThat(md.getCastMembers(WRITER)).isNotEmpty();
      assertEquals("Steve Franks", md.getCastMembers(WRITER).get(0).getName());

      assertThat(md.getRatings()).isNotEmpty();
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getId()).isNotEmpty();
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVotes()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);

      assertThat(md.getIds()).containsKeys(MediaMetadata.IMDB);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    // S3E12
    try {
      mp = new ImdbApiDevTvShowMetadataProvider();
      options = new TvShowEpisodeSearchAndScrapeOptions();
      options.setTvShowIds(Collections.singletonMap(MediaMetadata.IMDB, "tt0491738"));
      TvShowModuleManager.getInstance().getSettings().setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      options.setId(MediaMetadata.SEASON_NR, "3");
      options.setId(MediaMetadata.EPISODE_NR, "12");
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Earth, Wind and... Wait for It", md.getTitle());
      assertThat(md.getPlot()).isNotEmpty();
      assertEquals("23 January 2009", sdf.format(md.getReleaseDate()));

      assertThat(md.getRatings()).isNotEmpty();
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getId()).isNotEmpty();
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVotes()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);

      assertThat(md.getIds()).containsKeys(MediaMetadata.IMDB);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private void checkSearchResult(String title, int year, String imdbId, List<MediaSearchResult> results) {
    boolean found = true;

    for (MediaSearchResult result : results) {
      if (result.getTitle().equalsIgnoreCase(title) && result.getYear() == year && result.getIMDBId().equalsIgnoreCase(imdbId)) {
        found = true;
        break;
      }
    }

    assertThat(found).isEqualTo(true);
  }

  @Test
  public void testMovieScrape() {
    IMovieMetadataProvider mp = null;
    MovieSearchAndScrapeOptions options = null;
    MediaMetadata md = null;

    /*
     * scrape www.imdb.com - 9 - tt0472033
     */
    try {
      mp = new ImdbApiDevMovieMetadataProvider();
      options = new MovieSearchAndScrapeOptions();
      options.setImdbId("tt0472033");
      options.setLanguage(MediaLanguages.en);
      options.setCertificationCountry(CountryCode.US);
      options.setReleaseDateCountry("US");

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("9", 2009, "9", 7.0, 63365, "", 79, "Shane Acker", "Pamela Pettler, Shane Acker, Ben Gluck", "PG-13", "09-09-2009", md);

      assertThat(md.getGenres().size()).isGreaterThan(0);
      assertThat(md.getPlot()).isNotEmpty();
      checkCastMembers(md);
      checkMoviePoster(md);
      checkProductionCompany(md);

      // check localized values
      assertThat(md.getCountries()).contains("US");
      assertThat(md.getSpokenLanguages()).containsOnly("eng");
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    /*
     * scrape www.imdb.com - 12 Monkeys - tt0114746
     */
    try {
      mp = new ImdbApiDevMovieMetadataProvider();
      options = new MovieSearchAndScrapeOptions();
      options.setImdbId("tt0114746");
      options.setLanguage(MediaLanguages.de);
      options.setCertificationCountry(CountryCode.DE);
      options.setReleaseDateCountry("DE");

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("12 Monkeys", 1995, "Twelve Monkeys", 8.1, 262821, "", 129, "Terry Gilliam",
          "Chris Marker, David Webb Peoples, Janet Peoples", "16", "01-02-1996", md);

      assertThat(md.getGenres()).isNotEmpty();
      assertThat(md.getPlot()).containsIgnoringCase("Virus");
      checkCastMembers(md);
      checkProductionCompany(md);
      checkMoviePoster(md);

      // check localized values
      assertThat(md.getCountries()).contains("US");
      assertThat(md.getSpokenLanguages()).contains("eng", "fra");
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    /*
     * scrape www.imdb.com - Brave - tt1217209
     */
    try {
      mp = new ImdbApiDevMovieMetadataProvider();
      options = new MovieSearchAndScrapeOptions();
      options.setImdbId("tt1217209");
      options.setLanguage(MediaLanguages.en);
      options.setCertificationCountry(CountryCode.GB);
      options.setReleaseDateCountry("GB");

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("Brave", 2012, "Brave", 7.3, 52871, "", 93, "Mark Andrews, Brenda Chapman, Steve Purcell",
          "Mark Andrews, Steve Purcell, Brenda Chapman, Irene Mecchi", "PG", "02-08-2012", md);

      assertThat(md.getGenres()).isNotEmpty();
      assertThat(md.getPlot()).contains("Merida");
      checkCastMembers(md);
      checkProductionCompany(md);
      checkMoviePoster(md);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private void checkMovieDetails(String title, int year, String originalTitle, double rating, int voteCount, String tagline, int runtime,
      String director, String writer, String certification, String releaseDate, MediaMetadata md) {
    // title
    assertEquals("title ", title, md.getTitle());
    // year
    assertEquals("year", year, md.getYear());
    // original title
    assertEquals("originalTitle", originalTitle, md.getOriginalTitle());
    // rating
    assertThat(md.getRatings().size()).isGreaterThanOrEqualTo(1);
    MediaRating mediaRating = md.getRatings().get(0);

    assertEquals("rating", rating, mediaRating.getRating(), 0.5);
    // count (only check if parsed count count is smaller than the given votecount)
    if (voteCount > mediaRating.getVotes()) {
      assertEquals("count", voteCount, (int) mediaRating.getVotes());
    }
    // tagline
    assertEquals("tagline", tagline, md.getTagline());
    // runtime
    assertEquals("runtime", runtime, (int) md.getRuntime());
    // director
    StringBuilder sb = new StringBuilder();
    for (Person cm : md.getCastMembers(DIRECTOR)) {
      if (StringUtils.isNotEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(cm.getName());
    }
    assertEquals("director", director, sb.toString());
    // writer
    sb = new StringBuilder();
    for (Person cm : md.getCastMembers(WRITER)) {
      if (StringUtils.isNotEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(cm.getName());
    }
    assertEquals("writer", writer, sb.toString());

    // date can differ depending on the IP address
    assertNotEquals("release date", null, md.getReleaseDate());
    // certification
    // assertEquals("certification",
    // Certification.getCertification(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry(),
    // certification), md
    // .getCertifications().get(0));
  }

  private void checkMoviePoster(MediaMetadata md) {
    assertThat(md.getMediaArt()).isNotEmpty();
    assertThat(md.getMediaArt().get(0).getImageSizes()).isNotEmpty();
  }

  private void checkCastMembers(MediaMetadata md) {
    assertThat(md.getCastMembers(ACTOR)).isNotEmpty();
    // at least the must have a name/role
    for (Person member : md.getCastMembers(ACTOR)) {
      assertThat(member.getName()).isNotEmpty();
      assertThat(member.getRole()).isNotEmpty();
    }
  }

  private void checkProductionCompany(MediaMetadata md) {
    assertThat(md.getProductionCompanies()).isNotEmpty();
    assertThat(md.getProductionCompanies().get(0)).isNotEmpty();
  }
}
