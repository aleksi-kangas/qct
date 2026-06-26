/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.action;

import com.github.aleksikangas.qct.ui.model.QctModel;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public final class ConvertQctFileAction extends AbstractAction {
  private final QctModel qctModel;
  @Nullable
  private final JComponent parent;

  public ConvertQctFileAction(final QctModel qctModel, @Nullable final JComponent parent) {
    super("Convert...");
    this.qctModel = Objects.requireNonNull(qctModel);
    this.parent = parent;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    // TODO
  }
}
