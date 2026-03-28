/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Objects;

/**
 * <pre>
 * +--------+-----------+--------------------------------------+
 * | Offset | Data Type | Content                              |
 * +--------+-----------+--------------------------------------+
 * | 0x00   | Integer   | Magic Number                         |
 * |        |           | 0x1423D5FE - Quick Chart Information |
 * |        |           | 0x1423D5FF - Quick Chart Map         |
 * +--------+-----------+--------------------------------------+
 * </pre>
 */
public enum MagicNumber {
  QUICK_CHART_INFORMATION(0x1423D5FE),
  QUICK_CHART_MAP(0x1423D5FF);

  private final int value;

  MagicNumber(final int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }

  @Override
  public String toString() {
    return switch (this) {
      case QUICK_CHART_INFORMATION -> "Quick Chart Information";
      case QUICK_CHART_MAP -> "Quick Chart Map";
    };
  }

  public static final class Decoder {
    public static MagicNumber decode(final FileChannel fileChannel, final long byteOffset) {
      final int value = QctReader.readInt(fileChannel, byteOffset);
      return Arrays.stream(MagicNumber.values())
                   .filter(f -> f.value == value)
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown MagicNumber: %d", value)));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final MagicNumber magicNumber, final FileChannel fileChannel, final long byteOffset) {
      Objects.requireNonNull(magicNumber);
      QctWriter.writeInt(fileChannel, byteOffset, magicNumber.value);
    }

    private Encoder() {
    }
  }
}
