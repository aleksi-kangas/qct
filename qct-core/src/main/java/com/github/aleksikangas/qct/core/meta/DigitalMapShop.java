/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * <pre>
 * +--------+-------------------+-----------------------------+
 * | Offset | Data Type         | Content                     |
 * +--------+-------------------+-----------------------------+
 * | 0x00   | Integer           | Structure size, set to 8    |
 * | 0x04   | Pointer to String | Partial URL to QC3 map file |
 * +--------+-------------------+-----------------------------+
 * </pre>
 */
public record DigitalMapShop(int size,
                             String qc3Url) {
  public DigitalMapShop {
    Objects.requireNonNull(qc3Url);
  }

  public static final class Decoder {
    public static DigitalMapShop decode(final FileChannel fileChannel, final long byteOffset) {
      return new DigitalMapShop(QctReader.readInt(fileChannel, byteOffset),
                                QctReader.readStringFromPointer(fileChannel, byteOffset + 0x04L));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final DigitalMapShop digitalMapShop,
                              final FileChannel fileChannel,
                              final long byteOffset) {
      Objects.requireNonNull(digitalMapShop);
      throw new UnsupportedOperationException("Not implemented");
    }

    private Encoder() {
    }
  }
}
