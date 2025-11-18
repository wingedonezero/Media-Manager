package org.tinymediamanager.scraper.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class SimilarityTests {

  @Test
  public void same() {
    test("", "");
    test(null, "");
    test("Batman Begins", "Batman Begins 2");
    test("Batman", "Avatar");
    test("Batman 2", "Avatar 2");
    test("Avatar", "Avatar - Aufbruch nach Pandorra");
    test("Avatar", "Avatar 2");
    test("Avatar", "Avatar (2009)");
    test("Avatar", "Avatar: The Way of Water");
    test("Avatar - Aufbruch nach Pandorra", "Avatar: The Way of Water");

    test("france", "francais");
    test("france", "republic of france");
    test("french republic", "republic of france");
    test("french", "french food");
    test("french", "french cuisine");
  }

  private void test(String s1, String s2) {
    float f1 = Similarity.compareStrings(s1, s2);
    float f2 = AdvancedSimilarity.compareStrings(s1, s2);
    System.out.println(StringUtils.rightPad(s1 + " / " + s2, 70) + StringUtils.rightPad("old: " + f1 + " / new: " + f2, 35) + "  (scraper: "
        + MetadataUtil.calculateScore(s1, s2) + ")");
  }
}
