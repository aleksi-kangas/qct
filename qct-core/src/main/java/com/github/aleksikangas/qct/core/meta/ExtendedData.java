/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * <pre>
 * +--------+-------------------+---------------------------------------------------+
 * | Offset | Data Type         | Content                                           |
 * +--------+-------------------+---------------------------------------------------+
 * | 0x00   | Pointer to String | Map Type                                          |
 * | 0x04   | Pointer to Array  | Datum Shift (see section 6.2.3)                   |
 * | 0x08   | Pointer to String | Disk Name                                         |
 * | 0x0C   | Integer           | Reserved, set to 0                                |
 * | 0x10   | Integer           | Reserved, set to 0                                |
 * | 0x14   | Pointer to Struct | License Information (Optional, see section 6.2.4) |
 * | 0x18   | Pointer to String | Associated Data                                   |
 * | 0x1C   | Pointer to Struct | Digital Map Shop (Optional, see section 6.2.5)    |
 * +--------+-------------------+---------------------------------------------------+
 * </pre>
 */
public record ExtendedData(String mapType,
                           DatumShift datumShift,
                           String diskName,
                           LicenseInformation licenseInformation,
                           String associatedData,
                           DigitalMapShop digitalMapShop) {
  public static final int SIZE = 0x1C + 0x04;

  public ExtendedData {
    Objects.requireNonNull(mapType);
    Objects.requireNonNull(datumShift);
    Objects.requireNonNull(diskName);
    Objects.requireNonNull(licenseInformation);
    Objects.requireNonNull(associatedData);
    Objects.requireNonNull(digitalMapShop);
  }

  public static final class Decoder {
    public static ExtendedData decode(final QctReader qctReader, final int byteOffset) {
      return new ExtendedData(qctReader.readStringFromPointer(byteOffset),
                              DatumShift.Decoder.decodeFromPointer(qctReader, Math.toIntExact(byteOffset + 0x04L)),
                              qctReader.readStringFromPointer(Math.toIntExact(byteOffset + 0x08L)),
                              LicenseInformation.Decoder.decodeFromPointer(qctReader,
                                                                           Math.toIntExact(byteOffset + 0x14L)),
                              qctReader.readStringFromPointer(Math.toIntExact(byteOffset + 0x18L)),
                              DigitalMapShop.Decoder.decodeFromPointer(qctReader, Math.toIntExact(byteOffset + 0x1CL)));
    }

    public static ExtendedData decodeFromPointer(final QctReader qctReader, final int byteOffset) {
      final int pointer = qctReader.readPointer(byteOffset);
      return decode(qctReader, pointer);
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final ExtendedData extendedData, final int byteOffset) {
      Objects.requireNonNull(extendedData);

      qctWriter.allocateWriteString(byteOffset, extendedData.mapType);
      DatumShift.Encoder.encodeWithPointer(qctWriter, extendedData.datumShift, Math.toIntExact(byteOffset + 0x04L));
      qctWriter.allocateWriteString(Math.toIntExact(byteOffset + 0x08L), extendedData.diskName);
      LicenseInformation.Encoder.encodeWithPointer(qctWriter,
                                                   extendedData.licenseInformation,
                                                   Math.toIntExact(byteOffset + 0x14L));
      qctWriter.allocateWriteString(Math.toIntExact(byteOffset + 0x18L), extendedData.associatedData);
      DigitalMapShop.Encoder.encodeWithPointer(qctWriter,
                                               extendedData.digitalMapShop,
                                               Math.toIntExact(byteOffset + 0x1CL));
    }

    public static void encodeWithPointer(final QctWriter qctWriter,
                                         final ExtendedData extendedData,
                                         final int byteOffset) {
      final int pointer = qctWriter.allocate(ExtendedData.SIZE);
      qctWriter.writePointer(byteOffset, pointer);
      encode(qctWriter, extendedData, pointer);
    }

    private Encoder() {
    }
  }
}
