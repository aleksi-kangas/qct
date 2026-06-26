/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.header.action;

import com.github.aleksikangas.qct.ui.export.ExportDialog;
import com.github.aleksikangas.qct.ui.export.ExportManager;
import com.github.aleksikangas.qct.ui.file.QctFileManager;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public final class ExportQctFileAction extends AbstractAction {
  private final transient ExportManager exportManager;
  private final transient QctFileManager qctFileManager;
  @Nullable
  private final JComponent parent;

  public ExportQctFileAction(final ExportManager exportManager,
                             final QctFileManager qctFileManager,
                             @Nullable final JComponent parent) {
    super("Export...");
    this.exportManager = Objects.requireNonNull(exportManager);
    this.qctFileManager = Objects.requireNonNull(qctFileManager);
    this.parent = parent;
    setEnabled(false);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    ExportDialog.showDialog(exportManager, qctFileManager, parent);
  }
}
