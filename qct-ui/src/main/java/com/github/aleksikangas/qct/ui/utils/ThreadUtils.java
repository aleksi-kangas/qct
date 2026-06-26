/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.utils;

import javax.swing.*;

public final class ThreadUtils {
  public static void runOnEDT(final Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  private ThreadUtils() {
  }
}
