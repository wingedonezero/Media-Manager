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

import org.jupnp.transport.spi.StreamServerConfiguration;

// copy of old org.fourthline.cling.transport.impl.StreamServerConfigurationImpl
public class StreamServerConfigurationImpl implements StreamServerConfiguration {

  private int listenPort;
  private int tcpConnectionBacklog;

  /**
   * Defaults to port '0', ephemeral.
   */
  public StreamServerConfigurationImpl() {
  }

  public StreamServerConfigurationImpl(int listenPort) {
    this.listenPort = listenPort;
  }

  public int getListenPort() {
    return listenPort;
  }

  public void setListenPort(int listenPort) {
    this.listenPort = listenPort;
  }

  /**
   * @return Maximum number of queued incoming connections to allow on the listening socket, default is system default.
   */
  public int getTcpConnectionBacklog() {
    return tcpConnectionBacklog;
  }

  public void setTcpConnectionBacklog(int tcpConnectionBacklog) {
    this.tcpConnectionBacklog = tcpConnectionBacklog;
  }
}