/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.DatumShift;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.AsynchronousFileChannel;

/**
 * A decoder of {@link DatumShift}.
 *
 * @see DatumShift
 */
public final class DatumShiftDecoder {
  public static DatumShift decode(final AsynchronousFileChannel asyncFileChannel, final long byteOffset) {
    return new DatumShift(QctReader.readDouble(asyncFileChannel, byteOffset),
                          QctReader.readDouble(asyncFileChannel, byteOffset + 0x08L));
  }

  private DatumShiftDecoder() {
  }
}
