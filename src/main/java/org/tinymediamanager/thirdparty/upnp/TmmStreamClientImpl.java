
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

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jupnp.model.message.StreamRequestMessage;
import org.jupnp.model.message.StreamResponseMessage;
import org.jupnp.model.message.UpnpHeaders;
import org.jupnp.model.message.UpnpMessage;
import org.jupnp.model.message.UpnpRequest;
import org.jupnp.model.message.UpnpResponse;
import org.jupnp.model.message.header.UpnpHeader;
import org.jupnp.transport.impl.jetty.StreamClientConfigurationImpl;
import org.jupnp.transport.spi.InitializationException;
import org.jupnp.transport.spi.StreamClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

// Copy of org.fourthline.cling.transport.impl.StreamClientImpl, to not throw in constructor
public class TmmStreamClientImpl implements StreamClient<StreamClientConfigurationImpl> {

  private static final Logger                   LOGGER = LoggerFactory.getLogger(TmmStreamClientImpl.class);

  final protected StreamClientConfigurationImpl configuration;
  final protected OkHttpClient                  client;

  public TmmStreamClientImpl(StreamClientConfigurationImpl configuration) throws InitializationException {
    this.configuration = configuration;
    // client = TmmHttpClient.newBuilder()
    // .connectTimeout(Duration.ofSeconds(configuration.getTimeoutSeconds()))
    // .readTimeout(Duration.ofSeconds(configuration.getTimeoutSeconds()))
    // .build();

    try {
      // Trust manager that accepts all certificates
      final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }
      } };

      // Install the all-trusting trust manager
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustAllCerts, new SecureRandom());
      SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      X509TrustManager trustManager = (X509TrustManager) trustAllCerts[0];

      client = new OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(configuration.getTimeoutSeconds()))
          .readTimeout(Duration.ofSeconds(configuration.getTimeoutSeconds()))
          .sslSocketFactory(sslSocketFactory, trustManager)
          .hostnameVerifier((hostname, session) -> true)
          .build();
    }
    catch (Exception e) {
      throw new InitializationException("Failed to create SSL context for OkHttp client", e);
    }
  }

  @Override
  public StreamClientConfigurationImpl getConfiguration() {
    return configuration;
  }

  @Override
  public StreamResponseMessage sendRequest(StreamRequestMessage requestMessage) {
    if (Upnp.getInstance().getBlockedIps().contains(requestMessage.getUri().getHost())) {
      // throw new UnsupportedDataException("IP on blocklist - skipping");
      LOGGER.trace("IP on blocklist - skipping");
      return null;
    }

    final UpnpRequest requestOperation = requestMessage.getOperation();
    LOGGER.trace("Preparing HTTP request message with method '{}': {}", requestOperation.getHttpMethodName(), requestMessage);

    try {
      RequestBody body = null;
      if (requestMessage.getBody() != null) {
        if (requestMessage.getBodyType().equals(UpnpMessage.BodyType.STRING)) {
          body = RequestBody.create(requestMessage.getBodyString(), MediaType.parse("text/plain"));
        }
        else if (requestMessage.getBodyType().equals(UpnpMessage.BodyType.BYTES)) {
          body = RequestBody.create(requestMessage.getBodyBytes(), null);
        }
      }

      Builder request = new Request.Builder().url(requestOperation.getURI().toURL())
          .method(requestOperation.getHttpMethodName(), body)
          .addHeader(UpnpHeader.Type.USER_AGENT.getHttpName(),
              configuration.getUserAgentValue(requestMessage.getUdaMajorVersion(), requestMessage.getUdaMinorVersion()));

      requestMessage.getHeaders().forEach((name, values) -> {
        for (String value : values) {
          request.addHeader(name, value);
        }
      });

      LOGGER.debug("Sending HTTP request: {}", requestMessage);
      Response response = client.newCall(request.build()).execute();
      if (response.isSuccessful()) {
        LOGGER.debug("HTTP response successful: {}", response);
        UpnpResponse responseOperation = new UpnpResponse(response.code(), response.message());

        LOGGER.debug("Received response: {}", responseOperation);
        StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);
        responseMessage.setHeaders(new UpnpHeaders(response.headers().toMultimap()));

        ResponseBody responseBody = response.body();
        if (responseBody != null && responseBody.contentLength() != 0 && responseMessage.isContentTypeMissingOrText()) {
          LOGGER.debug("Response contains textual entity body, converting then setting string on message");
          responseMessage.setBodyCharacters(responseBody.bytes());
        }
        else if (responseBody != null && responseBody.contentLength() > 0) {
          LOGGER.debug("Response contains binary entity body, setting bytes on message");
          responseMessage.setBody(UpnpMessage.BodyType.BYTES, responseBody.bytes());
        }
        else {
          LOGGER.debug("Response did not contain entity body");
        }

        LOGGER.debug("Response message complete: {}", responseMessage);
        return responseMessage;
      }
      else {
        LOGGER.debug("HTTP response failed: {}", response);
        if (response.code() == 401) {
          // forbidden? auth required? we can add this to our blocklist...
          LOGGER.info("We've got a 401 - let's ignore that host from now on! ({})", requestMessage.getUri().getHost());
          Upnp.getInstance().addBlockedIp(requestMessage.getUri().getHost());
        }
      }
    }
    catch (Exception ex) {
      LOGGER.warn("HTTP request failed: {} - {}", requestMessage, ex.getMessage());
    }
    return null;
  }

  @Override
  public void stop() {
    // NOOP
  }
}
