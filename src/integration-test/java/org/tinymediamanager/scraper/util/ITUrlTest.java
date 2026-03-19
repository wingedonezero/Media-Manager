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
package org.tinymediamanager.scraper.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.core.BasicITest;
import org.tinymediamanager.scraper.http.Url;

public class ITUrlTest extends BasicITest {
  @Test
  public void testRobots() throws IOException, InterruptedException {
    Url u = new Url("https://www.google.at/search?q=test");
    String r = IOUtils.toString(u.getInputStream(), StandardCharsets.UTF_8);
    boolean allowed = UrlUtil.isAllowed(u);
    Assert.assertFalse("/search should NOT be allowed, but was - robots.txt parser wrong!", allowed);
  }

}
