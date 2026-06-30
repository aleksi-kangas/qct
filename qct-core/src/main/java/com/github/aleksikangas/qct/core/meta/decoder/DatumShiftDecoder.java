/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.DatumShift;
import com.github.aleksikangas.qct.core.meta.encoder.DatumShiftEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see DatumShift
 * @see DatumShiftEncoder
 */
public final class DatumShiftDecoder {
  public static DatumShift decode(final QctReader qctReader, final int byteOffset) {
    return new DatumShift(qctReader.readDouble(byteOffset), qctReader.readDouble(Math.toIntExact(byteOffset + 0x08L)));
  }

  public static DatumShift decodeFromPointer(final QctReader qctReader, final int byteOffset) {
    final int pointer = qctReader.readPointer(byteOffset);
    return decode(qctReader, pointer);
  }

  private DatumShiftDecoder() {
  }
}
