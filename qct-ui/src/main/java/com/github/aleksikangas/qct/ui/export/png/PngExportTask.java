/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export.png;

import com.github.aleksikangas.qct.convert.png.PngConvertFormatOptions;
import com.github.aleksikangas.qct.convert.png.PngConverter;
import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.ui.export.ExportFormat;
import com.github.aleksikangas.qct.ui.export.task.AbstractExportTask;

import java.nio.file.Path;
import java.util.Objects;

public final class PngExportTask extends AbstractExportTask {
  private final PngConvertFormatOptions pngConvertFormatOptions;

  public PngExportTask(final Path exportPath,
                       final QctFile qctFile,
                       final PngConvertFormatOptions pngConvertFormatOptions) {
    super(ExportFormat.PNG, exportPath, qctFile);
    this.pngConvertFormatOptions = Objects.requireNonNull(pngConvertFormatOptions);
  }

  @Override
  public void run() {
    PngConverter.convertPng(qctFile, exportPath, pngConvertFormatOptions);
  }
}
