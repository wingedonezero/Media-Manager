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

import org.jupnp.model.message.Connection;
import org.jupnp.protocol.ProtocolFactory;
import org.jupnp.transport.impl.HttpExchangeUpnpStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;

public class TmmHttpExchangeUpnpStream extends HttpExchangeUpnpStream {

  private final Logger logger = LoggerFactory.getLogger(TmmHttpExchangeUpnpStream.class.getName());

  protected TmmHttpExchangeUpnpStream(ProtocolFactory protocolFactory, HttpExchange httpExchange) {
    super(protocolFactory, httpExchange);
  }

  protected Connection createConnection() {
    return null;
  }
}