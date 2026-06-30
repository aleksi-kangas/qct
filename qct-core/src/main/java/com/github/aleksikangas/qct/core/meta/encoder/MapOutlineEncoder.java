/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.MapOutline;
import com.github.aleksikangas.qct.core.meta.decoder.MapOutlineDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see MapOutline
 * @see MapOutlineDecoder
 */
public final class MapOutlineEncoder {
  public static void encode(final QctWriter qctWriter, final MapOutline mapOutline, final int byteOffset) {
    Objects.requireNonNull(mapOutline);
    Objects.requireNonNull(mapOutline.points());

    final MapOutline.Point[] points = mapOutline.points();
    final int pointCount = points.length;

    qctWriter.writeInt(byteOffset, pointCount);

    final int arrayOffset = Math.toIntExact(byteOffset + 0x08L);
    qctWriter.writePointer(Math.toIntExact(byteOffset + 0x04L), arrayOffset);

    for (int i = 0; i < pointCount; ++i) {
      final MapOutline.Point point = points[i];
      final int pointByteOffset = Math.toIntExact(arrayOffset + i * 16L);

      qctWriter.writeDouble(pointByteOffset, point.latitude());
      qctWriter.writeDouble(Math.toIntExact(pointByteOffset + 0x08L), point.longitude());
    }
  }

  private MapOutlineEncoder() {
  }
}
