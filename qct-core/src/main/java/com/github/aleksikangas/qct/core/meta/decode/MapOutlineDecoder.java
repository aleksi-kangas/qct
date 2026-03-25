/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.MapOutline;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.FileChannel;

/**
 * A decoder of {@link MapOutline}.
 *
 * @see MapOutline
 */
public final class MapOutlineDecoder {
  public static MapOutline decode(final FileChannel fileChannel, final long byteOffset) {
    final int pointCount = QctReader.readInt(fileChannel, byteOffset);
    final int arrayByteOffset = QctReader.readPointer(fileChannel, byteOffset + 0x04L);
    final MapOutline.Point[] points = new MapOutline.Point[pointCount];
    for (int i = 0; i < pointCount; ++i) {
      final long pointByteOffset = arrayByteOffset + i * (0x08L + 0x08L);
      points[i] = new MapOutline.Point(QctReader.readDouble(fileChannel, pointByteOffset),
                                       QctReader.readDouble(fileChannel, pointByteOffset + 0x08L));
    }
    return new MapOutline(points);
  }

  private MapOutlineDecoder() {
  }
}
