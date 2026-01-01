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

package org.tinymediamanager.ui.renderer;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.tinymediamanager.core.RenamerPreviewContainer;
import org.tinymediamanager.ui.IconManager;

/**
 * The class RenamerCellRenderer is used to render the old and new filenames in the renamer dialogs
 *
 * @author Manuel Laggner
 */
public class RenamerCellRenderer extends JTextPane implements TableCellRenderer {
  private final StyledDocument document;
  private final Style          defaultStyle;

  private boolean              forOldFilenames;

  /**
   * Creates a RenamerCellRenderer instance configured for rendering old filenames.
   *
   * @return a RenamerCellRenderer for old filenames
   */
  public static RenamerCellRenderer forOldFilenames() {
    RenamerCellRenderer instance = new RenamerCellRenderer();
    instance.forOldFilenames = true;

    return instance;
  }

  /**
   * Creates a RenamerCellRenderer instance configured for rendering new filenames.
   *
   * @return a RenamerCellRenderer for new filenames
   */
  public static RenamerCellRenderer forNewFilenames() {
    RenamerCellRenderer instance = new RenamerCellRenderer();
    instance.forOldFilenames = false;

    return instance;
  }

  private RenamerCellRenderer() {

    document = getStyledDocument();

    defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof RenamerPreviewContainer.MediaFileTypeContainer container) {
      setText(null);

      if (forOldFilenames) {
        boolean first = true;
        for (String filename : container.oldFiles) {
          try {
            if (!first) {
              document.insertString(document.getLength(), "\n", defaultStyle);
            }

            if (container.newFiles.contains(filename)) {
              insertIcon(IconManager.RENAMER_SPACER);
            }
            else {
              insertIcon(IconManager.RENAMER_REMOVED);
            }

            document.insertString(document.getLength(), "  " + filename, defaultStyle);

            first = false;
          }
          catch (BadLocationException e) {
            throw new RuntimeException(e);
          }
        }
      }
      else {
        boolean first = true;
        for (String filename : container.newFiles) {
          try {
            if (!first) {
              document.insertString(document.getLength(), "\n", defaultStyle);
            }

            if (container.oldFiles.contains(filename)) {
              insertIcon(IconManager.RENAMER_SPACER);
            }
            else {
              insertIcon(IconManager.RENAMER_ADDED);
            }

            document.insertString(document.getLength(), "  " + filename, defaultStyle);

            first = false;
          }
          catch (BadLocationException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    else {
      setText(value != null ? value.toString() : "");
      setFont(table.getFont());
    }

    if (isSelected) {
      setBackground(table.getSelectionBackground());
      setForeground(table.getSelectionForeground());
    }
    else {
      setBackground(table.getBackground());
      setForeground(table.getForeground());
    }

    return this;
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension preferredSize = super.getPreferredSize();

    preferredSize.width += 5; // a bit of extra space

    return preferredSize;
  }
}
