/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export;

public interface ExportListener {
  /**
   * @implNote Not necessarily invoked on EDT.
   */
  void onExport(boolean anyExportTasksInProgress);
}
