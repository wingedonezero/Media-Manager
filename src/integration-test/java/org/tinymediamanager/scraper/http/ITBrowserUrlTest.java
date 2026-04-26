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

import org.junit.Test;
import org.tinymediamanager.core.BasicITest;
import org.tinymediamanager.scraper.util.UrlUtil;
import org.tinymediamanager.scraper.util.UrlUtil.UrlImpl;

/**
 * The class {@link ITBrowserUrlTest} validates browser-backed URL fetching.
 *
 * @author Manuel Laggner
 */
public class ITBrowserUrlTest extends BasicITest {

  /**
   * Test parsing a JavaScript-capable URL implementation through {@link UrlUtil}.
   *
   * @throws Exception
   *           if browser navigation fails
   */
  @Test
  public void testGetStringFromUrlViaBrowser() throws Exception {
    String html = UrlUtil.getStringFromUrl("https://example.com", UrlImpl.BROWSER);

    assertThat(html).isNotEmpty();
    assertThat(html.toLowerCase()).contains("example");
  }
}
