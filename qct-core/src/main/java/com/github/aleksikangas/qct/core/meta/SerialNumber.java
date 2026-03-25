/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;


import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

/**
 * <pre>
 * +--------+-----------+---------+
 * | Offset | Data Type | Content |
 * +--------+-----------+---------+
 * | 0x00   | 32 Bytes  | Unknown |
 * +--------+-----------+---------+
 * </pre>
 */
public record SerialNumber(int[] bytes) {
  public SerialNumber {
    Objects.requireNonNull(bytes);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    final SerialNumber that = (SerialNumber) o;
    return Objects.deepEquals(bytes, that.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Nonnull
  @Override
  public String toString() {
    return Arrays.toString(bytes);
  }
}
