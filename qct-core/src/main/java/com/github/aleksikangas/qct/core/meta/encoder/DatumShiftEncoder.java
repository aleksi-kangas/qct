/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.DatumShift;
import com.github.aleksikangas.qct.core.meta.decoder.DatumShiftDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see DatumShift
 * @see DatumShiftDecoder
 */
public final class DatumShiftEncoder {
  public static void encode(final QctWriter qctWriter, final DatumShift datumShift, final int byteOffset) {
    Objects.requireNonNull(datumShift);
    qctWriter.writeDouble(byteOffset, datumShift.north());
    qctWriter.writeDouble(Math.toIntExact(byteOffset + 0x08L), datumShift.east());
  }

  public static void encodeWithPointer(final QctWriter qctWriter, final DatumShift datumShift, final int byteOffset) {
    final int pointer = qctWriter.allocate(DatumShift.SIZE);
    qctWriter.writePointer(byteOffset, pointer);
    encode(qctWriter, datumShift, pointer);
  }

  private DatumShiftEncoder() {
  }
}
