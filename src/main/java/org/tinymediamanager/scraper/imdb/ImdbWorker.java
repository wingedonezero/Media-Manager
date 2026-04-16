package org.tinymediamanager.scraper.imdb;

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.exceptions.HttpException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.http.InMemoryCachedUrl;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.scraper.util.UrlUtil;

class ImdbWorker implements Callable<Document> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImdbWorker.class);

  protected final String      pageUrl;
  protected final String      language;
  protected final String      country;
  protected final boolean     useCachedUrl;

  ImdbWorker(String url, String language, String country) {
    this(url, language, country, true);
  }

  ImdbWorker(String url, String language, String country, boolean useCachedUrl) {
    this.pageUrl = url;
    this.language = language;
    this.country = country;
    this.useCachedUrl = useCachedUrl;
  }

  @Override
  public Document call() throws Exception {
    Document doc = null;

    Url url;

    try {
      // inject language into the url for correct caching
      String urlWithHeader = this.pageUrl + "|Accept-Language=" + getAcceptLanguage(language, country);
      if (useCachedUrl) {
        url = new InMemoryCachedUrl(urlWithHeader);
      }
      else {
        url = new Url(urlWithHeader);
      }
      // url.addHeader("Accept-Language", getAcceptLanguage(language, country));
    }
    catch (Exception e) {
      LOGGER.debug("tried to fetch imdb page {} - {}", this.pageUrl, e.getMessage());
      throw new ScrapeException(e);
    }

    try (InputStream is = url.getInputStream()) {
      if (url.getStatusCode() == 202) {
        // 202 indicates that the WAF is active
        throw new HttpException(202, "Request blocked - WAF active");
      }
      doc = Jsoup.parse(is, "UTF-8", "");
    }
    catch (InterruptedException | InterruptedIOException e) {
      // do not swallow these Exceptions
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      LOGGER.debug("tried to fetch imdb page {} - {}", this.pageUrl, e.getMessage());
      throw e;
    }

    return doc;
  }

  /**
   * generates the accept-language http header for imdb
   *
   * @param language
   *          the language code to be used
   * @param country
   *          the country to be used
   * @return the Accept-Language string
   */
  protected static String getAcceptLanguage(String language, String country) {
    List<String> languageString = new ArrayList<>();

    // first: take the preferred language from settings,
    // but validate whether it is legal or not
    if (StringUtils.isNotBlank(language) && StringUtils.isNotBlank(country) && LocaleUtils.isAvailableLocale(new Locale(language, country))) {
      String combined = language + "-" + country;
      languageString.add(combined);
    }

    // also build langu & default country
    Locale localeFromLanguage = UrlUtil.getLocaleFromLanguage(language);
    String cFromLang = localeFromLanguage.getCountry();
    if (localeFromLanguage != null) {
      String combined = language + (cFromLang.isEmpty() ? "" : "-" + cFromLang);
      if (!languageString.contains(combined)) {
        languageString.add(combined);
      }
    }

    if (StringUtils.isNotBlank(language)) {
      languageString.add(language);
    }

    // second: the JRE language
    Locale jreLocale = Locale.getDefault();
    String jreCountry = jreLocale.getCountry();
    String combined = jreLocale.getLanguage() + (jreCountry.isEmpty() ? "" : "-" + jreCountry);
    if (!languageString.contains(combined)) {
      languageString.add(combined);
    }
    if (!languageString.contains(jreLocale.getLanguage())) {
      languageString.add(jreLocale.getLanguage());
    }

    // third: fallback to en
    if (!languageString.contains("en-US")) {
      languageString.add("en-US");
    }
    if (!languageString.contains("en")) {
      languageString.add("en");
    }

    // build a http header for the preferred language
    StringBuilder languages = new StringBuilder();
    float qualifier = 1f;

    for (String line : languageString) {
      if (languages.length() > 0) {
        languages.append(",");
      }
      languages.append(line);
      if (qualifier < 1) {
        languages.append(String.format(Locale.US, ";q=%1.1f", qualifier));
      }
      qualifier -= 0.1;
    }

    return languages.toString();
  }
}
