/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.ExtendedData;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.FileChannel;

/**
 * A decoder of {@link ExtendedData}.
 *
 * @see ExtendedData
 */
public final class ExtendedDataDecoder {
  public static ExtendedData decode(final FileChannel fileChannel, final long byteOffset) {
    return new ExtendedData(QctReader.readStringFromPointer(fileChannel, byteOffset),
                            DatumShiftDecoder.decode(fileChannel,
                                                     QctReader.readPointer(fileChannel, byteOffset + 0x04L)),
                            QctReader.readStringFromPointer(fileChannel, byteOffset + 0x08L),
                            LicenseInformationDecoder.decode(fileChannel,
                                                             QctReader.readPointer(fileChannel, byteOffset + 0x14L)),
                            QctReader.readStringFromPointer(fileChannel, byteOffset + 0x18L),
                            DigitalMapShopDecoder.decode(fileChannel,
                                                         QctReader.readPointer(fileChannel, byteOffset + 0x1CL)));
  }

  private ExtendedDataDecoder() {
  }
}
