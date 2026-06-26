/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export.task;

import com.github.aleksikangas.qct.ui.export.ExportFormat;

import java.nio.file.Path;

public interface ExportTask extends Runnable {
  ExportFormat exportFormat();

  Path exportPath();
}
