/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * <pre>
 * +--------+-------------------+--------------------------------------------------------------------------------------+
 * | Offset | Data Type         | Content                                                                              |
 * +--------+-------------------+--------------------------------------------------------------------------------------+
 * | 0x00   | Integer           | Identifier of the license.                                                           |
 * |        |                   | This correlates with name of the license file used that must be paired with the map. |
 * | 0x04   | Integer           | Unknown                                                                              |
 * | 0x08   | Integer           | Unknown                                                                              |
 * | 0x0C   | Pointer to String | License description                                                                  |
 * | 0x10   | Pointer to Struct | Serial Number                                                                        |
 * | 0x14   | Integer           | Unknown                                                                              |
 * | 0x18   | 16 Bytes          | Unknown, set to 0                                                                    |
 * | 0x28   | 64 Bytes          | Unknown                                                                              |
 * +--------+-------------------+--------------------------------------------------------------------------------------+
 * </pre>
 */
public record LicenseInformation(int identifier,
                                 SerialNumber serialNumber) {
  public static final int SIZE = 0x28 + 0x64;

  public LicenseInformation {
    Objects.requireNonNull(serialNumber);
  }

  public static final class Decoder {
    public static LicenseInformation decode(final QctReader qctReader, final int byteOffset) {
      return new LicenseInformation(qctReader.readInt(byteOffset),
                                    SerialNumber.Decoder.decodeFromPointer(qctReader,
                                                                           Math.toIntExact(byteOffset + 0x10L)));
    }

    public static LicenseInformation decodeFromPointer(final QctReader qctReader, final int byteOffset) {
      final int pointer = qctReader.readPointer(byteOffset);
      return decode(qctReader, pointer);
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter,
                              final LicenseInformation licenseInformation,
                              final int byteOffset) {
      Objects.requireNonNull(licenseInformation);

      qctWriter.writeInt(byteOffset, licenseInformation.identifier());
      SerialNumber.Encoder.encodeWithPointer(qctWriter,
                                             licenseInformation.serialNumber,
                                             Math.toIntExact(byteOffset + 0x10L));
    }

    public static void encodeWithPointer(final QctWriter qctWriter,
                                         final LicenseInformation licenseInformation,
                                         final int byteOffset) {
      final int pointer = qctWriter.allocate(LicenseInformation.SIZE);
      qctWriter.writePointer(byteOffset, pointer);
      encode(qctWriter, licenseInformation, pointer);
    }

    private Encoder() {
    }
  }
}
