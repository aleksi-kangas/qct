/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.interpolation;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

/**
 * <pre>
 * +--------+---------------+---------------------+
 * | Offset | Size (Bytes) | Content              |
 * +--------+--------------+----------------------+
 * | 0x05A0 | 128 x 128    | Interpolation Matrix |
 * +--------+--------------+----------------------+
 *
 * +--------+-----------+---------------------------+
 * | Offset | Data Type | Content                   |
 * +--------+-----------+---------------------------+
 * | 0x0000 | Byte      | Precalculated Color Index |
 * | 0x0001 | Byte      | Precalculated Color Index |
 * | ...    | ...       | ...                       |
 * | 0x4000 | Byte      | Precalculated Color Index |
 * +--------+-----------+---------------------------+
 * </pre>
 * The offset of any given row/column index into the matrix is given by: {@code offset = (128 x y) + x}. Due to
 * symmetry, {@code y} and {@code x} are interchangeable.
 */
public record InterpolationMatrix(int[] indices) {
  public static final int BYTE_OFFSET = 0x05A0;
  public static final int SIZE_COLUMNS = 128;
  public static final int SIZE_ROWS = 128;
  public static final int SIZE = SIZE_COLUMNS * SIZE_ROWS;

  public InterpolationMatrix {
    Objects.requireNonNull(indices);
    Preconditions.checkState(indices.length == SIZE);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    final InterpolationMatrix that = (InterpolationMatrix) o;
    return Objects.deepEquals(indices, that.indices);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(indices);
  }

  @Nonnull
  @Override
  public String toString() {
    return Arrays.toString(indices);
  }

  public static int offsetOf(final int y, final int x) {
    Objects.checkIndex(y, SIZE_ROWS);
    Objects.checkIndex(x, SIZE_COLUMNS);
    return (128 * y) + x;
  }

  public int paletteIndexOf(final int yColorIndex, final int xColorIndex) {
    return indices[offsetOf(yColorIndex, xColorIndex)];
  }

  public static final class Decoder {
    public static InterpolationMatrix decode(final QctReader qctReader) {
      return new InterpolationMatrix(qctReader.readBytes(InterpolationMatrix.BYTE_OFFSET, InterpolationMatrix.SIZE));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final InterpolationMatrix interpolationMatrix) {
      qctWriter.writeBytes(InterpolationMatrix.BYTE_OFFSET, interpolationMatrix.indices);
    }

    private Encoder() {
    }
  }
}
