/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.SerialNumber;
import com.github.aleksikangas.qct.core.meta.decoder.SerialNumberDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see SerialNumber
 * @see SerialNumberDecoder
 */
public final class SerialNumberEncoder {
  public static void encode(final QctWriter qctWriter, final SerialNumber serialNumber, final int byteOffset) {
    Objects.requireNonNull(serialNumber);
    qctWriter.writeBytes(byteOffset, serialNumber.bytes());
  }

  public static void encodeWithPointer(final QctWriter qctWriter,
                                       final SerialNumber serialNumber,
                                       final int byteOffset) {
    final int pointer = qctWriter.allocate(SerialNumber.SIZE);
    qctWriter.writePointer(byteOffset, pointer);
    encode(qctWriter, serialNumber, pointer);
  }

  private SerialNumberEncoder() {
  }
}
