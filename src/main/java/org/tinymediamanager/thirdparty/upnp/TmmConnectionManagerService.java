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

package org.tinymediamanager.thirdparty.upnp;

import org.jupnp.support.connectionmanager.ConnectionManagerService;
import org.jupnp.support.model.Protocol;
import org.jupnp.support.model.ProtocolInfo;
import org.jupnp.support.model.ProtocolInfos;
import org.jupnp.util.MimeType;

public class TmmConnectionManagerService extends ConnectionManagerService {

  public TmmConnectionManagerService() {
    super(getSourceProtocolInfos(), new ProtocolInfos());
  }

  private static ProtocolInfos getSourceProtocolInfos() {
    return new ProtocolInfos(
        // this one overlap all ???
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, MimeType.WILDCARD, ProtocolInfo.WILDCARD),
        // this one overlap all images ???
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/" + MimeType.WILDCARD, ProtocolInfo.WILDCARD),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "audio", ProtocolInfo.WILDCARD),
        // this one overlap all audio ???
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "audio/" + MimeType.WILDCARD, ProtocolInfo.WILDCARD),
        // this one overlap all video ???
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/" + MimeType.WILDCARD, ProtocolInfo.WILDCARD),
        // IMAGE
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/jpeg", "DLNA.ORG_PN=JPEG_TN"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/jpeg", "DLNA.ORG_PN=JPEG_SM"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/jpeg", "DLNA.ORG_PN=JPEG_MED"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/jpeg", "DLNA.ORG_PN=JPEG_LRG"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/jpeg", "DLNA.ORG_PN=JPEG_RES_H_V"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/png", "DLNA.ORG_PN=PNG_TN"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/png", "DLNA.ORG_PN=PNG_LRG"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/png", "DLNA.ORG_PN=PNG_LRG"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/png", "DLNA.ORG_PN=PNG_LRG"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "image/gif", "DLNA.ORG_PN=GIF_LRG"),
        // AUDIO
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "audio/mpeg", "DLNA.ORG_PN=MP3"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "audio/L16", "DLNA.ORG_PN=LPCM"),
        // VIDEO
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/mpeg", "DLNA.ORG_PN=AVC_TS_HD_24_AC3_ISO;SONY.COM_PN=AVC_TS_HD_24_AC3_ISO"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/mpeg", "DLNA.ORG_PN=MPEG_TS_SD_EU_ISO"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/mpeg", "DLNA.ORG_PN=MPEG_TS_HD_50_L2_ISO;SONY.COM_PN=HD2_50_ISO"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/mpeg", "DLNA.ORG_PN=MPEG_TS_HD_60_L2_ISO;SONY.COM_PN=HD2_60_ISO"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts",
            "DLNA.ORG_PN=AVC_TS_HD_24_AC3;SONY.COM_PN=AVC_TS_HD_24_AC3"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts",
            "DLNA.ORG_PN=AVC_TS_HD_24_AC3_T;SONY.COM_PN=AVC_TS_HD_24_AC3_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_PS_PAL"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_PS_NTSC"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_TS_SD_50_L2_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_TS_SD_50_AC3_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_TS_SD_60_L2_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_TS_SD_60_AC3_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_TS_SD_EU"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_TS_SD_EU_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_TS_HD_50_L2_T;SONY.COM_PN=HD2_50_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts", "DLNA.ORG_PN=MPEG_TS_HD_60_L2_T;SONY.COM_PN=HD2_60_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts",
            "DLNA.ORG_PN=AVC_TS_HD_50_AC3;SONY.COM_PN=AVC_TS_HD_50_AC3"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts",
            "DLNA.ORG_PN=AVC_TS_HD_60_AC3;SONY.COM_PN=AVC_TS_HD_60_AC3"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts",
            "DLNA.ORG_PN=AVC_TS_HD_50_AC3_T;SONY.COM_PN=AVC_TS_HD_50_AC3_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/vnd.dlna.mpeg-tts",
            "DLNA.ORG_PN=AVC_TS_HD_60_AC3_T;SONY.COM_PN=AVC_TS_HD_60_AC3_T"),
        new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/x-mp2t-mphl-188", ProtocolInfo.WILDCARD));
  }

}
