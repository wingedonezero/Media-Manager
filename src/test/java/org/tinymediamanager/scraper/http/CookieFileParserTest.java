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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.tinymediamanager.core.BasicTest;

public class CookieFileParserTest extends BasicTest {

  @Test
  public void testParseCookieFromHeaderExport() throws Exception {
    Path cookieFile = tmpFolder.newFile("cookies-1.txt").toPath();
    Files.writeString(cookieFile, "session-id=abc123; session-token=token-value; csm-hit=some-value;", StandardCharsets.UTF_8);

    assertThat(CookieFileParser.parseCookieValue(cookieFile, "session-token")).contains("token-value");
  }

  @Test
  public void testParseCookieFromNetscapeExport() throws Exception {
    Path cookieFile = tmpFolder.newFile("cookies-2.txt").toPath();
    Files.writeString(cookieFile, "# Netscape HTTP Cookie File\n" + ".asdf.com\tTRUE\t/\tTRUE\t1811149736\tsession-id\tabc123\n"
        + ".asdf.com\tTRUE\t/\tTRUE\t1811149736\tsession-token\ttoken-value\n", StandardCharsets.UTF_8);

    assertThat(CookieFileParser.parseCookieValue(cookieFile, "session-token")).contains("token-value");
  }

  @Test
  public void testParseCookieFromJsonExport() throws Exception {
    Path cookieFile = tmpFolder.newFile("cookies.json").toPath();
    Files.writeString(cookieFile, "[{\"name\":\"session-id\",\"value\":\"abc123\"},{\"name\":\"session-token\",\"value\":\"token-value\"}]",
        StandardCharsets.UTF_8);

    assertThat(CookieFileParser.parseCookieValue(cookieFile, "session-token")).contains("token-value");
  }

  @Test
  public void testCookieNotFound() throws Exception {
    Path cookieFile = tmpFolder.newFile("cookies-1.txt").toPath();
    Files.writeString(cookieFile, "session-id=abc123", StandardCharsets.UTF_8);

    assertThat(CookieFileParser.parseCookieValue(cookieFile, "session-token")).isEmpty();
  }
}
