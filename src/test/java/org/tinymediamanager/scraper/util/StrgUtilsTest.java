package org.tinymediamanager.scraper.util;

import java.text.ParseException;
import java.util.HexFormat;

import org.apache.commons.text.WordUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.core.BasicTest;

import jakarta.xml.bind.DatatypeConverter;

public class StrgUtilsTest extends BasicTest {

  @Test
  public void testCompareVersion() {
    Assert.assertTrue(StrgUtils.compareVersion("2.7", "2.7-SNAPSHOT") > 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.7-SNAPSHOT", "2.7") < 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.7-SNAPSHOT", "2.7.1") < 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.7.1", "2.7.2-SNAPSHOT") < 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.6.9", "2.7-SNAPSHOT") < 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.7.1-SNAPSHOT", "2.7.2-SNAPSHOT") < 0);

    Assert.assertTrue(StrgUtils.compareVersion("2.7.1", "2.7.1") == 0);
    // Assert.assertTrue(StrgUtils.compareVersion("SVN", "2.7.1") < 0); // dunno how to get actual version for comparison
    Assert.assertTrue(StrgUtils.compareVersion("2.7.1-SNAPSHOT", "2.7.1-SNAPSHOT") < 0); // same snapshot should be considered as lower!
    Assert.assertTrue(StrgUtils.compareVersion("SVN", "SVN") < 0); // dito for SVN
  }

  @Test
  public void hex() {
    // Assert.assertEquals("6162636465666768", StrgUtils.bytesToHex("abcdefgh".getBytes()));
    Assert.assertEquals("6162636465666768", HexFormat.of().formatHex("abcdefgh".getBytes()));
    Assert.assertEquals("6162636465666768", DatatypeConverter.printHexBinary("abcdefgh".getBytes()));

    // AesUtil (lowercase)
    Assert.assertArrayEquals(DatatypeConverter.parseHexBinary("12ab34cd"), HexFormat.of().parseHex("12ab34cd"));

    // leading zero
    Assert.assertEquals("105833", Integer.toHexString(1071155)); // old variant, stripping leading zeros
    Assert.assertEquals("105833", Long.toHexString(1071155L)); // old variant, stripping leading zeros
    Assert.assertEquals("00105833", HexFormat.of().toHexDigits(1071155));
    Assert.assertEquals("00105833", HexFormat.of().toHexDigits(1071155L, 8)); // long shortened to 8 chars
    Assert.assertEquals("0000000000105833", HexFormat.of().toHexDigits(1071155L));

    // HexFormat is lowercase per default
    Assert.assertEquals(HexFormat.of().toHexDigits(15).toUpperCase(), HexFormat.of().withUpperCase().toHexDigits(15));
    Assert.assertEquals("f", Integer.toHexString(15)); // old variant, stripping leading zeros
    Assert.assertEquals("f", Long.toHexString(15L)); // old variant, stripping leading zeros
    Assert.assertEquals("0000000f", HexFormat.of().toHexDigits(15));
    Assert.assertEquals("0000000F", HexFormat.of().withUpperCase().toHexDigits(15));
    Assert.assertEquals("000000000000000f", HexFormat.of().toHexDigits(15L));
  }

  @Test
  public void parseDateTests() throws ParseException {
    Assert.assertNotNull(DateUtils.parseDate("11 Oct. 2001"));
    Assert.assertNotNull(DateUtils.parseDate("11 Okt. 2001"));
    Assert.assertNotNull(DateUtils.parseDate("11 Dic. 2001"));
    Assert.assertNotNull(DateUtils.parseDate("1 Okt. 2001"));
    Assert.assertNotNull(DateUtils.parseDate("01 Okt. 2001"));
    Assert.assertNotNull(DateUtils.parseDate("1. Oktober 2001"));
    Assert.assertNotNull(DateUtils.parseDate("11 Okt..... 2001"));
    Assert.assertNotNull(DateUtils.parseDate("2019-02-12"));
    Assert.assertNotNull(DateUtils.parseDate("12-02-2019"));
    Assert.assertNotNull(DateUtils.parseDate("2019-02-12 15:16"));
    Assert.assertNotNull(DateUtils.parseDate("2019-02-12 15:16:13"));
    Assert.assertNotNull(DateUtils.parseDate("2021-04-21T21:08:22.451Z"));
    Assert.assertNotNull(DateUtils.parseDate("2014-12-25T09:31:55Z"));
  }

  @Test
  public void titleCase() {
    String text = "i'm am FINE | U.N.c.l.e. | iv | gigi d'agostino | part iI | WALL·E (c) | m*a*s*h | F***ed Up | how's going? | rick o'shea | i'll get it";
    char[] delim = new char[] { ' ', '-', '_', '.', '\'', '(', '[', '*' };

    System.out.println(WordUtils.capitalize(text));
    System.out.println(WordUtils.capitalizeFully(text));
    System.out.println(WordUtils.capitalize(text, delim));
    System.out.println(WordUtils.capitalizeFully(text, delim));

    System.out.println("TMM impl");
    System.out.println(StrgUtils.capitalize(text));
  }
}
