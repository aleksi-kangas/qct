/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export.task;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.ui.export.ExportFormat;

import java.nio.file.Path;
import java.util.Objects;

public abstract class AbstractExportTask implements ExportTask {
  protected final ExportFormat exportFormat;
  protected final Path exportPath;
  protected final QctFile qctFile;

  protected AbstractExportTask(final ExportFormat exportFormat, final Path exportPath, final QctFile qctFile) {
    this.exportFormat = Objects.requireNonNull(exportFormat);
    this.exportPath = Objects.requireNonNull(exportPath);
    this.qctFile = Objects.requireNonNull(qctFile);
  }

  @Override
  public final ExportFormat exportFormat() {
    return exportFormat;
  }

  @Override
  public final Path exportPath() {
    return exportPath;
  }
}
