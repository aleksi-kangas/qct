/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.action;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

public final class BrowseQctFileAction extends AbstractAction {
  private final Consumer<Path> qctFilePathConsumer;
  @Nullable
  private final JComponent parent;

  public BrowseQctFileAction(final Consumer<Path> qctFilePathConsumer, @Nullable final JComponent parent) {
    super("Browse...");
    this.qctFilePathConsumer = Objects.requireNonNull(qctFilePathConsumer);
    this.parent = parent;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final var fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(new FileNameExtensionFilter("QCT file", "qct"));
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
      qctFilePathConsumer.accept(fileChooser.getSelectedFile().toPath());
    }
  }
}
