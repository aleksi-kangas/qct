/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export;

import com.github.aleksikangas.qct.ui.export.task.ExportTask;
import com.github.aleksikangas.qct.ui.toast.QctToast;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ThreadSafe
public final class ExportManager {
  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
  private final List<ExportTask> exportTasks = new CopyOnWriteArrayList<>();
  private final Set<ExportListener> exportListeners = new CopyOnWriteArraySet<>();

  public void addListener(final ExportListener exportListener) {
    exportListeners.add(Objects.requireNonNull(exportListener));
  }

  public void removeListener(final ExportListener exportListener) {
    exportListeners.remove(Objects.requireNonNull(exportListener));
  }

  public void export(final ExportTask exportTask) {
    exportTasks.add(exportTask);
    executorService.submit(() -> {
      notifyListeners();
      try {
        exportTask.run();
        QctToast.show(QctToast.Type.SUCCESS,
                      String.format("%s export successful: %s",
                                    exportTask.exportFormat().name(),
                                    exportTask.exportPath()));
      } catch (final Exception e) {
        QctToast.show(QctToast.Type.ERROR, String.format("Export failed: %s", e.getMessage()));
      } finally {
        exportTasks.remove(exportTask);
      }
      notifyListeners();
    });
  }

  private void notifyListeners() {
    exportListeners.forEach(listener -> listener.onExport(!exportTasks.isEmpty()));
  }
}
