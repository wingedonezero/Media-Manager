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
package org.tinymediamanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaSource;
import org.tinymediamanager.core.entities.MediaStreamInfo;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

public class MediaInfoSnapshot implements IMediaInformation {
  private final MediaFile                   mainVideoFile;

  private final long                        videoFilesize;
  private final String                      videoFormat;
  private final int                         videoBitDepth;
  private final String                      videoResolution;
  private final float                       aspectRatio;
  private final Float                       aspectRatio2;
  private final String                      videoCodec;
  private final double                      frameRate;
  private final boolean                     videoIn3D;
  private final String                      videoHDRFormat;

  private final String                      audioCodec;
  private final String                      audioChannels;
  private final String                      audioChannelsDot;
  private final String                      audioLanguage;
  private final String                      containerFormat;
  private final int                         audioStreamCount;
  private final List<String>                audioCodecList;
  private final List<String>                audioChannelList;
  private final List<String>                audioChannelDotList;
  private final List<String>                audioLanguageList;

  private final int                         subtitleStreamCount;
  private final List<String>                subtitleLanguageList;
  private final List<String>                subtitleCodecList;
  private final List<MediaStreamInfo.Flags> subtitleTypeList;

  public MediaInfoSnapshot(List<MediaFile> mediaFiles) {
    List<MediaFile> videoFiles = filterForTyes(mediaFiles, MediaFileType.VIDEO);
    mainVideoFile = getMainVideoFile(videoFiles);

    videoFilesize = calculateVideoFileSize(videoFiles);
    videoFormat = mainVideoFile.getVideoFormat();
    videoBitDepth = mainVideoFile.getBitDepth();
    videoResolution = mainVideoFile.getVideoResolution();
    aspectRatio = mainVideoFile.getAspectRatio();
    aspectRatio2 = mainVideoFile.getAspectRatio2();
    videoCodec = mainVideoFile.getVideoCodec();
    frameRate = mainVideoFile.getFrameRate();
    videoIn3D = StringUtils.isNotBlank(mainVideoFile.getVideo3DFormat());
    videoHDRFormat = mainVideoFile.getHdrFormat();
    containerFormat = mainVideoFile.getContainerFormat();

    List<MediaFile> audioFiles = filterForTyes(mediaFiles, MediaFileType.AUDIO);
    audioCodec = mainVideoFile.getAudioCodec();
    audioChannels = mainVideoFile.getAudioChannels();
    audioChannelsDot = mainVideoFile.getAudioChannelsDot();
    audioLanguage = mainVideoFile.getAudioLanguage();
    audioStreamCount = getAudioStreamCount(audioFiles);
    audioCodecList = getAudioCodecList(audioFiles);
    audioChannelList = getAudioChannelList(audioFiles);
    audioChannelDotList = getAudioChannelDotList(audioFiles);
    audioLanguageList = getAudioLanguageList(audioFiles);

    List<MediaFile> subtitleFiles = filterForTyes(mediaFiles, MediaFileType.SUBTITLE);
    subtitleStreamCount = getSubtitleStreamCount(subtitleFiles);
    subtitleLanguageList = getSubtitleLanguageList(subtitleFiles);
    subtitleCodecList = getSubtitleCodecList(subtitleFiles);

    subtitleTypeList = getSubtitleTypeList(subtitleFiles);
  }

  private MediaFile getMainVideoFile(List<MediaFile> videoFiles) {
    if (videoFiles.isEmpty()) {
      return MediaFile.EMPTY_MEDIAFILE;
    }

    MediaFile firstVideoFile = videoFiles.get(0);
    if (videoFiles.size() == 1) {
      // only one video file? we can return here
      return firstVideoFile;
    }

    MediaFile vid = null;

    if (firstVideoFile.isDVDFile()) {
      // if the first video file is a DVD type, we need to process the disc logic
      vid = getMainDVDVideoFile(videoFiles);
    }
    else if (firstVideoFile.getStacking() > 0) {
      // stacked video files - find the one with the lowest stacking value
      for (MediaFile mediaFile : videoFiles) {
        if (vid == null || mediaFile.getStacking() < vid.getStacking()) {
          vid = mediaFile;
        }
      }
    }

    if (vid == null) {
      for (MediaFile mediaFile : videoFiles) {
        if (vid == null || mediaFile.getFilesize() >= vid.getFilesize()) {
          vid = mediaFile;
        }
      }
    }

    if (vid == null) {
      // prevent null
      vid = MediaFile.EMPTY_MEDIAFILE;
    }

    return vid;
  }

  public MediaFile getMainDVDVideoFile(List<MediaFile> videoFiles) {
    MediaFile vid = null;

    // find IFO file with the longest duration
    for (MediaFile mf : videoFiles) {
      if (mf.getExtension().equalsIgnoreCase("ifo")) {
        if (vid == null || mf.getDuration() > vid.getDuration()) {
          vid = mf;
        }
      }
    }
    // find the vob matching to our ifo
    if (vid != null) {
      // check DVD VOBs
      String prefix = StrgUtils.substr(vid.getFilename(), "(?i)^(VTS_\\d+).*");
      if (prefix.isEmpty()) {
        // check HD-DVD
        prefix = StrgUtils.substr(vid.getFilename(), "(?i)^(HV\\d+)I.*");
      }
      for (MediaFile mif : videoFiles) {
        if (mif.getFilename().startsWith(prefix) && !mif.getFilename().endsWith("IFO")) {
          vid = mif;
          // take last to not get the menu one...
        }
      }
    }

    // no IFO/VOB? - might be bluray
    if (vid == null) {
      for (MediaFile mf : videoFiles) {
        if (mf.getExtension().equalsIgnoreCase("m2ts")) {
          if (vid == null || mf.getDuration() > vid.getDuration()) {
            vid = mf;
          }
        }
      }
    }

    return vid;
  }

  private long calculateVideoFileSize(List<MediaFile> videoFiles) {
    long filesize = 0;

    for (MediaFile mf : videoFiles) {
      filesize += mf.getFilesize();
    }

    return filesize;
  }

  private int getAudioStreamCount(List<MediaFile> audioFiles) {
    int audioStreamCount = getMainVideoFile().getAudioStreams().size();

    for (MediaFile mf : audioFiles) {
      audioStreamCount += mf.getAudioStreams().size();
    }

    return audioStreamCount;
  }

  private List<String> getAudioCodecList(List<MediaFile> audioFiles) {
    List<String> codecs = new ArrayList<>(getMainVideoFile().getAudioCodecList());

    for (MediaFile mf : audioFiles) {
      codecs.addAll(mf.getAudioCodecList());
    }

    return codecs;
  }

  private List<String> getAudioChannelList(List<MediaFile> audioFiles) {
    List<String> channels = new ArrayList<>(getMainVideoFile().getAudioChannelsList());

    for (MediaFile mf : audioFiles) {
      channels.addAll(mf.getAudioChannelsList());
    }

    return channels;
  }

  private List<String> getAudioChannelDotList(List<MediaFile> audioFiles) {
    List<String> channels = new ArrayList<>(getMainVideoFile().getAudioChannelsDotList());

    for (MediaFile mf : audioFiles) {
      channels.addAll(mf.getAudioChannelsDotList());
    }

    return channels;
  }

  private List<String> getAudioLanguageList(List<MediaFile> audioFiles) {
    List<String> languages = new ArrayList<>(getMainVideoFile().getAudioLanguagesList());

    for (MediaFile mf : audioFiles) {
      languages.addAll(mf.getAudioLanguagesList());
    }

    return languages;
  }

  private int getSubtitleStreamCount(List<MediaFile> subtitleFiles) {
    int subtitleStreamCount = getMainVideoFile().getSubtitles().size();

    for (MediaFile mf : subtitleFiles) {
      subtitleStreamCount += mf.getSubtitles().size();
    }

    return subtitleStreamCount;
  }

  private List<String> getSubtitleLanguageList(List<MediaFile> subtitleFiles) {
    Set<String> languages = new TreeSet<>(getMainVideoFile().getSubtitleLanguages());

    for (MediaFile mf : subtitleFiles) {
      languages.addAll(mf.getSubtitleLanguages());
    }

    return new ArrayList<>(languages);
  }

  private List<String> getSubtitleCodecList(List<MediaFile> subtitleFiles) {
    Set<String> codecs = new TreeSet<>(getMainVideoFile().getSubtitleCodecs());

    for (MediaFile mf : subtitleFiles) {
      codecs.addAll(mf.getSubtitleCodecs());
    }

    return new ArrayList<>(codecs);
  }

  private List<MediaStreamInfo.Flags> getSubtitleTypeList(List<MediaFile> subtitleFiles) {
    Set<MediaStreamInfo.Flags> types = new TreeSet<>(getMainVideoFile().getSubtitleTypes());

    for (MediaFile mf : subtitleFiles) {
      types.addAll(mf.getSubtitleTypes());
    }
    return new ArrayList<>(types);
  }

  private List<MediaFile> filterForTyes(List<MediaFile> mediaFiles, MediaFileType... types) {
    if (ListUtils.isEmpty(mediaFiles)) {
      return Collections.emptyList();
    }

    List<MediaFile> filteredMediaFiles = new ArrayList<>();

    for (MediaFile mediaFile : mediaFiles) {
      boolean match = false;
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          match = true;
        }
      }
      if (match) {
        filteredMediaFiles.add(mediaFile);
      }
    }

    return filteredMediaFiles;
  }

  @Override
  public MediaFile getMainVideoFile() {
    return mainVideoFile;
  }

  @Override
  public long getVideoFilesize() {
    return videoFilesize;
  }

  @Override
  public String getMediaInfoVideoFormat() {
    return videoFormat;
  }

  @Override
  public int getMediaInfoVideoBitDepth() {
    return videoBitDepth;
  }

  @Override
  public String getMediaInfoVideoResolution() {
    return videoResolution;
  }

  @Override
  public float getMediaInfoAspectRatio() {
    return aspectRatio;
  }

  @Override
  public Float getMediaInfoAspectRatio2() {
    return aspectRatio2;
  }

  @Override
  public String getMediaInfoVideoCodec() {
    return videoCodec;
  }

  @Override
  public double getMediaInfoFrameRate() {
    return frameRate;
  }

  @Override
  public boolean isVideoIn3D() {
    return videoIn3D;
  }

  @Override
  public String getVideoHDRFormat() {
    return videoHDRFormat;
  }

  @Override
  public Integer getMediaInfoAudioStreamCount() {
    return audioStreamCount;
  }

  @Override
  public String getMediaInfoAudioCodec() {
    return audioCodec;
  }

  @Override
  public List<String> getMediaInfoAudioCodecList() {
    return audioCodecList;
  }

  @Override
  public String getMediaInfoAudioChannels() {
    return audioChannels;
  }

  @Override
  public String getMediaInfoAudioChannelsDot() {
    return audioChannelsDot;
  }

  @Override
  public List<String> getMediaInfoAudioChannelList() {
    return audioChannelList;
  }

  @Override
  public List<String> getMediaInfoAudioChannelDotList() {
    return audioChannelDotList;
  }

  @Override
  public String getMediaInfoAudioLanguage() {
    return audioLanguage;
  }

  @Override
  public List<String> getMediaInfoAudioLanguageList() {
    return audioLanguageList;
  }

  @Override
  public Integer getMediaInfoSubtitleStreamCount() {
    return subtitleStreamCount;
  }

  @Override
  public List<String> getMediaInfoSubtitleLanguageList() {
    return subtitleLanguageList;
  }

  @Override
  public List<String> getMediaInfoSubtitleCodecList() {
    return subtitleCodecList;
  }

  @Override
  public List<MediaStreamInfo.Flags> getMediaInfoSubtitleTypeList() {
    return subtitleTypeList;
  }

  @Override
  public String getMediaInfoContainerFormat() {
    return containerFormat;
  }

  @Override
  public MediaSource getMediaInfoSource() {
    // not used here, but in the MediaEntity
    return null;
  }
}
