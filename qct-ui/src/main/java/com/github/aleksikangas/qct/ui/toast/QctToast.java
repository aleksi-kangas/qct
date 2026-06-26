/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.toast;

import raven.modal.Toast;
import raven.modal.toast.option.ToastLocation;

import java.awt.*;
import java.util.Arrays;

public final class QctToast {
  public enum Type {
    SUCCESS,
    INFO,
    WARNING,
    ERROR;

    private Toast.Type toToastType() {
      return switch (this) {
        case SUCCESS -> Toast.Type.SUCCESS;
        case INFO -> Toast.Type.INFO;
        case WARNING -> Toast.Type.WARNING;
        case ERROR -> Toast.Type.ERROR;
      };
    }
  }

  public static void show(final Type type, final String message) {
    Toast.show(getMainFrame(), type.toToastType(), message, ToastLocation.BOTTOM_TRAILING);
  }

  private static Frame getMainFrame() {
    return Arrays.stream(Frame.getFrames()).findFirst().orElseThrow();
  }
}
