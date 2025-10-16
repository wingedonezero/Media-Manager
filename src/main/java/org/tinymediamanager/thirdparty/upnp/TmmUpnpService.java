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

import org.jupnp.DefaultUpnpServiceConfiguration;
import org.jupnp.UpnpServiceImpl;
import org.jupnp.binding.xml.DeviceDescriptorBinder;
import org.jupnp.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import org.jupnp.binding.xml.RecoveringUDA10ServiceDescriptorBinderImpl;
import org.jupnp.binding.xml.ServiceDescriptorBinder;
import org.jupnp.model.DiscoveryOptions;
import org.jupnp.protocol.ProtocolFactory;
import org.jupnp.registry.Registry;
import org.jupnp.transport.impl.jetty.StreamClientConfigurationImpl;
import org.jupnp.transport.spi.DatagramProcessor;
import org.jupnp.transport.spi.NetworkAddressFactory;
import org.jupnp.transport.spi.StreamClient;
import org.jupnp.transport.spi.StreamServer;

public class TmmUpnpService extends UpnpServiceImpl {

  public TmmUpnpService(int port) {
    this(port, true);
  }

  public TmmUpnpService(int port, boolean createServer) {
    // use defaults, except...
    super(new DefaultUpnpServiceConfiguration(port) {

      @Override
      public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
        // Recommended for best interoperability with broken UPnP stacks!
        return new RecoveringUDA10DeviceDescriptorBinderImpl();
      }

      @Override
      public ServiceDescriptorBinder getServiceDescriptorBinderUDA10() {
        return new RecoveringUDA10ServiceDescriptorBinderImpl();
      }

      @Override
      @SuppressWarnings("rawtypes")
      public StreamClient createStreamClient() {
        return new TmmStreamClientImpl(new StreamClientConfigurationImpl(createDefaultExecutorService()));
      }

      @Override
      @SuppressWarnings("rawtypes")
      public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        if (createServer) {
          return new TmmStreamServerImpl(new StreamServerConfigurationImpl(networkAddressFactory.getStreamListenPort()),
              createDefaultExecutorService());
        }
        else {
          // do not start a MediaServer ;)
          return null;
        }
      }

      @Override
      public DatagramProcessor getDatagramProcessor() {
        return new TmmDatagramProcessor();
      }
    });

  }

  @Override
  protected Registry createRegistry(ProtocolFactory protocolFactory) {
    Registry result = super.createRegistry(protocolFactory);
    result.addListener(UpnpListener.getListener());
    result.addDevice(TmmLocalDevice.createMediaServerDevice(), new DiscoveryOptions(true));
    return result;
  }
}
