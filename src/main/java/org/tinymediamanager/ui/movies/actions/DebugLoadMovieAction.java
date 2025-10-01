/*
 * Copyright 2012 - 2025 Manuel Laggner
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
package org.tinymediamanager.ui.movies.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.actions.TmmAction;

public class DebugLoadMovieAction extends TmmAction {
  private static final Logger LOGGER = LoggerFactory.getLogger(DebugLoadMovieAction.class);

  public DebugLoadMovieAction() {
    putValue(NAME, TmmResourceBundle.getString("debug.entity.load"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    Path file = TmmUIHelper.selectFile(TmmResourceBundle.getString("debug.entity.load"), ".",
        new FileNameExtensionFilter("User submitted JSON files", ".json"));
    if (file != null && Utils.isRegularFile(file)) {
      try {
        String json = Utils.readFileToString(file);
        MovieModuleManager.getInstance().load(json);
      }
      catch (IOException io) {
        LOGGER.error("Error loading movie into DB: {}", io);
      }
    }
  }
}
