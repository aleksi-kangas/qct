/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;


import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import javax.annotation.Nonnull;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Objects;

/**
 * <pre>
 * +--------+-----------+-----------+
 * | Offset | Data Type | Content   |
 * +--------+-----------+-----------+
 * | 0x00   | Double    | Latitude  |
 * | 0x08   | Double    | Longitude |
 * | ...    | ...       | ...       |
 * +--------+-----------+-----------+
 * </pre>
 */
public record MapOutline(Point[] points) {
  public MapOutline {
    Objects.requireNonNull(points);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    final MapOutline that = (MapOutline) o;
    return Objects.deepEquals(points, that.points);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(points);
  }

  @Nonnull
  @Override
  public String toString() {
    return Arrays.toString(points);
  }

  public record Point(double latitude,
                      double longitude) {
  }

  public static final class Decoder {
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

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final MapOutline mapOutline, final FileChannel fileChannel, final long byteOffset) {
      Objects.requireNonNull(mapOutline);
      Objects.requireNonNull(mapOutline.points());

      final Point[] points = mapOutline.points();
      final int pointCount = points.length;

      QctWriter.writeInt(fileChannel, byteOffset, pointCount);

      final long arrayOffset = byteOffset + 0x08L;
      QctWriter.writePointer(fileChannel, byteOffset + 0x04L, (int) arrayOffset);

      for (int i = 0; i < pointCount; ++i) {
        final Point point = points[i];
        final long pointByteOffset = arrayOffset + i * 16L;

        QctWriter.writeDouble(fileChannel, pointByteOffset, point.latitude());
        QctWriter.writeDouble(fileChannel, pointByteOffset + 0x08L, point.longitude());
      }
    }

    private Encoder() {
    }
  }
}
