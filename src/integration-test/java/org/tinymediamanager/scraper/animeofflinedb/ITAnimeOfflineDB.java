package org.tinymediamanager.scraper.animeofflinedb;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ITAnimeOfflineDB {

  @Test
  public void testIdLookup() {
    AnimeOfflineDb dataset = AnimeOfflineDb.getInstance();

    Map<String, Object> map = dataset.getIdsFor("anidb", "11649xxxx");
    Assert.assertTrue(map.isEmpty());

    map = dataset.getIdsFor("animeplanet", "o-sawako-kabuki");
    Assert.assertFalse(map.isEmpty());
    System.out.println(map);
  }
}
