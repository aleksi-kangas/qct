/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

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
  @SuppressWarnings("java:S1845")
  public static final int HEADER_SIZE = 0x04 + 0x04;

  public DigitalMapShop {
    Objects.requireNonNull(qc3Url);
  }

  public static final class Decoder {
    public static DigitalMapShop decode(final QctReader qctReader, final int byteOffset) {
      return new DigitalMapShop(qctReader.readInt(byteOffset),
                                qctReader.readStringFromPointer(Math.toIntExact(byteOffset + 0x04L)));
    }

    public static DigitalMapShop decodeFromPointer(final QctReader qctReader, final int byteOffset) {
      final int pointer = qctReader.readPointer(byteOffset);
      return decode(qctReader, pointer);
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final DigitalMapShop digitalMapShop, final int byteOffset) {
      Objects.requireNonNull(digitalMapShop);

      qctWriter.writeInt(byteOffset, digitalMapShop.size);
      qctWriter.allocateWriteString(Math.toIntExact(byteOffset + 0x04L), digitalMapShop.qc3Url);
    }

    public static void encodeWithPointer(final QctWriter qctWriter,
                                         final DigitalMapShop digitalMapShop,
                                         final int byteOffset) {
      final int pointer = qctWriter.allocate(DigitalMapShop.HEADER_SIZE);
      qctWriter.writePointer(byteOffset, pointer);
      encode(qctWriter, digitalMapShop, pointer);
    }

    private Encoder() {
    }
  }
}
