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
package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.PostProcessExecutionResult;
import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.ui.components.NoBorderScrollPane;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link PostProcessResultDialog} displays the collected output of a post-processing execution.
 *
 * @author Manuel Laggner
 */
public class PostProcessResultDialog extends TmmDialog {
  private final JScrollPane scrollPane = new NoBorderScrollPane();

  /**
   * Creates a new dialog showing command and output for each executed post-process call.
   *
   * @param processName
   *          the name of the post-process action
   * @param executionResults
   *          the list of collected execution results
   */
  public PostProcessResultDialog(String processName, List<PostProcessExecutionResult> executionResults) {
    super(TmmResourceBundle.getString("postprocessing.result.title") + " - " + processName, "postProcessResult");
    setBounds(5, 5, 1000, 620);

    JPanel panelContent = new JPanel(new MigLayout("", "[grow]", "[grow]"));
    getContentPane().add(panelContent, BorderLayout.CENTER);

    JPanel panelResults = new JPanel(new MigLayout("insets 0,wrap", "[grow]", ""));

    for (PostProcessExecutionResult executionResult : executionResults) {
      panelResults.add(createResultPanel(executionResult), "growx");
    }

    scrollPane.setViewportView(panelResults);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    panelContent.add(scrollPane, "cell 0 0,grow");

    JButton btnClose = new JButton(TmmResourceBundle.getString("Button.close"));
    btnClose.addActionListener(arg0 -> setVisible(false));
    addDefaultButton(btnClose);
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      // For modal dialogs, schedule this before super.setVisible(true) because that call blocks.
      SwingUtilities.invokeLater(() -> {
        scrollPane.getViewport().setViewPosition(new Point(0, 0));
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getHorizontalScrollBar().setValue(0);
      });
    }
    super.setVisible(visible);
  }

  /**
   * Builds one visual entry for a single command execution.
   *
   * @param executionResult
   *          the execution result to render
   * @return the panel showing command and output
   */
  private JPanel createResultPanel(PostProcessExecutionResult executionResult) {
    JPanel panelResult = new JPanel(new MigLayout("insets dialog", "[grow]", "[][][][grow]"));
    panelResult.setBorder(BorderFactory.createTitledBorder(executionResult.entityName()));

    panelResult.add(new JLabel(TmmResourceBundle.getString("postprocessing.result.command")), "cell 0 0");

    JTextArea taCommand = createTextArea(false);
    taCommand.setText(String.join(" ", executionResult.command()));
    panelResult.add(taCommand, "cell 0 1,growx");

    panelResult.add(new JLabel(TmmResourceBundle.getString("postprocessing.result.output")), "cell 0 2");

    JTextArea taOutput = createTextArea(true);
    if (StringUtils.isBlank(executionResult.output())) {
      taOutput.setText(TmmResourceBundle.getString("postprocessing.result.nooutput"));
    }
    else {
      taOutput.setText(normalizeLineBreaks(executionResult.output()));
    }
    panelResult.add(taOutput, "cell 0 3,grow, hmin 90lp");

    return panelResult;
  }

  /**
   * Creates a non-editable text area with the dialog defaults.
   *
   * @param lineWrap
   *          {@code true} to enable line wrapping; {@code false} otherwise
   * @return the configured text area
   */
  private JTextArea createTextArea(boolean lineWrap) {
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setOpaque(false);
    textArea.setWrapStyleWord(lineWrap);
    textArea.setLineWrap(lineWrap);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, textArea.getFont().getSize()));
    textArea.setCaretPosition(0);
    return textArea;
  }

  /**
   * Normalizes line endings from different operating systems for predictable rendering in Swing text areas.
   *
   * @param text
   *          the raw process output
   * @return output with Unix line endings
   */
  private String normalizeLineBreaks(String text) {
    return text.replace("\r\n", "\n").replace('\r', '\n');
  }
}
