/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.exception;

/**
 * A generic QCT {@link RuntimeException}.
 */
public class QctRuntimeException extends RuntimeException {
  public QctRuntimeException(final String message) {
    super(message);
  }

  public QctRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public QctRuntimeException(final Throwable cause) {
    super(cause);
  }
}
