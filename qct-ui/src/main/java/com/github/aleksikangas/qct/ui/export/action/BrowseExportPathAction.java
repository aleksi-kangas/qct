/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export.action;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BrowseExportPathAction extends AbstractAction {
  private final transient Supplier<Optional<Path>> defaultSelectedFileSupplier;
  private final transient Supplier<FileNameExtensionFilter> fileNameExtensionFilterSupplier;
  private final transient Consumer<Path> exportPathConsumer;
  @Nullable
  private final Component parent;

  public BrowseExportPathAction(final Supplier<Optional<Path>> defaultSelectedFileSupplier,
                                final Supplier<FileNameExtensionFilter> fileNameExtensionFilterSupplier,
                                final Consumer<Path> exportPathConsumer,
                                @Nullable final Component parent) {
    super("Browse...");
    this.defaultSelectedFileSupplier = Objects.requireNonNull(defaultSelectedFileSupplier);
    this.fileNameExtensionFilterSupplier = Objects.requireNonNull(fileNameExtensionFilterSupplier);
    this.exportPathConsumer = Objects.requireNonNull(exportPathConsumer);
    this.parent = parent;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final var fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setApproveButtonText("Export");
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setFileFilter(fileNameExtensionFilterSupplier.get());
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    defaultSelectedFileSupplier.get().ifPresent(path -> fileChooser.setSelectedFile(path.toFile()));
    if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
      exportPathConsumer.accept(fileChooser.getSelectedFile().toPath());
    }
  }
}
