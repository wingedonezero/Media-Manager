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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jupnp.binding.annotations.AnnotationLocalServiceBinder;
import org.jupnp.model.DefaultServiceManager;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.DeviceIdentity;
import org.jupnp.model.meta.Icon;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.LocalService;
import org.jupnp.model.profile.DeviceDetailsProvider;
import org.jupnp.model.profile.RemoteClientInfo;
import org.jupnp.model.types.UDADeviceType;
import org.jupnp.support.connectionmanager.ConnectionManagerService;
import org.jupnp.support.xmicrosoft.AbstractMediaReceiverRegistrarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local UPnP MediaServer device for TinyMediaManager.
 */
public class TmmLocalDevice extends LocalDevice {
  private static final Logger                         LOGGER = LoggerFactory.getLogger(TmmLocalDevice.class);
  private final DeviceDetailsProvider                 deviceDetailsProvider;
  private final ContentDirectoryService               contentDirectoryService;
  private final TmmConnectionManagerService           connectionManagerService;
  private final AbstractMediaReceiverRegistrarService mediaReceiverRegistrarService;

  public TmmLocalDevice() throws ValidationException {
    super(
    // @formatter:off
        new DeviceIdentity(TmmDeviceDetails.IDENTITY.getUdn()),
        new UDADeviceType("MediaServer", 1),
        TmmDeviceDetails.DEVICE_DETAILS,
        createDeviceIcons(),
        createMediaServerServices(), 
        null);
    // @formatter:on
    this.deviceDetailsProvider = new TmmDeviceDetails();
    this.contentDirectoryService = getServiceImplementation(ContentDirectoryService.class);
    this.connectionManagerService = getServiceImplementation(TmmConnectionManagerService.class);
    this.mediaReceiverRegistrarService = getServiceImplementation(AbstractMediaReceiverRegistrarService.class);
  }

  @Override
  public DeviceDetails getDetails(RemoteClientInfo info) {
    if (deviceDetailsProvider != null) {
      return deviceDetailsProvider.provide(info);
    }
    LOGGER.info("UPnP: Hello, I'm {}", TmmDeviceDetails.IDENTITY.getUdn().getIdentifierString());
    // Delegate to superclass implementation instead of recursive call
    return super.getDetails(info);
  }

  public ContentDirectoryService getContentDirectoryService() {
    return this.contentDirectoryService;
  }

  public ConnectionManagerService getConnectionManagerService() {
    return this.connectionManagerService;
  }

  public AbstractMediaReceiverRegistrarService getMediaReceiverRegistrarService() {
    return this.mediaReceiverRegistrarService;
  }

  @SuppressWarnings("unchecked")
  private <T> T getServiceImplementation(Class<T> baseClass) {
    for (LocalService<?> service : getServices()) {
      if (service != null && service.getManager().getImplementation().getClass().equals(baseClass)) {
        return (T) service.getManager().getImplementation();
      }
    }
    return null;
  }

  /**
   * Create the local UMS device
   *
   * @return the device
   */
  public static TmmLocalDevice createMediaServerDevice() {
    try {
      return new TmmLocalDevice();
    }
    catch (ValidationException e) {
      LOGGER.debug("Error in upnp local device creation: {}", e.toString());
      return null;
    }
  }

  private static Icon[] createDeviceIcons() {
    List<Icon> icons = new ArrayList<>();
    try {
      // only when deployed, take from filesystem
      icons.add(new Icon(MimeTypes.getMimeTypeAsString("png"), 128, 128, 24, new File("tmm.png")));
    }
    catch (Exception e) {
      // in eclipse
      try {
        icons.add(new Icon(MimeTypes.getMimeTypeAsString("png"), 128, 128, 24, new File("AppBundler/tmm.png")));
      }
      catch (Exception e2) {
        LOGGER.debug("Did not find device icon");
      }
    }
    try {
      // only when deployed, take from filesystem
      icons.add(new Icon(MimeTypes.getMimeTypeAsString("ico"), 128, 128, 24, new File("tmm.ico")));
    }
    catch (Exception e) {
      // in eclipse
      try {
        icons.add(new Icon(MimeTypes.getMimeTypeAsString("ico"), 128, 128, 24, new File("AppBundler/tmm.ico")));
      }
      catch (Exception e2) {
        LOGGER.debug("Did not find device icon2");
      }
    }

    return icons.toArray(Icon[]::new);
  }

  /**
   * Create the upnp services provided by UMS
   *
   * @return the media server services
   */
  private static LocalService<?>[] createMediaServerServices() {
    List<LocalService<?>> services = new ArrayList<>();
    services.add(createContentDirectoryService());
    services.add(createServerConnectionManagerService());
    services.add(createMediaReceiverRegistrarService());
    return services.toArray(LocalService[]::new);
  }

  /**
   * Creates the upnp ContentDirectoryService.
   * 
   * @return The ContentDirectoryService.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static LocalService<?> createContentDirectoryService() {
    LocalService<?> contentDirectoryService = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
    contentDirectoryService.setManager(new DefaultServiceManager(contentDirectoryService, null) {

      @Override
      protected void lock() {
        // don't lock cds.
      }

      @Override
      protected void unlock() {
        // don't lock cds.
      }

      @Override
      protected ContentDirectoryService createServiceInstance() throws Exception {
        return new ContentDirectoryService();
      }
    });
    return contentDirectoryService;
  }

  /**
   * creates the upnp ConnectionManagerService.
   *
   * @return the service
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static LocalService<?> createServerConnectionManagerService() {
    LocalService<?> connectionManagerService = new AnnotationLocalServiceBinder().read(TmmConnectionManagerService.class);
    connectionManagerService.setManager(new DefaultServiceManager(connectionManagerService, TmmConnectionManagerService.class) {

      @Override
      protected int getLockTimeoutMillis() {
        return 1000;
      }

      @Override
      protected TmmConnectionManagerService createServiceInstance() throws Exception {
        return new TmmConnectionManagerService();
      }
    });

    return connectionManagerService;
  }

  /**
   * creates the upnp MediaReceiverRegistrarService.
   *
   * @return the service
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static LocalService<?> createMediaReceiverRegistrarService() {
    LocalService<?> mediaReceiverRegistrarService = new AnnotationLocalServiceBinder().read(MSMediaReceiverRegistrarService.class);
    mediaReceiverRegistrarService.setManager(new DefaultServiceManager(mediaReceiverRegistrarService, null) {
      @Override
      protected int getLockTimeoutMillis() {
        return 1000;
      }

      @Override
      protected MSMediaReceiverRegistrarService createServiceInstance() throws Exception {
        return new MSMediaReceiverRegistrarService();
      }
    });
    return mediaReceiverRegistrarService;
  }

}
