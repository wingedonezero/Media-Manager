package org.tinymediamanager.scraper.imdb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.http.OnDiskCachedUrl;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.scraper.imdb.entities.ImdbTitleType;
import org.tinymediamanager.scraper.util.JsonUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ITImdbJsonTest {

  // ****************************************************
  // ****************************************************
  // ****************************************************
  private static final String IMDB_TD = "tt0086190";

  @Test
  public void downloadAllImdbJsonFilesForAboveID() throws Exception {
    for (String sub : SUBS) {
      String json = getJsonFromPage("https://www.imdb.com/de/title/" + IMDB_TD + "/" + sub);
      Path file = saveTo.resolve(this.titleType + "_" + IMDB_TD + "-" + sub + ".json");
      Utils.writeStringToFile(file, json);
    }
    System.out.println();
    System.out.println("***************************************");
    System.out.println("All JSONs saved to " + saveTo.toAbsolutePath());
    System.out.println("***************************************");
  }

  // ****************************************************
  // ****************************************************
  // ****************************************************

  private Path                  saveTo    = Path.of("ignore", "IMDB_json");
  private String                titleType = "";
  private String                title     = "";

  // all found sub paths (FIRST needs to be EMPTY, since this is the main page!)
  private static final String[] SUBS      = new String[] { "", "alternateversions", "awards", "companycredits", "crazycredits", "criticreviews",
      "externalreviews", "externalsites", "faq", "fullcredits", "goofs", "keywords", "locations", "mediaindex", "news", "parentalguide",
      "plotsummary", "quotes", "ratings", "reference", "releaseinfo", "reviews", "soundtrack", "taglines", "technical", "trivia", "videogallery" };

  private String getJsonFromPage(String urlString) throws Exception {
    Url url = new OnDiskCachedUrl(urlString, 7, TimeUnit.DAYS); // forced cache, since IMDB does not allow it
    InputStream is = url.getInputStream();
    Document doc = Jsoup.parse(is, "UTF-8", "");
    Element jsonEl = doc.getElementById("__NEXT_DATA__");
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(jsonEl.data());

    fillGlobalsIfEmpty(mapper, node);

    // console debug
    System.out.println("--------------------------------");
    printStruct(urlString, node, "/props/pageProps/aboveTheFoldData");
    printStruct(urlString, node, "/props/pageProps/mainColumnData");
    printStruct(urlString, node, "/props/pageProps/contentData");
    return node.toPrettyString();
  }

  // just to set our global vars (from first call
  private void fillGlobalsIfEmpty(ObjectMapper mapper, JsonNode node) throws IOException {
    if (titleType.isEmpty()) {
      title = JsonUtils.at(node, "/props/pageProps/aboveTheFoldData/titleText/text").asText();
      title = title.replaceAll("[^a-zA-Z0-9 ._-]", ""); // basic sanitize

      JsonNode ttype = JsonUtils.at(node, "/props/pageProps/aboveTheFoldData/titleType");
      if (ttype != null) {
        ImdbTitleType type = JsonUtils.parseObject(mapper, ttype, ImdbTitleType.class);
        this.titleType = type.id;
        if (type.isEpisode) {
          String parentId = JsonUtils.at(node, "/props/pageProps/aboveTheFoldData/series/series/id").asText();
          String parentTitle = JsonUtils.at(node, "/props/pageProps/aboveTheFoldData/series/series/titleText/text").asText();
          parentTitle = parentTitle.replaceAll("[^a-zA-Z0-9 ._-]", ""); // basic sanitize
          int ep = JsonUtils.at(node, "/props/pageProps/aboveTheFoldData/series/episodeNumber/episodeNumber").asInt();
          int s = JsonUtils.at(node, "/props/pageProps/aboveTheFoldData/series/episodeNumber/seasonNumber").asInt();
          saveTo = saveTo.resolve("tvSeries_" + parentId + "_" + parentTitle); // inject series folder
          saveTo = saveTo.resolve("S" + s + "E" + ep + "_" + IMDB_TD + "_" + title); // create EP folder
        }
        else {
          saveTo = saveTo.resolve(type.id + "_" + IMDB_TD + "_" + title); // create title folder
        }
      }
      else {
        // could not determine title type, use default
        saveTo = saveTo.resolve("unknown_" + IMDB_TD + "_" + title);
      }
      Files.createDirectories(saveTo); // ensure the folder exists
    }
  }

  private void printStruct(String urlString, JsonNode node, String path) {
    JsonNode dbg = JsonUtils.at(node, path);
    if (dbg != null && !dbg.isMissingNode()) {
      Iterator<String> iterator = dbg.fieldNames();
      System.out.println(urlString);
      System.out.println("  " + path);
      iterator.forEachRemaining(e -> System.out.println("    " + e));
    }
  }
}
