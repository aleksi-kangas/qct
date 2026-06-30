/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;


import javax.annotation.Nonnull;
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
}
