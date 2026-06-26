/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.footer;

import com.github.aleksikangas.qct.ui.export.ExportManager;
import com.github.aleksikangas.qct.ui.utils.ThreadUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public final class Footer extends JPanel {
  private final JLabel progressLabel = new JLabel("Exporting...");
  private final JProgressBar progressBar = new JProgressBar();

  public Footer(final ExportManager exportManager) {
    super(new MigLayout("fill, insets 0", "[][fill, grow]", "[fill]"));
    progressBar.setIndeterminate(true);
    add(progressLabel);
    add(progressBar);
    setExportProgressVisible(false);
    exportManager.addListener(this::setExportProgressVisible);
  }

  private void setExportProgressVisible(final boolean visible) {
    ThreadUtils.runOnEDT(() -> {
      progressLabel.setVisible(visible);
      progressBar.setVisible(visible);
    });
  }
}
