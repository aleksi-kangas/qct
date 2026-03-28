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
 * +--------+-----------+--------------------------------------------------------+
 * | Offset | Data Type | Content                                                |
 * +--------+-----------+--------------------------------------------------------+
 * | 0x04   | Integer   | File Format Version                                    |
 * |        |           | 0x00000002 – Quick Chart                               |
 * |        |           | 0x00000004 – Quick Chart supporting License Management |
 * |        |           | 0x20000001 – QC3 Format                                |
 * +--------+-----------+--------------------------------------------------------+
 * </pre>
 */
public enum FileFormatVersion {
  QUICK_CHART(0x00000002),
  QUICK_CHART_SUPPORTING_LICENSE_MANAGEMENT(0x00000004),
  QC3(0x20000001);

  private final int value;

  FileFormatVersion(final int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }

  @Override
  public String toString() {
    return switch (this) {
      case QUICK_CHART -> "Quick Chart";
      case QUICK_CHART_SUPPORTING_LICENSE_MANAGEMENT -> "Quick Chart supporting License Management";
      case QC3 -> "QC3 Format";
    };
  }

  public static final class Decoder {
    public static FileFormatVersion decode(final FileChannel fileChannel, final int byteOffset) {
      final int value = QctReader.readInt(fileChannel, byteOffset);
      return Arrays.stream(FileFormatVersion.values())
                   .filter(f -> f.value == value)
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown FileFormatVersion: %d",
                                                                                 value)));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final FileFormatVersion fileFormatVersion,
                              final FileChannel fileChannel,
                              final int byteOffset) {
      Objects.requireNonNull(fileFormatVersion);
      QctWriter.writeInt(fileChannel, byteOffset, fileFormatVersion.value);
    }

    private Encoder() {
    }
  }
}
