/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.SerialNumber;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.AsynchronousFileChannel;

/**
 * A decoder of {@link SerialNumber}.
 *
 * @see SerialNumber
 */
public final class SerialNumberDecoder {
  public static SerialNumber decode(final AsynchronousFileChannel asyncFileChannel, final long byteOffset) {
    return new SerialNumber(QctReader.readBytes(asyncFileChannel, byteOffset, 32));
  }

  private SerialNumberDecoder() {
  }
}
