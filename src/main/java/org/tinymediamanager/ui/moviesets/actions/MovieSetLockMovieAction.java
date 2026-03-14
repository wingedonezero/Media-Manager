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
package org.tinymediamanager.ui.moviesets.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;

/**
 * The {@link MovieSetLockMovieAction} - to lock a movie against modifications
 * 
 * @author Manuel Laggner
 */
public class MovieSetLockMovieAction extends TmmAction {
  public MovieSetLockMovieAction() {
    putValue(LARGE_ICON_KEY, IconManager.LOCK_BLUE);
    putValue(SMALL_ICON, IconManager.LOCK_BLUE);
    putValue(NAME, TmmResourceBundle.getString("movie.lock"));
    putValue(SHORT_DESCRIPTION, TmmResourceBundle.getString("movie.lock.desc"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Movie> selectedMovies = new ArrayList<>(MovieSetUIModule.getInstance().getSelectionModel().getSelectedMovies(true));

    // filter out dummy movies
    selectedMovies = selectedMovies.stream().filter(movie -> !(movie instanceof MovieSet.MovieSetMovie)).toList();

    if (selectedMovies.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getInstance(), TmmResourceBundle.getString("tmm.nothingselected"));
      return;
    }

    MainWindow.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    for (Movie movie : selectedMovies) {
      movie.setLocked(true);
      movie.saveToDb();
    }
    MainWindow.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }
}
