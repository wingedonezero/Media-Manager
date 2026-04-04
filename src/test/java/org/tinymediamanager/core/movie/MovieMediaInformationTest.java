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
package org.tinymediamanager.core.movie;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.tinymediamanager.TmmOsUtils;
import org.tinymediamanager.core.IMediaInformation;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaStreamInfo;
import org.tinymediamanager.core.movie.entities.Movie;

/**
 * Unit tests for {@link IMediaInformation} on {@link Movie} using MediaInfo XML fixtures.
 *
 * @author Manuel Laggner
 */
public class MovieMediaInformationTest extends BasicMovieTest {

  /**
   * Initializes test environment and ensures native libraries are available for media parsing.
   *
   * @throws Exception
   *           if setup fails
   */
  @Before
  @Override
  public void setup() throws Exception {
    super.setup();

    TmmOsUtils.loadNativeLibs();
  }

  /**
   * Verifies all methods from {@link IMediaInformation} for movies with MediaInfo XML backed media files.
   *
   * @throws Exception
   *           if fixture preparation or media parsing fails
   */
  @Test
  public void testIMediaInformationMethods() throws Exception {
    copyResourceFolderToWorkFolder("mediainfo");
    copyResourceFolderToWorkFolder("testmovies/MediainfoXML");

    Path mediainfoFolder = getWorkFolder().resolve("mediainfo");
    Path legacyMediainfoFolder = getWorkFolder().resolve("testmovies/MediainfoXML");

    Movie movie = new Movie();

    MediaFile videoMediaFile = new MediaFile(mediainfoFolder.resolve("subtitle-title.avi"));
    videoMediaFile.gatherMediaInformation();
    movie.addToMediaFiles(videoMediaFile);

    // fake them to get more data
    MediaFile audioMediaFile = new MediaFile(legacyMediainfoFolder.resolve("MediaInfoMKV.mkv"));
    audioMediaFile.gatherMediaInformation();
    audioMediaFile.setType(MediaFileType.AUDIO);
    movie.addToMediaFiles(audioMediaFile);

    // fake them to get more data
    MediaFile subtitleMediaFile = new MediaFile(legacyMediainfoFolder.resolve("MediaInfo-BD.iso"));
    subtitleMediaFile.gatherMediaInformation();
    subtitleMediaFile.setType(MediaFileType.SUBTITLE);
    movie.addToMediaFiles(subtitleMediaFile);

    assertMediaInformation(movie, movie);
  }

  /**
   * Asserts all values exposed through {@link IMediaInformation} against expected values from attached media files.
   *
   * @param mediaInformation
   *          the media information interface under test
   * @param movie
   *          the movie used as source for media file aggregation
   */
  private void assertMediaInformation(IMediaInformation mediaInformation, Movie movie) {
    MediaFile mainVideoFile = movie.getMainVideoFile();

    assertThat(mediaInformation.getMainVideoFile()).isEqualTo(mainVideoFile);

    assertThat(mediaInformation.getVideoFilesize())
        .isEqualTo(movie.getMediaFiles(MediaFileType.VIDEO).stream().mapToLong(MediaFile::getFilesize).sum());

    assertThat(mediaInformation.getMediaInfoVideoFormat()).isEqualTo(mainVideoFile.getVideoFormat());
    assertThat(mediaInformation.getMediaInfoVideoBitDepth()).isEqualTo(mainVideoFile.getBitDepth());
    assertThat(mediaInformation.getMediaInfoVideoResolution()).isEqualTo(mainVideoFile.getVideoResolution());
    assertThat(mediaInformation.getMediaInfoAspectRatio()).isEqualTo(mainVideoFile.getAspectRatio());
    assertThat(mediaInformation.getMediaInfoAspectRatio2()).isEqualTo(mainVideoFile.getAspectRatio2());
    assertThat(mediaInformation.getMediaInfoVideoCodec()).isEqualTo(mainVideoFile.getVideoCodec());
    assertThat(mediaInformation.getMediaInfoFrameRate()).isEqualTo(mainVideoFile.getFrameRate());
    assertThat(mediaInformation.isVideoIn3D()).isEqualTo(!mainVideoFile.getVideo3DFormat().isEmpty());
    assertThat(mediaInformation.getVideoHDRFormat()).isEqualTo(mainVideoFile.getHdrFormat());

    List<MediaFile> audioFiles = movie.getMediaFiles(MediaFileType.AUDIO);
    List<MediaFile> subtitleFiles = movie.getMediaFiles(MediaFileType.SUBTITLE);

    int expectedAudioStreamCount = mainVideoFile.getAudioStreams().size();
    for (MediaFile audioFile : audioFiles) {
      expectedAudioStreamCount += audioFile.getAudioStreams().size();
    }
    assertThat(mediaInformation.getMediaInfoAudioStreamCount()).isEqualTo(expectedAudioStreamCount);

    assertThat(mediaInformation.getMediaInfoAudioCodec()).isEqualTo(mainVideoFile.getAudioCodec());

    List<String> expectedAudioCodecList = new ArrayList<>(mainVideoFile.getAudioCodecList());
    for (MediaFile audioFile : audioFiles) {
      expectedAudioCodecList.addAll(audioFile.getAudioCodecList());
    }
    assertThat(mediaInformation.getMediaInfoAudioCodecList()).isEqualTo(expectedAudioCodecList);

    assertThat(mediaInformation.getMediaInfoAudioChannels()).isEqualTo(mainVideoFile.getAudioChannels());
    assertThat(mediaInformation.getMediaInfoAudioChannelsDot()).isEqualTo(mainVideoFile.getAudioChannelsDot());

    List<String> expectedAudioChannelList = new ArrayList<>(mainVideoFile.getAudioChannelsList());
    for (MediaFile audioFile : audioFiles) {
      expectedAudioChannelList.addAll(audioFile.getAudioChannelsList());
    }
    assertThat(mediaInformation.getMediaInfoAudioChannelList()).isEqualTo(expectedAudioChannelList);

    List<String> expectedAudioChannelDotList = new ArrayList<>(mainVideoFile.getAudioChannelsDotList());
    for (MediaFile audioFile : audioFiles) {
      expectedAudioChannelDotList.addAll(audioFile.getAudioChannelsDotList());
    }
    assertThat(mediaInformation.getMediaInfoAudioChannelDotList()).isEqualTo(expectedAudioChannelDotList);

    assertThat(mediaInformation.getMediaInfoAudioLanguage()).isEqualTo(mainVideoFile.getAudioLanguage());

    List<String> expectedAudioLanguageList = new ArrayList<>(mainVideoFile.getAudioLanguagesList());
    for (MediaFile audioFile : audioFiles) {
      expectedAudioLanguageList.addAll(audioFile.getAudioLanguagesList());
    }
    assertThat(mediaInformation.getMediaInfoAudioLanguageList()).isEqualTo(expectedAudioLanguageList);

    int expectedSubtitleStreamCount = mainVideoFile.getSubtitles().size();
    for (MediaFile subtitleFile : subtitleFiles) {
      expectedSubtitleStreamCount += subtitleFile.getSubtitles().size();
    }
    assertThat(mediaInformation.getMediaInfoSubtitleStreamCount()).isEqualTo(expectedSubtitleStreamCount);

    Set<String> expectedSubtitleLanguages = new TreeSet<>(mainVideoFile.getSubtitleLanguages());
    for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.AUDIO, MediaFileType.SUBTITLE)) {
      expectedSubtitleLanguages.addAll(mediaFile.getSubtitleLanguages());
    }
    assertThat(mediaInformation.getMediaInfoSubtitleLanguageList()).isEqualTo(new ArrayList<>(expectedSubtitleLanguages));

    Set<String> expectedSubtitleCodecs = new TreeSet<>(mainVideoFile.getSubtitleCodecs());
    for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.AUDIO, MediaFileType.SUBTITLE)) {
      expectedSubtitleCodecs.addAll(mediaFile.getSubtitleCodecs());
    }
    assertThat(mediaInformation.getMediaInfoSubtitleCodecList()).isEqualTo(new ArrayList<>(expectedSubtitleCodecs));

    Set<MediaStreamInfo.Flags> expectedSubtitleTypes = new TreeSet<>(mainVideoFile.getSubtitleTypes());
    for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.AUDIO, MediaFileType.SUBTITLE)) {
      expectedSubtitleTypes.addAll(mediaFile.getSubtitleTypes());
    }
    assertThat(mediaInformation.getMediaInfoSubtitleTypeList()).isEqualTo(new ArrayList<>(expectedSubtitleTypes));

    assertThat(mediaInformation.getMediaInfoContainerFormat()).isEqualTo(mainVideoFile.getContainerFormat());
  }
}
