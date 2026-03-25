/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.DatumShift;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.FileChannel;

/**
 * A decoder of {@link DatumShift}.
 *
 * @see DatumShift
 */
public final class DatumShiftDecoder {
  public static DatumShift decode(final FileChannel fileChannel, final long byteOffset) {
    return new DatumShift(QctReader.readDouble(fileChannel, byteOffset),
                          QctReader.readDouble(fileChannel, byteOffset + 0x08L));
  }

  private DatumShiftDecoder() {
  }
}
