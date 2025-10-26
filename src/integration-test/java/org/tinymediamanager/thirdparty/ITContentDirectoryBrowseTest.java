/*
 * Copyright 2012 - 2025 Manuel Laggner
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
package org.tinymediamanager.thirdparty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jupnp.support.contentdirectory.ContentDirectoryException;
import org.jupnp.support.model.BrowseFlag;
import org.jupnp.support.model.BrowseResult;
import org.jupnp.support.model.SortCriterion;
import org.tinymediamanager.core.BasicITest;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.thirdparty.upnp.ContentDirectoryService;

public class ITContentDirectoryBrowseTest extends BasicITest {

  private static final String                  KODI_FILTER = "dc:date,dc:description,upnp:longDescription,upnp:genre,res,res@duration,res@size,upnp:albumArtURI,upnp:rating,upnp:lastPlaybackPosition,upnp:lastPlaybackTime,upnp:playbackCount,upnp:originalTrackNumber,upnp:episodeNumber,upnp:programTitle,upnp:seriesTitle,upnp:album,upnp:artist,upnp:author,upnp:director,dc:publisher,searchable,childCount,dc:title,dc:creator,upnp:actor,res@resolution,upnp:episodeCount,upnp:episodeSeason,xbmc:dateadded,xbmc:rating,xbmc:votes,xbmc:artwork,xbmc:uniqueidentifier,xbmc:country,xbmc:userrating";
  private static final ContentDirectoryService CDS         = new ContentDirectoryService();
  // depends on where we run that
  private static final String                  ADVENTURE   = MediaGenres.ADVENTURE.getLocalizedName();

  @Before
  public void setup() throws Exception {
    super.setup();
    setTraceLogging();

    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();

    createFakeMovie("UPNPMovie3");
    createFakeMovie("UPNPMovie2");
    createFakeMovie("UPNPMovie1");
    createFakeMovie("AnotherMovie");

    createFakeShow("UPNPShow3");
    createFakeShow("UPNPShow2");
    createFakeShow("UPNPShow1");
    createFakeShow("AnotherShow");

    System.out.println("Used movie genres: " + MovieModuleManager.getInstance().getMovieList().getUsedGenres());
  }

  @After
  public void shutdown() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  private String getValidMovieID() {
    return MovieModuleManager.getInstance().getMovieList().getMovies().get(0).getDbId().toString();
  }

  private String getValidShowID() {
    return TvShowModuleManager.getInstance().getTvShowList().getTvShows().get(0).getDbId().toString();
  }

  @Test
  public void browseStructure() throws ContentDirectoryException {
    // ***** ROOT
    browse(1L, "0", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf("")); // 1 result / full meta
    browse(2L, "0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf("")); // list of needed meta
    browse(1L, "0", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf("")); // list of needed meta (filtered for 1 result)

    // ***** MOVIES
    browse(1L, "1", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    browse(3L, "1", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    browse(1L, "1", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));
    browse(2L, "1", BrowseFlag.DIRECT_CHILDREN, "*", 1, 0, SortCriterion.valueOf(""));

    browse(1L, "1/t", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    browse(4L, "1/t", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    browse(1L, "1/t", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    browse(1L, "1/t/" + getUUID("AnotherMovie"), BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    browse(0L, "1/t/" + getUUID("AnotherMovie"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    browse(0L, "1/t/" + getUUID("AnotherMovie"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    browse(1L, "1/g", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    Long usedMovieGenres = Long.valueOf(MovieModuleManager.getInstance().getMovieList().getUsedGenres().size());
    browse(usedMovieGenres, "1/g", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    browse(1L, "1/g", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    browse(1L, "1/g/" + ADVENTURE, BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    browse(4L, "1/g/" + ADVENTURE, BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    browse(1L, "1/g/" + ADVENTURE, BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    browse(1L, "1/g/" + ADVENTURE + "/" + getUUID("AnotherMovie"), BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    browse(0L, "1/g/" + ADVENTURE + "/" + getUUID("AnotherMovie"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    browse(0L, "1/g/" + ADVENTURE + "/" + getUUID("AnotherMovie"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    browse(0L, "1/g/invalid", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    browse(0L, "1/g/invalid", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    browse(0L, "1/g/invalid", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    // ***** TV SHOWS
    browse(1L, "2", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf(""));
    browse(4L, "2", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf(""));
    browse(1L, "2", BrowseFlag.DIRECT_CHILDREN, "*", 0, 1, SortCriterion.valueOf(""));

    browse(1L, "2/" + getUUID("UPNPShow3"), BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf("")); // show
    browse(1L, "2/" + getUUID("UPNPShow3") + "/1", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf("")); // episode
    browse(1L, "2/" + getUUID("UPNPShow3") + "/1/2", BrowseFlag.METADATA, "*", 0, 0, SortCriterion.valueOf("")); // season
    browse(1L, "2/" + getUUID("UPNPShow3"), BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf("")); // show
    browse(1L, "2/" + getUUID("UPNPShow3") + "/1", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf("")); // episode
    browse(0L, "2/" + getUUID("UPNPShow3") + "/1/2", BrowseFlag.DIRECT_CHILDREN, "*", 0, 0, SortCriterion.valueOf("")); // season
  }

  // =====================================================
  // directory browsing
  // =====================================================
  @Test
  public void browseRoot() throws ContentDirectoryException {
    browse(2L, "0", BrowseFlag.DIRECT_CHILDREN);
  }

  @Test
  public void browseMovies() throws ContentDirectoryException {
    browse(3L, "1", BrowseFlag.DIRECT_CHILDREN);
  }

  @Test
  public void browseTvShow() throws ContentDirectoryException {
    browse(4L, "2", BrowseFlag.DIRECT_CHILDREN);
  }

  @Test
  public void browseEpisode() throws ContentDirectoryException {
    browse(1L, "2/" + getValidShowID(), BrowseFlag.DIRECT_CHILDREN);
  }

  // =====================================================
  // meta data information
  // =====================================================
  @Test
  public void metadataRootContainer() throws ContentDirectoryException {
    browse(1L, "0", BrowseFlag.METADATA);
  }

  @Test
  public void metadataMovie() throws ContentDirectoryException {
    browse(0L, "1/" + getValidMovieID(), BrowseFlag.METADATA);
  }

  @Test
  public void metadataEpisode() throws ContentDirectoryException {
    browse(1L, "2/" + getValidShowID() + "/1/2", BrowseFlag.METADATA);
  }

  // =====================================================
  // INVALID exception tests / empty responses!
  // =====================================================
  @Test
  public void metadataMovieContainer() throws ContentDirectoryException {
    browse(1L, "1", BrowseFlag.METADATA);
  }

  @Test
  public void metadataTvShowContainer() throws ContentDirectoryException {
    browse(1L, "2", BrowseFlag.METADATA);
  }

  @Test
  public void invalidMovieUUID() throws ContentDirectoryException {
    browse(0L, "1/00000000-0000-0000-0000-000000000000", BrowseFlag.METADATA);
  }

  @Test
  public void invalidShowUUID() throws ContentDirectoryException {
    browse(0L, "2/00000000-0000-0000-0000-000000000000/1/2", BrowseFlag.METADATA);
  }

  @Test
  public void invalidEpisodeSE() throws ContentDirectoryException {
    browse(0L, "2/" + getValidShowID() + "/10/20", BrowseFlag.METADATA);
  }

  private BrowseResult browse(Long expectedCount, String s, BrowseFlag b) throws ContentDirectoryException {
    BrowseResult r = CDS.browse(s, b, "", 0, 200, SortCriterion.valueOf("+dc:date,+dc:title"));
    assertEqual(expectedCount, r.getCountLong());
    return r;
  }

  private BrowseResult browse(Long expectedCount, String objectID, BrowseFlag browseFlag, String filter, long firstResult, long maxResults,
      SortCriterion[] orderby) throws ContentDirectoryException {
    BrowseResult r = CDS.browse(objectID, browseFlag, filter, firstResult, maxResults, orderby);
    assertEqual(expectedCount, r.getCountLong());
    return r;
  }

}
