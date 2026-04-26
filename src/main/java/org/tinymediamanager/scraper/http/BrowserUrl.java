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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.exceptions.HttpException;
import org.tinymediamanager.scraper.util.Pair;

import okhttp3.Headers;

/**
 * The class {@link BrowserUrl} fetches URL content through a headless browser and returns the rendered DOM as HTML.
 * <p>
 * This implementation mirrors the {@link Url} contract but executes the request in a headless browser to support pages that need JavaScript execution
 * before content extraction.
 * </p>
 *
 * @author Manuel Laggner
 */
public class BrowserUrl extends Url {
  private static final Logger LOGGER                = LoggerFactory.getLogger(BrowserUrl.class);
  private static final int    TIMEOUT_SEC           = 30;
  private static final int    CHALLENGE_TIMEOUT_SEC = 90;
  private static final int    WAIT_POLL_MS          = 250;

  private String              usedUserAgent         = "";

  /**
   * Instantiates a new browser url.
   *
   * @param url
   *          the url to fetch
   * @throws MalformedURLException
   *           if the URL is malformed
   */
  public BrowserUrl(String url) throws MalformedURLException {
    super(url);
  }

  /**
   * Gets the input stream using a headless browser.
   *
   * @return the input stream containing the rendered HTML
   * @throws IOException
   *           Signals that an I/O exception has occurred
   * @throws InterruptedException
   *           Signals that the thread has been interrupted
   */
  @Override
  public InputStream getInputStream() throws IOException, InterruptedException {
    return getInputStream(false);
  }

  /**
   * Gets the input stream using a headless browser.
   *
   * @param headRequest
   *          if {@code true}, only metadata is fetched and an empty stream is returned
   * @return the rendered HTML as stream or an empty stream for HEAD requests
   * @throws IOException
   *           Signals that an I/O exception has occurred
   * @throws InterruptedException
   *           Signals that the thread has been interrupted
   */
  @Override
  public InputStream getInputStream(boolean headRequest) throws IOException, InterruptedException {
    WebDriver browserDriver = null;

    try {
      ChromeDriver chromeDriver = new ChromeDriver(createChromeOptions());
      Map<String, Object> version = chromeDriver.executeCdpCommand("Browser.getVersion", Map.of());
      usedUserAgent = String.valueOf(version.get("userAgent"));

      browserDriver = chromeDriver;
      browserDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(TIMEOUT_SEC));

      browserDriver.get(url);
      waitForPageLoad(browserDriver);

      responseCode = 200;
      responseMessage = "OK";
      responseCharset = StandardCharsets.UTF_8;
      responseContentType = "text/html; charset=UTF-8";
      headersResponse = extractCookieHeaders(browserDriver);

      if (headRequest) {
        responseContentLength = -1;
        return new NullInputStream(0);
      }

      String pageSource = String.valueOf(browserDriver.getPageSource());
      byte[] content = pageSource.getBytes(StandardCharsets.UTF_8);
      responseContentLength = content.length;

      return new ByteArrayInputStream(content);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw e;
    }
    catch (WebDriverException e) {
      responseCode = 0;
      responseMessage = StringUtils.defaultIfBlank(e.getMessage(), "Browser request failed");
      throw new HttpException(url, responseMessage);
    }
    catch (Exception e) {
      responseCode = 0;
      responseMessage = StringUtils.defaultIfBlank(e.getMessage(), "Browser request failed");
      throw new IOException("Could not fetch URL via browser: " + responseMessage, e);
    }
    finally {
      if (browserDriver != null) {
        try {
          browserDriver.quit();
        }
        catch (Exception e) {
          LOGGER.debug("Could not close browser driver", e);
        }
      }
    }
  }

  public String getUsedUserAgent() {
    return usedUserAgent;
  }

  private ChromeOptions createChromeOptions() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--disable-gpu");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-extensions");
    options.addArguments("--disable-popup-blocking");
    options.addArguments("--disable-notifications");
    options.addArguments("--disable-infobars");
    options.addArguments("--disable-blink-features=AutomationControlled");
    options.addArguments("--window-size=1920,1080");

    String userAgent = getUserAgentHeader();
    if (StringUtils.isNotBlank(userAgent)) {
      options.addArguments("--user-agent=" + userAgent);
    }

    return options;
  }

  private String getUserAgentHeader() {
    for (Pair<String, String> header : headersRequest) {
      if (USER_AGENT.equalsIgnoreCase(header.first())) {
        return header.second();
      }
    }
    return null;
  }

  private Headers extractCookieHeaders(WebDriver browserDriver) {
    Headers.Builder builder = new Headers.Builder();

    try {
      // Get all cookies from the page
      String cookies = browserDriver.manage()
          .getCookies()
          .stream()
          .map(cookie -> cookie.getName() + "=" + cookie.getValue())
          .reduce("", (a, b) -> StringUtils.isBlank(a) ? b : a + "; " + b);

      if (StringUtils.isNotBlank(cookies)) {
        builder.add("Cookie", cookies);
      }
    }
    catch (Exception e) {
      LOGGER.debug("Error extracting cookies from page: {}", e.getMessage());
    }

    return builder.build();
  }

  private void waitForPageLoad(WebDriver browserDriver) throws IOException, InterruptedException {
    JavascriptExecutor js = (JavascriptExecutor) browserDriver;

    waitUntil(() -> "complete".equals(js.executeScript("return document.readyState")), Duration.ofSeconds(TIMEOUT_SEC),
        "document ready state to be complete");

    waitForChallengePageToFinish(browserDriver);
  }

  private void waitForChallengePageToFinish(WebDriver browserDriver) throws IOException, InterruptedException {
    if (!isChallengePage(browserDriver)) {
      return;
    }

    final int[] stablePolls = { 0 };

    waitUntil(() -> {
      if (isChallengePage(browserDriver)) {
        stablePolls[0] = 0;
        return false;
      }

      stablePolls[0]++;
      return stablePolls[0] >= 5;
    }, Duration.ofSeconds(CHALLENGE_TIMEOUT_SEC), "challenge page to finish");
  }

  private boolean isChallengePage(WebDriver browserDriver) {
    String currentUrl = String.valueOf(browserDriver.getCurrentUrl()).toLowerCase(Locale.ROOT);
    String title = String.valueOf(browserDriver.getTitle()).toLowerCase(Locale.ROOT);
    String pageSource = String.valueOf(browserDriver.getPageSource()).toLowerCase(Locale.ROOT);

    boolean urlHint = currentUrl.contains("challenge") || currentUrl.contains("interstitial") || currentUrl.contains("captcha")
        || currentUrl.contains("bot");
    boolean titleHint = title.contains("just a moment") || title.contains("checking") || title.contains("verify") || title.contains("captcha");
    boolean sourceHint = pageSource.contains("javascript is disabled") || pageSource.contains("cf-challenge") || pageSource.contains("g-recaptcha")
        || pageSource.contains("hcaptcha") || pageSource.contains("challenge-running") || pageSource.contains("bot challenge");

    return urlHint || titleHint || sourceHint;
  }

  private void waitUntil(BooleanSupplier condition, Duration timeout, String description) throws IOException, InterruptedException {
    Instant deadline = Instant.now().plus(timeout);
    Throwable lastError = null;

    while (Instant.now().isBefore(deadline)) {
      try {
        if (condition.getAsBoolean()) {
          return;
        }
      }
      catch (Exception e) {
        lastError = e;
      }

      Thread.sleep(WAIT_POLL_MS);
    }

    if (lastError != null) {
      throw new IOException("Timed out waiting for " + description + " - " + lastError.getMessage());
    }

    throw new IOException("Timed out waiting for " + description);
  }
}
