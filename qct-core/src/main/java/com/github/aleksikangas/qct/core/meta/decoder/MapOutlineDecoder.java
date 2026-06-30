/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.MapOutline;
import com.github.aleksikangas.qct.core.meta.encoder.MapOutlineEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see MapOutline
 * @see MapOutlineEncoder
 */
public final class MapOutlineDecoder {
  public static MapOutline decode(final QctReader qctReader, final int byteOffset) {
    final int pointCount = qctReader.readInt(byteOffset);
    final int arrayByteOffset = qctReader.readPointer(Math.toIntExact(byteOffset + 0x04L));
    final MapOutline.Point[] points = new MapOutline.Point[pointCount];
    for (int i = 0; i < pointCount; ++i) {
      final int pointByteOffset = Math.toIntExact(arrayByteOffset + i * (0x08L + 0x08L));
      points[i] = new MapOutline.Point(qctReader.readDouble(pointByteOffset),
                                       qctReader.readDouble(Math.toIntExact(pointByteOffset + 0x08L)));
    }
    return new MapOutline(points);
  }

  private MapOutlineDecoder() {
  }
}
