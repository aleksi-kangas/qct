/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.async;

import com.github.aleksikangas.qct.convert.png.PngConvertFormatOptions;
import com.github.aleksikangas.qct.convert.png.PngConverter;
import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.ui.export.ExportTask;
import com.github.aleksikangas.qct.ui.toast.QctToast;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class ExportQctFileWorker extends SwingWorker<QctFile, Void> {
  private final ExportTask exportTask;
  private final QctFile qctFile;

  public ExportQctFileWorker(final ExportTask exportTask, final QctFile qctFile) {
    this.exportTask = Objects.requireNonNull(exportTask);
    this.qctFile = Objects.requireNonNull(qctFile);
  }

  @Override
  protected QctFile doInBackground() {
    switch (exportTask.exportFormat()) {
      case PNG -> PngConverter.convertPng(qctFile,
                                          exportTask.exportPath(),
                                          (PngConvertFormatOptions) exportTask.convertFormatOptions());
      default -> throw new UnsupportedOperationException();
    }

    return null;
  }

  @Override
  protected void done() {
    try {
      get();
      QctToast.show(QctToast.Type.SUCCESS, "Export successful");
    } catch (final ExecutionException e) {
      QctToast.show(QctToast.Type.ERROR, String.format("Export failed: %s", e.getMessage()));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      QctToast.show(QctToast.Type.ERROR, String.format("Export interrupted: %s", e.getMessage()));
    }
  }
}
