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

package org.tinymediamanager.scraper.http;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.MetadataUtil;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * {@link Interceptor} to handle HTTP 429 (Too Many Requests) responses by respecting the server's {@code Retry-After} header. Requests are retried up
 * to {@value MAX_RETRIES} times to prevent an infinite loop when the server continuously rate-limits the client. If the requested wait time exceeds
 * {@value MAX_RETRY_AFTER_SECONDS} seconds, the retry is aborted immediately and an {@link IOException} is thrown so the caller can surface the
 * rate-limit information. Ensure this is added as an application interceptor (never a network interceptor), otherwise caching will be broken and
 * requests will fail.
 *
 * @author Myron Boyle
 */
public class TmmHttp429RetryInterceptor implements Interceptor {
  private static final Logger LOGGER                  = LoggerFactory.getLogger(TmmHttp429RetryInterceptor.class);
  // maximum number of 429 retries to avoid an infinite loop when the server keeps rate-limiting us
  private static final int    MAX_RETRIES             = 3;
  // default wait time when there is no parsable header
  private static final int    DEFAULT_WAIT            = 10;
  // refuse to wait longer than this many seconds; surface the information as an exception instead
  private static final int    MAX_RETRY_AFTER_SECONDS = 30;

  @NotNull
  public Response intercept(@NotNull Chain chain) throws IOException {
    return handleIntercept(chain, 0);
  }

  /**
   * Handles the intercept with a retry count to prevent infinite retry loops.
   *
   * @param chain
   *          the interceptor chain
   * @param retryCount
   *          the current retry attempt (0-based)
   * @return the {@link Response}
   * @throws IOException
   *           on any I/O error or when the thread is interrupted while waiting to retry
   */
  public Response handleIntercept(Chain chain, int retryCount) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);

    if (!response.isSuccessful() && response.code() == 429) {
      // 429 Too Many Requests - we are being rate-limited

      if (retryCount < MAX_RETRIES) {
        // re-try, but only up to MAX_RETRIES times
        String retryHeader = response.header("Retry-After");
        int retryAfterSeconds;

        if (StringUtils.isNotBlank(retryHeader)) {
          // parse the Retry-After header
          retryAfterSeconds = MetadataUtil.parseInt(retryHeader, DEFAULT_WAIT);
        }
        else {
          // some server may not send this - just wait DEFAULT_WAIT seconds and try again
          retryAfterSeconds = DEFAULT_WAIT;
        }

        try {
          // refuse to wait longer than the configured maximum
          if (retryAfterSeconds > MAX_RETRY_AFTER_SECONDS) {
            LOGGER.debug("Server requested a Retry-After of {} seconds which exceeds the limit of {} seconds - aborting", retryAfterSeconds,
                MAX_RETRY_AFTER_SECONDS);
            if (response.body() != null) {
              response.body().close();
            }
            throw new IOException("HTTP 429: server requested a Retry-After of " + retryAfterSeconds + " seconds, which exceeds the maximum wait of "
                + MAX_RETRY_AFTER_SECONDS + " seconds");
          }

          LOGGER.debug("Hold your horses! The server is asking us to wait {} seconds before retrying (attempt {}/{})", retryAfterSeconds,
              retryCount + 1, MAX_RETRIES);

          // close body of unsuccessful response
          if (response.body() != null) {
            response.body().close();
          }

          // wait the Retry-After and just a little longer
          Thread.sleep((int) ((retryAfterSeconds + 1) * 1000));

          // is fine because, unlike a network interceptor, an application interceptor can re-try requests
          return handleIntercept(chain, retryCount + 1);
        }
        catch (InterruptedException e) {
          // restore the interrupt flag so callers can detect the cancellation, then abort
          Thread.currentThread().interrupt();
          throw new IOException("Interrupted while waiting to retry after HTTP 429", e);
        }
      }
      else {
        LOGGER.debug("Giving up after {} retries due to persistent HTTP 429 rate-limiting", MAX_RETRIES);
      }
    }

    return response;
  }
}
