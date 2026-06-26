/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.action;

import com.github.aleksikangas.qct.ui.export.ExportDialog;
import com.github.aleksikangas.qct.ui.model.QctModel;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public final class ExportQctFileAction extends AbstractAction {
  private final QctModel qctModel;
  @Nullable
  private final JComponent parent;

  public ExportQctFileAction(final QctModel qctModel, @Nullable final JComponent parent) {
    super("Export...");
    this.qctModel = Objects.requireNonNull(qctModel);
    this.parent = parent;
    setEnabled(false);

    qctModel.addPropertyChangeListener(e -> {
      if (Objects.equals(e.getPropertyName(), QctModel.QCT_FILE)) {
        setEnabled(e.getNewValue() != null);
      }
    });
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    ExportDialog.showDialog(qctModel, parent);
  }
}
