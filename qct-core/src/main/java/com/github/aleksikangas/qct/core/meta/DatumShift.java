/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.nio.channels.FileChannel;
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
  public static final class Decoder {
    public static DatumShift decode(final FileChannel fileChannel, final int byteOffset) {
      return new DatumShift(QctReader.readDouble(fileChannel, byteOffset),
                            QctReader.readDouble(fileChannel, Math.toIntExact(byteOffset + 0x08L)));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final DatumShift datumShift, final FileChannel fileChannel, final int byteOffset) {
      Objects.requireNonNull(datumShift);
      QctWriter.writeDouble(fileChannel, byteOffset, datumShift.north());
      QctWriter.writeDouble(fileChannel, Math.toIntExact(byteOffset + 0x08L), datumShift.east());
    }

    private Encoder() {
    }
  }
}
