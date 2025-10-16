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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jupnp.UpnpService;
import org.jupnp.controlpoint.ActionCallback;
import org.jupnp.model.action.ActionInvocation;
import org.jupnp.model.message.UpnpResponse;
import org.jupnp.model.message.header.UDADeviceTypeHeader;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.Service;
import org.jupnp.model.types.UDADeviceType;
import org.jupnp.model.types.UDAServiceId;
import org.jupnp.support.avtransport.callback.Play;
import org.jupnp.support.avtransport.callback.SetAVTransportURI;
import org.jupnp.support.avtransport.callback.Stop;
import org.jupnp.support.contentdirectory.DIDLParser;
import org.jupnp.support.model.DIDLContent;
import org.jupnp.transport.RouterException;
import org.jupnp.util.SpecificationViolationReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.thirdparty.NetworkUtil;

/**
 * Small helper to manage the JUPnP service used by TinyMediaManager.
 *
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Start/stop the local UPnP service</li>
 * <li>Discover remote media renderers</li>
 * <li>Control a selected renderer (play/stop)</li>
 * </ul>
 * </p>
 * 
 * @author Myron Boyle
 */
public class Upnp {
  private static final Logger LOGGER        = LoggerFactory.getLogger(Upnp.class);

  // ROOT is fixed to 0, do not change
  public static final String  ID_ROOT       = "0";
  public static final String  ID_MOVIES     = "1";
  public static final String  ID_TVSHOWS    = "2";

  private static boolean      initialized   = false;
  private static Upnp         instance;

  private final String        ipAddress;
  private TmmUpnpService      upnpService   = null;
  private Service<?, ?>       playerService = null;

  public final Set<String>    blockedIps    = new HashSet<>();

  // currently active UPnP listen port
  private int                 upnpPort;

  private Upnp() {
    ipAddress = NetworkUtil.getMachineIPAddress();
  }

  public static synchronized Upnp getInstance() {
    if (Upnp.instance == null) {
      SpecificationViolationReporter.disableReporting();
      Upnp.instance = new Upnp();
    }
    return Upnp.instance;
  }

  public static void init() {
    initialized = true;
  }

  public UpnpService getUpnpService() {
    return this.upnpService;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public int getPort() {
    return upnpPort;
  }

  public Set<String> getBlockedIps() {
    return blockedIps;
  }

  public void addBlockedIp(String ip) {
    blockedIps.add(ip);
  }

  /**
   * Starts the UPnP service (JUPnP) and registers the local media server/device. Safe to call multiple times.
   */
  private synchronized void createUpnpService() {
    // only create service if we want it
    if (Settings.getInstance().isUpnpShareLibrary() || Settings.getInstance().isUpnpRemotePlay()) {
      if (this.upnpService == null) {
        this.upnpService = new TmmUpnpService(this.upnpPort, Settings.getInstance().isUpnpShareLibrary());
        this.upnpService.startup();
        boolean isStarted;

        try {
          isStarted = upnpService.getRouter().isEnabled();
        }
        catch (RouterException ex) {
          isStarted = false;
        }

        if (!isStarted) {
          LOGGER.error("UPnP fatal error: unable to start UPnP service");
        }
        else {
          LOGGER.debug("UPnP (JUPnP) services are online and listening for media renderers on port {}", this.upnpPort);
        }
      }
    }
  }

  /**
   * Update UPnP configuration: enable/disable and port. If port changes while enabled the service will be restarted on the new port.
   *
   * @param shareLibrary
   *          whether to enable the UPnP media server
   * @param port
   *          the listen port to use when enabling
   */
  public synchronized void updateConfiguration(boolean shareLibrary, boolean remotePlay, int port) {
    if (!initialized) {
      // UPnP not initialized yet - ignore changes
      return;
    }

    // disable
    if (!shareLibrary && !remotePlay) {
      if (upnpService != null) {
        LOGGER.debug("Stopping UPnP service as requested by settings change");
        upnpService.shutdown();
        upnpService = null;
      }
      return;
    }

    // enable
    if (upnpService == null) {
      this.upnpPort = port;
      this.blockedIps.clear();
      LOGGER.debug("Starting UPnP service on port {} as requested by settings", this.upnpPort);
      createUpnpService();
      return;
    }

    // already running but port changed -> restart
    if (this.upnpPort != port) {
      LOGGER.debug("Restarting UPnP service on new port {} (was {})", port, this.upnpPort);
      try {
        upnpService.shutdown();
      }
      catch (Exception e) {
        LOGGER.debug("Error while shutting down existing UPnP service", e);
      }
      this.upnpPort = port;
      this.upnpService = null;
      createUpnpService();
    }
  }

  /**
   * Sends an SSDP search for MediaRenderer devices. Call {@link #getAvailablePlayers()} after a short delay to retrieve discovered devices.
   */
  public void sendPlayerSearchRequest() {
    createUpnpService();
    this.upnpService.getControlPoint().search(new UDADeviceTypeHeader(new UDADeviceType("MediaRenderer")));
  }

  /**
   * Collects discovered MediaRenderer devices from the registry.
   *
   * @return list of discovered devices (may be empty)
   */
  public List<Device<?, ?, ?>> getAvailablePlayers() {
    createUpnpService();
    List<Device<?, ?, ?>> ret = new ArrayList<>();
    for (Device<?, ?, ?> device : this.upnpService.getRegistry().getDevices()) {
      if (device.getType().getType().equals("MediaRenderer")) {
        LOGGER.debug("Found MediaRenderer: {}", device.getDisplayString());
        ret.add(device);
      }
    }
    return ret;
  }

  /**
   * Selects a renderer device for subsequent control commands. If the device does not expose an AVTransport service the selected player will be null.
   *
   * @param device
   *          The device to use as player
   */
  public void setPlayer(Device<?, ?, ?> device) {
    this.playerService = device.findService(new UDAServiceId("AVTransport"));
    if (this.playerService == null) {
      LOGGER.debug("Could not find AVTransport service on UPnP device: {}", device.getDisplayString());
    }
  }

  /**
   * Play a media file on the selected renderer. The method will construct DIDL-Lite metadata for known media entity types (Movie, TvShowEpisode). If
   * a MediaEntity is not provided, the method will use the URL from the MediaFile only (if present).
   *
   * @param me
   *          media entity (optional)
   * @param mf
   *          media file (required)
   */
  public void playFile(MediaEntity me, MediaFile mf) {
    if (this.playerService == null) {
      LOGGER.debug("No UPnP player set; did you call setPlayer(Device)?");
      return;
    }

    if (mf == null) {
      LOGGER.debug("No MediaFile provided to playFile()");
      return;
    }

    String url = "";
    String meta = "NO METADATA";

    if (me != null) {
      try {
        DIDLContent didl = new DIDLContent();
        DIDLParser dip = new DIDLParser();
        if (me instanceof Movie movie) {
          didl.addItem(Metadata.getUpnpMovie(movie, true));
        }
        else if (me instanceof TvShowEpisode episode) {
          didl.addItem(Metadata.getUpnpTvShowEpisode(episode.getTvShow(), episode, true));
        }

        // get url from didl, no need to regenerate this
        url = didl.getItems().get(0).getResources().get(0).getValue();

        meta = dip.generate(didl);
      }
      catch (Exception e) {
        LOGGER.debug("Could not generate metadata / URL for UPnP playback", e);
        return;
      }
    }

    ActionCallback setAVTransportURIAction = new SetAVTransportURI(this.playerService, url, meta) {
      @Override
      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        LOGGER.warn("Setting URL for player failed: {}", defaultMsg);
      }
    };
    this.upnpService.getControlPoint().execute(setAVTransportURIAction);

    ActionCallback playAction = new Play(this.playerService) {
      @Override
      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        LOGGER.warn("Playing via UPnP failed: {}", defaultMsg);
      }
    };
    this.upnpService.getControlPoint().execute(playAction);
  }

  /**
   * Stop playback on the selected renderer (if any).
   */
  public void stopPlay() {
    if (this.playerService == null) {
      return;
    }

    ActionCallback stopAction = new Stop(this.playerService) {
      @Override
      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        LOGGER.warn("Stopping UPnP playback failed: {}", defaultMsg);
      }
    };
    this.upnpService.getControlPoint().execute(stopAction);
  }

  /**
   * Ensure the media server is started. This is an alias for {@link #createUpnpService()}.
   */
  public void startMediaServer() {
    createUpnpService();
  }

  /**
   * Stop the local UPnP service (shuts down JUPnP internals).
   */
  public void stopMediaServer() {
    if (this.upnpService != null) {
      this.upnpService.shutdown();
    }
  }

  /**
   * Shutdown UPnP components (stop playback and media server).
   */
  public void shutdown() {
    stopPlay();
    stopMediaServer();
  }
}