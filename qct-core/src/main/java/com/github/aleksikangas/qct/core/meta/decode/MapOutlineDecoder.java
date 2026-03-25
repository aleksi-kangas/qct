/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.MapOutline;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.AsynchronousFileChannel;

/**
 * A decoder of {@link MapOutline}.
 *
 * @see MapOutline
 */
public final class MapOutlineDecoder {
  public static MapOutline decode(final AsynchronousFileChannel asyncFileChannel, final long byteOffset) {
    final int pointCount = QctReader.readInt(asyncFileChannel, byteOffset);
    final int arrayByteOffset = QctReader.readPointer(asyncFileChannel, byteOffset + 0x04L);
    final MapOutline.Point[] points = new MapOutline.Point[pointCount];
    for (int i = 0; i < pointCount; ++i) {
      final long pointByteOffset = arrayByteOffset + i * (0x08L + 0x08L);
      points[i] = new MapOutline.Point(QctReader.readDouble(asyncFileChannel, pointByteOffset),
                                       QctReader.readDouble(asyncFileChannel, pointByteOffset + 0x08L));
    }
    return new MapOutline(points);
  }

  private MapOutlineDecoder() {
  }
}
