/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;


import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

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
  public static final int SIZE = 32;

  public SerialNumber {
    Objects.requireNonNull(bytes);
    Preconditions.checkState(bytes.length == SIZE);
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

  public static final class Decoder {
    public static SerialNumber decode(final QctReader qctReader, final int byteOffset) {
      return new SerialNumber(qctReader.readBytes(byteOffset, SIZE));
    }

    public static SerialNumber decodeFromPointer(final QctReader qctReader, final int byteOffset) {
      final int pointer = qctReader.readPointer(byteOffset);
      return decode(qctReader, pointer);
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final SerialNumber serialNumber, final int byteOffset) {
      Objects.requireNonNull(serialNumber);
      qctWriter.writeBytes(byteOffset, serialNumber.bytes);
    }

    public static void encodeWithPointer(final QctWriter qctWriter,
                                         final SerialNumber serialNumber,
                                         final int byteOffset) {
      final int pointer = qctWriter.allocate(SerialNumber.SIZE);
      qctWriter.writePointer(byteOffset, pointer);
      encode(qctWriter, serialNumber, pointer);
    }

    private Encoder() {
    }
  }
}
