package org.tinymediamanager.thirdparty;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tinymediamanager.core.BasicITest;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.jsonrpc.api.call.VideoLibrary;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieFields;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.TVShowFields;
import org.tinymediamanager.scraper.entities.MediaEpisodeGroup;
import org.tinymediamanager.scraper.entities.MediaEpisodeNumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// copy of impl to quick test em

public class ITKodiRPCTestLocal extends BasicITest {

  @Before
  public void initRPC() throws Exception {
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
  }

  @After
  public void tearDown() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  @Test
  public void testMovieMapping() throws IOException {
    // set same paths as in Kodi JSONs
    Movie m = new Movie();
    m.setDataSource("C:\\some\\datasource");
    m.setPath("C:\\some\\datasource\\5 Centimeters per Second (2007)");
    MediaFile mf = new MediaFile();
    mf.setType(MediaFileType.VIDEO);
    mf.setPath("C:\\some\\datasource\\5 Centimeters per Second (2007)");
    mf.setFilename("5 Centimeters per Second (2007) 1080p h264.mkv");
    m.addToMediaFiles(mf);
    MovieModuleManager.getInstance().getMovieList().addMovie(m);

    // map Kodi response to TMM entities
    ObjectMapper mapper = new ObjectMapper();

    // load saved KodiRPC movies response
    JsonNode node = mapper.readTree(new File("src/test/resources/KodiRPC/getMovies.json"));
    VideoLibrary.GetMovies call = new VideoLibrary.GetMovies(MovieFields.FILE);
    call.setResponse(node);
    KodiRPC.getInstance().getAndSetMovieMappings(call.getResults());

    assertEquals(1, KodiRPC.getInstance().getMappedMoviesSize());
  }

  @Test
  public void testTvShowMapping() throws IOException {
    // set same paths as in Kodi JSONs
    TvShow s = new TvShow();
    s.setTitle("Clannad");
    s.setDataSource("C:\\some\\datasource");
    s.setPath("C:\\some\\datasource\\Clannad (2007)");
    TvShowEpisode episode = new TvShowEpisode();
    episode.setDataSource("C:\\some\\datasource");
    episode.setPath("C:\\some\\datasource\\Clannad (2007)");
    episode.setTvShow(s);
    episode.setTitle("clannad-EP");
    episode.setEpisode(new MediaEpisodeNumber(MediaEpisodeGroup.DEFAULT_AIRED, 1, 2));
    MediaFile mf = new MediaFile();
    mf.setType(MediaFileType.VIDEO);
    mf.setPath("C:\\some\\datasource\\Clannad (2007)");
    mf.setFilename("S01E01.mkv");
    episode.addToMediaFiles(mf);
    s.addEpisode(episode);
    TvShowModuleManager.getInstance().getTvShowList().addTvShow(s);

    // map Kodi response to TMM entities
    ObjectMapper mapper = new ObjectMapper();

    // load saved KodiRPC tvshow response
    JsonNode node = mapper.readTree(new File("src/test/resources/KodiRPC/getShows.json"));
    VideoLibrary.GetTVShows tvShowCall = new VideoLibrary.GetTVShows(TVShowFields.FILE);
    tvShowCall.setResponse(node);
    KodiRPC.getInstance().getAndSetTvShowMappings(tvShowCall.getResults());

    assertEquals(1, KodiRPC.getInstance().getMappedTvShowsSize());
  }
}
