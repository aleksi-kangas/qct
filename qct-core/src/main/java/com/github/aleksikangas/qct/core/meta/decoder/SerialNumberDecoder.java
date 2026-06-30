/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.SerialNumber;
import com.github.aleksikangas.qct.core.meta.encoder.SerialNumberEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see SerialNumber
 * @see SerialNumberEncoder
 */
public final class SerialNumberDecoder {
  public static SerialNumber decode(final QctReader qctReader, final int byteOffset) {
    return new SerialNumber(qctReader.readBytes(byteOffset, SerialNumber.SIZE));
  }

  public static SerialNumber decodeFromPointer(final QctReader qctReader, final int byteOffset) {
    final int pointer = qctReader.readPointer(byteOffset);
    return decode(qctReader, pointer);
  }

  private SerialNumberDecoder() {
  }
}
