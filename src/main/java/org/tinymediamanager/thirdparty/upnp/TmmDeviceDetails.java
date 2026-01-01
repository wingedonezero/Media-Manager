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

import java.net.URI;

import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.DeviceIdentity;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.profile.DeviceDetailsProvider;
import org.jupnp.model.profile.RemoteClientInfo;
import org.jupnp.model.types.DLNACaps;
import org.jupnp.model.types.DLNADoc;
import org.jupnp.model.types.UDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.thirdparty.NetworkUtil;

public class TmmDeviceDetails implements DeviceDetailsProvider {

  public static final Logger         LOGGER         = LoggerFactory.getLogger(TmmDeviceDetails.class);
  public static final DeviceIdentity IDENTITY       = new DeviceIdentity(UDN.uniqueSystemIdentifier("tinyMediaManager"));
  public static final DeviceDetails  DEVICE_DETAILS = createDeviceDetails();

  static DeviceDetails createDeviceDetails() {
    DeviceDetails details = null;

    try {
      // @formatter:off
      details = new DeviceDetails(
          null, // baseURL
          "tinyMediaManager", // friendlyName
          new ManufacturerDetails("tinyMediaManager", "https://www.tinymediamanager.org/"),
          new ModelDetails("tinyMediaManager", "tinyMediaManager - Media Server", ReleaseInfo.getVersion()), 
          null, // serialNumber
          null, // upc
          new URI("http://" + NetworkUtil.getMachineIPAddress() + ":" + Upnp.getInstance().getPort() + "/dev/" + IDENTITY.getUdn().getIdentifierString() + "/desc"),
          new DLNADoc[] { new DLNADoc("DMS", DLNADoc.Version.V1_5), new DLNADoc("M-DMS", DLNADoc.Version.V1_5) }, new DLNACaps(new String[] {}),
          new DLNACaps(new String[] { "smi", "DCM10", "getMediaInfo.sec", "getCaptionInfo.sec","av-upload", "image-upload", "audio-upload" }));
      // @formatter:on
    }
    catch (Exception e) {
      // TODO: handle exception
    }
    return details;
  }

  @Override
  public DeviceDetails provide(RemoteClientInfo info) {
    return DEVICE_DETAILS;
  }
}
