/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * <pre>
 * +--------+-----------+-------------------+
 * | Offset | Data Type | Content           |
 * +--------+-----------+-------------------+
 * | 0x00   | Double    | Datum Shift North |
 * | 0x08   | Double    | Datum Shift East  |
 * +--------+-----------+-------------------+
 * </pre>
 */
public record DatumShift(double north,
                         double east) {
  public static final int SIZE = 0x08 + 0x08;

  public static final class Decoder {
    public static DatumShift decode(final QctReader qctReader, final int byteOffset) {
      return new DatumShift(qctReader.readDouble(byteOffset),
                            qctReader.readDouble(Math.toIntExact(byteOffset + 0x08L)));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final DatumShift datumShift, final int byteOffset) {
      Objects.requireNonNull(datumShift);
      qctWriter.writeDouble(byteOffset, datumShift.north());
      qctWriter.writeDouble(Math.toIntExact(byteOffset + 0x08L), datumShift.east());
    }

    private Encoder() {
    }
  }
}
