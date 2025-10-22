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

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jupnp.model.message.Connection;
import org.jupnp.transport.Router;
import org.jupnp.transport.impl.HttpExchangeUpnpStream;
import org.jupnp.transport.spi.InitializationException;
import org.jupnp.transport.spi.StreamServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class TmmStreamServerImpl implements StreamServer<StreamServerConfigurationImpl> {

  private static final Logger                   LOGGER = LoggerFactory.getLogger(TmmStreamServerImpl.class);

  protected final StreamServerConfigurationImpl configuration;
  protected final ExecutorService               executorService;

  protected HttpServer                          server;

  public TmmStreamServerImpl(StreamServerConfigurationImpl configuration, ExecutorService executorService) {
    this.configuration = configuration;
    this.executorService = executorService;
  }

  synchronized public void init(InetAddress bindAddress, Router router) throws InitializationException {
    try {
      InetSocketAddress socketAddress = new InetSocketAddress(bindAddress, configuration.getListenPort());

      server = HttpServer.create(socketAddress, configuration.getTcpConnectionBacklog());
      server.setExecutor(executorService);
      server.createContext("/upnp/movies", new VideoHttpHandler());
      server.createContext("/upnp/tvshows", new VideoHttpHandler());
      server.createContext("/", new RequestHttpHandler(router));

      LOGGER.info("Created server (for receiving TCP streams) on: {}", server.getAddress());

    }
    catch (Exception ex) {
      // include the cause but avoid unnecessary toString() calls
      throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex.getMessage(), ex);
    }
  }

  synchronized public int getPort() {
    return server.getAddress().getPort();
    // return Upnp.WEBSERVER_PORT;
  }

  public StreamServerConfigurationImpl getConfiguration() {
    return configuration;
  }

  synchronized public void run() {
    LOGGER.info("Starting StreamServer...");
    // Starts a new thread but inherits the properties of the calling thread
    server.start();
  }

  synchronized public void stop() {
    LOGGER.info("Stopping StreamServer...");
    if (server != null) {
      server.stop(1);
    }
  }

  // helper visible to inner classes
  private String getFirstHeader(Map<String, List<String>> headers, String name) {
    List<String> values = headers.get(name);
    return (values != null && !values.isEmpty()) ? values.get(0) : null;
  }

  protected class RequestHttpHandler implements HttpHandler {

    private final Router router;

    public RequestHttpHandler(Router router) {
      this.router = router;
    }

    // This is executed in the request receiving thread!
    public void handle(final HttpExchange httpExchange) throws IOException {
      // And we pass control to the service, which will (hopefully) start a new thread immediately so we can
      // continue the receiving thread ASAP
      LOGGER.trace("Received HTTP exchange: {} {} {}", httpExchange.getRemoteAddress(), httpExchange.getRequestMethod(),
          httpExchange.getRequestURI());
      router.received(new HttpExchangeUpnpStream(router.getProtocolFactory(), httpExchange) {
        @Override
        protected Connection createConnection() {
          return new HttpServerConnection(httpExchange);
        }
      });
    }
  }

  protected class VideoHttpHandler implements HttpHandler {

    public VideoHttpHandler() {

    }

    // This is executed in the request receiving thread!
    public void handle(final HttpExchange httpExchange) throws IOException {
      // And we pass control to the service, which will (hopefully) start a new thread immediately so we can
      // continue the receiving thread ASAP
      LOGGER.trace("Received HTTP exchange: {} {}", httpExchange.getRequestMethod(), httpExchange.getRequestURI());

      // Process request in a separate thread
      new Thread(() -> {
        String uri = httpExchange.getRequestURI().toString();
        LOGGER.debug("Incoming: {} {} {}", httpExchange.getRemoteAddress(), httpExchange.getRequestMethod(), uri);

        if (uri.startsWith("/upnp")) {
          String[] path = StringUtils.split(uri, '/');
          // [0] = upnp
          // [1] = movie|tvshow
          // [2] = UUID of MediaEntity
          // [3] = MF relative path

          if (path.length > 3) {
            try {
              UUID uuid = UUID.fromString(path[2]);
              MediaEntity m = null;
              if ("movies".equals(path[1])) {
                m = MovieModuleManager.getInstance().getMovieList().lookupMovie(uuid);
              }
              else if ("tvshows".equals(path[1])) {
                m = TvShowModuleManager.getInstance().getTvShowList().lookupTvShow(uuid);
              }

              if (m != null) {
                MediaFile mf = new MediaFile();
                mf.setPath(m.getPathNIO().toString());
                String fname = URLDecoder.decode(uri.substring(uri.indexOf(path[2]) + path[2].length() + 1), StandardCharsets.UTF_8);
                String sanitized = FilenameUtils.normalize(fname); // filter path traversal strings
                if (sanitized != null) {
                  mf.setFilename(sanitized);
                  serveMediaFile(mf, httpExchange);
                }
              }
            }
            catch (IllegalArgumentException e) {
              LOGGER.debug("Seems not to be a valid MediaEntity", e);
            }
            catch (Exception e) {
              LOGGER.error("Error serving media file", e);
            }
          }
        }
        else {
          LOGGER.warn("WebServer does not know what to do with this request");
        }

      }).start();
    }

    private void serveMediaFile(MediaFile file, HttpExchange exchange) throws IOException {
      LOGGER.info("Serving: {}", file.getFileAsPath());

      String method = exchange.getRequestMethod();
      Map<String, List<String>> headers = exchange.getRequestHeaders();

      String mime = MimeTypes.getMimeTypeAsString(file.getExtension());
      Path path = file.getFileAsPath();

      long fileLen = Files.size(path);
      String etag = Integer.toHexString((path.toString() + Files.getLastModifiedTime(path) + fileLen).hashCode());

      long startFrom = 0;
      long endAt = fileLen - 1;
      boolean isPartial = false;

      String range = getFirstHeader(headers, "Range");
      if (range != null && range.startsWith("bytes=")) {
        String[] parts = range.substring(6).split("-");
        try {
          startFrom = Long.parseLong(parts[0]);
          if (parts.length > 1 && !parts[1].isEmpty()) {
            endAt = Long.parseLong(parts[1]);
          }
          if (startFrom <= endAt && endAt < fileLen) {
            isPartial = true;
          }
        }
        catch (NumberFormatException ignored) {
          LOGGER.trace("Invalid Range header: {}", range);
        }
      }

      String ifRange = getFirstHeader(headers, "If-Range");
      String ifNoneMatch = getFirstHeader(headers, "If-None-Match");

      boolean ifRangeMatches = (ifRange == null || ifRange.equals(etag));
      boolean ifNoneMatchMatches = (ifNoneMatch != null && (ifNoneMatch.equals("*") || ifNoneMatch.equals(etag)));

      // NOT MODIFIED
      if ((range == null && ifNoneMatchMatches) || (!ifRangeMatches && ifNoneMatchMatches)) {
        sendResponse(exchange, 304, mime, null, 0, etag, false);
        return;
      }

      // RANGE NOT SATISFIABLE - only relevant when a Range header was present
      if (range != null && startFrom >= fileLen) {
        exchange.getResponseHeaders().add("Content-Range", "bytes */" + fileLen);
        sendResponse(exchange, 416, "text/plain", null, 0, etag, false);
        return;
      }

      // HEAD REQUEST
      if ("HEAD".equalsIgnoreCase(method)) {
        int status = isPartial ? 206 : 200;
        long contentLength = isPartial ? (endAt - startFrom + 1) : fileLen;
        if (isPartial) {
          exchange.getResponseHeaders().add("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
        }
        sendResponse(exchange, status, mime, null, contentLength, etag, true);
        return;
      }

      // GET REQUEST
      try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
        raf.seek(startFrom);
        long contentLength = endAt - startFrom + 1;

        if (isPartial) {
          exchange.getResponseHeaders().add("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
          sendResponse(exchange, 206, mime, raf, contentLength, etag, false);
        }
        else {
          sendResponse(exchange, 200, mime, raf, fileLen, etag, false);
        }

      }
      catch (IOException e) {
        // If we get an IOException while preparing or sending the file (e.g. broken pipe because the client closed the connection),
        // don't try to send a second response (headers already sent). Just log and ensure the exchange is closed.
        LOGGER.debug("(UPnP) Error serving file: {}", e.getMessage());
        try {
          exchange.close();
        }
        catch (Exception ex) {
          LOGGER.trace("Error closing exchange after IO failure", ex);
        }
      }
    }

    private void sendResponse(HttpExchange exchange, int status, String mime, RandomAccessFile raf, long contentLength, String etag,
        boolean headOnly) {
      try {
        if (etag != null) {
          exchange.getResponseHeaders().add("ETag", etag);
        }
        exchange.getResponseHeaders().add("Content-Type", mime);
        exchange.getResponseHeaders().add("Accept-Ranges", "bytes");

        if (!headOnly) {
          try {
            exchange.sendResponseHeaders(status, contentLength);
          }
          catch (IOException e) {
            LOGGER.debug("(UPnP) Failed to send response headers: {}", e.getMessage());
            try {
              exchange.close();
            }
            catch (Exception ex) {
              LOGGER.trace("Error closing exchange after header failure", ex);
            }
            return;
          }
        }
        else {
          try {
            exchange.sendResponseHeaders(status, -1);
          }
          catch (IOException e) {
            LOGGER.debug("(UPnP) Failed to send response headers (HEAD): {}", e.getMessage());
            try {
              exchange.close();
            }
            catch (Exception ex) {
              LOGGER.trace("Error closing exchange after HEAD header failure", ex);
            }
            return;
          }
          try {
            exchange.close();
          }
          catch (Exception ex) {
            LOGGER.trace("Error closing exchange after HEAD response", ex);
          }
          return;
        }

        if (raf != null) {
          try (OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[8192];
            long remaining = contentLength;
            while (remaining > 0) {
              int read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining));
              if (read <= 0)
                break;
              os.write(buffer, 0, read);
              remaining -= read;
            }
            os.flush();
          }
          catch (IOException e) {
            LOGGER.debug("(UPnP) IO while writing response body: {}", e.getMessage());
          }
          finally {
            try {
              raf.close();
            }
            catch (IOException ignored) {
              LOGGER.trace("Error closing RandomAccessFile after response", ignored);
            }
          }
        }
        else {
          try (OutputStream os = exchange.getResponseBody()) {
            os.flush();
          }
          catch (IOException e) {
            LOGGER.debug("(UPnP) IO while flushing/closing empty response: {}", e.getMessage());
          }
        }
      }
      finally {
        try {
          exchange.close();
        }
        catch (Exception ignored) {
          LOGGER.trace("Error closing exchange in finally", ignored);
        }
      }
    }

    private String getFirstHeader(Map<String, List<String>> headers, String name) {
      List<String> values = headers.get(name);
      return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
  }

  /**
   * Logs a warning and returns <code>true</code>, we can't access the socket using the awful JDK webserver API.
   * <p>
   * Override this method if you know how to do it.
   * </p>
   */
  protected boolean isConnectionOpen(HttpExchange exchange) {
    LOGGER.warn("Can't check client connection, socket access impossible on JDK webserver!");
    return true;
  }

  protected class HttpServerConnection implements Connection {

    protected HttpExchange exchange;

    public HttpServerConnection(HttpExchange exchange) {
      this.exchange = exchange;
    }

    @Override
    public boolean isOpen() {
      return isConnectionOpen(exchange);
    }

    @Override
    public InetAddress getRemoteAddress() {
      return exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress() : null;
    }

    @Override
    public InetAddress getLocalAddress() {
      return exchange.getLocalAddress() != null ? exchange.getLocalAddress().getAddress() : null;
    }
  }
}
