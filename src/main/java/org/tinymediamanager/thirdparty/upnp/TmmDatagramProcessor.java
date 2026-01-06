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

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import org.jupnp.http.Headers;
import org.jupnp.model.UnsupportedDataException;
import org.jupnp.model.message.IncomingDatagramMessage;
import org.jupnp.model.message.OutgoingDatagramMessage;
import org.jupnp.model.message.UpnpOperation;
import org.jupnp.model.message.UpnpRequest;
import org.jupnp.model.message.UpnpResponse;
import org.jupnp.transport.impl.DatagramProcessorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TmmDatagramProcessor extends DatagramProcessorImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatagramProcessorImpl.class);

  /**
   * Overridden as JUPnP logger show null char from the datagram data buffer. Check on JUPnP update if it's corrected, then remove the overriding
   * method.
   * 
   * @param receivedOnAddress
   * @param datagram
   * @return
   * @throws UnsupportedDataException
   */
  @Override
  public IncomingDatagramMessage read(InetAddress receivedOnAddress, DatagramPacket datagram) throws UnsupportedDataException {

    if (Upnp.getInstance().getBlockedIps().contains(datagram.getAddress().getHostAddress())) {
      throw new UnsupportedDataException("IP on blocklist - skipping");
    }

    try {
      // if (LOGGER.isTraceEnabled()) {
      LOGGER.debug("Reading message data from: {}:{}\n{}\n{}\n{}", datagram.getAddress(), datagram.getPort(),
          "===================================== DATAGRAM BEGIN ============================================", new String(datagram.getData()).trim(),
          "-===================================== DATAGRAM END =============================================");
      // }

      ByteArrayInputStream is = new ByteArrayInputStream(datagram.getData());

      String[] startLine = Headers.readLine(is).split(" ");
      if (startLine[0].startsWith("HTTP/1.")) {
        return readResponseMessage(receivedOnAddress, datagram, is, Integer.parseInt(startLine[1]), startLine[2], startLine[0]);
      }
      else {
        return readRequestMessage(receivedOnAddress, datagram, is, startLine[0], startLine[2]);
      }

    }
    catch (Exception ex) {
      throw new UnsupportedDataException("Could not parse headers: " + ex, ex, datagram.getData());
    }
  }

  /**
   * Overridden as there is no simple way to change the product/version for datagrams message.
   *
   * @param message
   * @return
   * @throws UnsupportedDataException
   */
  @Override
  public DatagramPacket write(OutgoingDatagramMessage message) throws UnsupportedDataException {
    StringBuilder statusLine = new StringBuilder();

    UpnpOperation operation = message.getOperation();

    if (operation instanceof UpnpRequest requestOperation) {
      statusLine.append(requestOperation.getHttpMethodName()).append(" * ");
      statusLine.append("HTTP/1.").append(operation.getHttpMinorVersion()).append("\r\n");
    }
    else if (operation instanceof UpnpResponse responseOperation) {
      statusLine.append("HTTP/1.").append(operation.getHttpMinorVersion()).append(" ");
      statusLine.append(responseOperation.getStatusCode()).append(" ").append(responseOperation.getStatusMessage());
      statusLine.append("\r\n");
    }
    else {
      throw new UnsupportedDataException("Message operation is not request or response, don't know how to process: " + message);
    }

    // UDA 1.0, 1.1.2: No body but message must have a blank line after header
    StringBuilder messageData = new StringBuilder();
    messageData.append(statusLine);

    messageData.append(message.getHeaders().toString()).append("\r\n");

    // if (LOGGER.isTraceEnabled()) {
    LOGGER.debug("Writing message data for {} to: {}:{}\n{}\n{}\n{}", message, message.getDestinationAddress(), message.getDestinationPort(),
        "===================================== DATAGRAM BEGIN ============================================",
        messageData.toString().substring(0, messageData.length() - 2),
        "-===================================== DATAGRAM END =============================================");
    // }

    // According to HTTP 1.0 RFC, headers and their values are US-ASCII
    // TODO: Probably should look into escaping rules, too
    byte[] data = messageData.toString().getBytes(StandardCharsets.US_ASCII);

    LOGGER.trace("Writing new datagram packet with " + data.length + " bytes for: " + message);
    return new DatagramPacket(data, data.length, message.getDestinationAddress(), message.getDestinationPort());
  }
}
