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

package org.tinymediamanager.thirdparty.upnp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.jupnp.support.model.DIDLObject.Property.DC;
import org.jupnp.support.model.PersonWithRole;
import org.jupnp.support.model.ProtocolInfo;
import org.jupnp.support.model.Res;
import org.jupnp.support.model.item.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.Person;

class Metadata {

  // https://github.com/4thline/cling/tree/master/support/src/main/java/org/fourthline/cling/support/model/item
  private static final Logger LOGGER = LoggerFactory.getLogger(Metadata.class);

  private Metadata() {
    throw new IllegalAccessError();
  }

  /**
   * wraps a TMM movie into a UPNP movie/video item object
   * 
   * @param tmmMovie
   *          our movie
   * @param full
   *          full details, or when false just the mandatory for a directory listing (title, and a few others)
   * @return
   */
  public static Movie getUpnpMovie(org.tinymediamanager.core.movie.entities.Movie tmmMovie, boolean full) {
    Movie m = new Movie();
    try {
      m.setId(tmmMovie.getDbId().toString());
      m.setParentID(Upnp.ID_MOVIES);
      if (tmmMovie.getYear() > 0) {
        m.addProperty(new DC.DATE(tmmMovie.getReleaseDateFormatted())); // no setDate on Movie (but on other items)???
      }
      m.setTitle(tmmMovie.getTitle());

      for (MediaFile mf : tmmMovie.getMediaFiles(MediaFileType.VIDEO)) {
        String rel = tmmMovie.getPathNIO().relativize(mf.getFileAsPath()).toString().replaceAll("\\\\", "/");
        String url = "http://" + Upnp.getInstance().getIpAddress() + ":" + Upnp.getInstance().getPort() + "/upnp/movies/"
            + tmmMovie.getDbId().toString() + "/" + URLEncoder.encode(rel, StandardCharsets.UTF_8);
        Res r = createRes(url, mf);
        m.addResource(r);
      }

      // an own res is an own playable file - so it is not a poster the the video :/
      // List<MediaFile> posters = tmmMovie.getMediaFiles(MediaFileType.POSTER);
      // MediaFile poster = posters.isEmpty() ? null : posters.get(0);
      // if (poster != null) {
      // String rel = tmmMovie.getPathNIO().relativize(poster.getFileAsPath()).toString().replaceAll("\\\\", "/");
      // String url = "http://" + Upnp.getInstance().getIpAddress() + ":" + Upnp.getInstance().getPort() + "/upnp/movies/"
      // + tmmMovie.getDbId().toString() + "/" + URLEncoder.encode(rel, StandardCharsets.UTF_8);
      // Res r = createRes(url, poster);
      // m.addResource(r);
      // }

      if (full) {
        m.setDescription(tmmMovie.getPlot());
        m.setLanguage(tmmMovie.getSpokenLanguages());
        m.setRating(tmmMovie.getCertification().getLocalizedName());

        List<String> genres = new ArrayList<>();
        for (MediaGenres g : tmmMovie.getGenres()) {
          genres.add(g.getLocalizedName());
        }
        if (!genres.isEmpty()) {
          String[] arr = genres.toArray(new String[genres.size()]);
          m.setGenres(arr);
        }

        m.setActors(morphTmmPersons(tmmMovie.getActors()));
        m.setProducers(morphTmmPersons(tmmMovie.getProducers()));
        m.setDirectors(morphTmmPersons(tmmMovie.getDirectors()));
      }

    }
    catch (Exception e) {
      LOGGER.error("UPnP: Error getting movie '{}'", e.getMessage());
    }
    return m;
  }

  private static PersonWithRole[] morphTmmPersons(List<Person> persons) {
    List<PersonWithRole> ret = new ArrayList<>();
    for (Person person : persons) {
      ret.add(new PersonWithRole(person.getName(), person.getRole()));
    }
    if (!ret.isEmpty()) {
      return ret.toArray(new PersonWithRole[ret.size()]);
    }
    return new PersonWithRole[] {};
  }

  /**
   * wraps a TMM TvShowEpisode into a UPNP tvshow/video item object
   * 
   * @param show
   *          our TvShow
   * @param full
   *          full details, or when false just the mandatory for a directory listing (title, and a few others)
   * @return
   */
  public static Movie getUpnpTvShowEpisode(org.tinymediamanager.core.tvshow.entities.TvShow show,
      org.tinymediamanager.core.tvshow.entities.TvShowEpisode ep, boolean full) {
    Movie m = new Movie(); // yes, it is a UPNP movie object!

    try {
      // 2/UUID/S/E
      m.setId(Upnp.ID_TVSHOWS + "/" + show.getDbId().toString() + "/" + ep.getSeason() + "/" + ep.getEpisode());
      m.setParentID(Upnp.ID_TVSHOWS + "/" + show.getDbId().toString() + "/" + ep.getSeason());
      if (ep.getYear() > 0) {
        m.addProperty(new DC.DATE(ep.getFirstAiredFormatted())); // no setDate on Movie (but on other items)???
      }
      m.setTitle("S" + lz(ep.getSeason()) + "E" + lz(ep.getEpisode()) + " " + ep.getTitle());

      for (MediaFile mf : ep.getMediaFiles(MediaFileType.VIDEO)) {
        String rel = show.getPathNIO().relativize(mf.getFileAsPath()).toString().replaceAll("\\\\", "/");
        String url = "http://" + Upnp.getInstance().getIpAddress() + ":" + Upnp.getInstance().getPort() + "/upnp/tvshows/" + show.getDbId().toString()
            + "/" + URLEncoder.encode(rel, StandardCharsets.UTF_8);
        Res r = createRes(url, mf);
        m.addResource(r);
      }

      if (full) {
        m.setDescription(ep.getPlot());
        m.setRating(String.valueOf(ep.getRating()));

        List<String> genres = new ArrayList<>();
        for (MediaGenres g : show.getGenres()) {
          genres.add(g.getLocalizedName());
        }
        if (!genres.isEmpty()) {
          String[] arr = genres.toArray(new String[genres.size()]);
          m.setGenres(arr);
        }

        List<PersonWithRole> persons = new ArrayList<>();
        for (Person a : ep.getActors()) {
          persons.add(new PersonWithRole(a.getName(), a.getRole()));
        }
        if (!persons.isEmpty()) {
          PersonWithRole[] arr = persons.toArray(new PersonWithRole[persons.size()]);
          m.setActors(arr);
        }
      }

    }
    catch (Exception e) {
      LOGGER.error("UPnP: Error getting episode - '{}'", e.getMessage());
    }

    return m;
  }

  private static Res createRes(String url, MediaFile mf) {
    Res r = new Res();
    r.setValue(url);
    r.setProtocolInfo(new ProtocolInfo(MimeTypes.getMimeType(mf.getExtension())));
    if (mf.getFilesize() > 0) {
      r.setSize(mf.getFilesize());
    }
    if (mf.getVideoWidth() > 0 && mf.getVideoHeight() > 0) {
      r.setResolution(mf.getVideoWidth(), mf.getVideoHeight());
    }
    if (mf.getVideoBitRate() > 0) {
      r.setBitrate(Long.valueOf(mf.getVideoBitRate()));
    }
    if (mf.getAudioChannelCount() > 0) {
      r.setNrAudioChannels(Long.valueOf(mf.getAudioChannelCount()));
    }
    if (mf.getDuration() > 0) {
      r.setDuration(mf.getDurationHHMMSS());
    }
    return r;
  }

  /**
   * add leadingZero if only 1 char
   * 
   * @param num
   *          the number
   * @return the string with a leading 0
   */
  private static String lz(int num) {
    return String.format("%02d", num);
  }
}
