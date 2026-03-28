/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.FileChannel;
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
  public ExtendedData {
    Objects.requireNonNull(mapType);
    Objects.requireNonNull(datumShift);
    Objects.requireNonNull(diskName);
    Objects.requireNonNull(licenseInformation);
    Objects.requireNonNull(associatedData);
    Objects.requireNonNull(digitalMapShop);
  }

  public static final class Decoder {
    public static ExtendedData decode(final FileChannel fileChannel, final long byteOffset) {
      return new ExtendedData(QctReader.readStringFromPointer(fileChannel, byteOffset),
                              DatumShift.Decoder.decode(fileChannel,
                                                        QctReader.readPointer(fileChannel, byteOffset + 0x04L)),
                              QctReader.readStringFromPointer(fileChannel, byteOffset + 0x08L),
                              LicenseInformation.Decoder.decode(fileChannel,
                                                                QctReader.readPointer(fileChannel, byteOffset + 0x14L)),
                              QctReader.readStringFromPointer(fileChannel, byteOffset + 0x18L),
                              DigitalMapShop.Decoder.decode(fileChannel,
                                                            QctReader.readPointer(fileChannel, byteOffset + 0x1CL)));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final ExtendedData extendedData, final FileChannel fileChannel, final long byteOffset) {
      Objects.requireNonNull(extendedData);
      throw new UnsupportedOperationException("Not implemented");
    }

    private Encoder() {
    }
  }
}
