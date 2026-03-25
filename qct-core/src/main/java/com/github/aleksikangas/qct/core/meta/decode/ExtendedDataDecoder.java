/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.ExtendedData;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.AsynchronousFileChannel;

/**
 * A decoder of {@link ExtendedData}.
 *
 * @see ExtendedData
 */
public final class ExtendedDataDecoder {
  public static ExtendedData decode(final AsynchronousFileChannel asyncFileChannel, final long byteOffset) {
    return new ExtendedData(QctReader.readStringFromPointer(asyncFileChannel, byteOffset),
                            DatumShiftDecoder.decode(asyncFileChannel,
                                                     QctReader.readPointer(asyncFileChannel, byteOffset + 0x04L)),
                            QctReader.readStringFromPointer(asyncFileChannel, byteOffset + 0x08L),
                            LicenseInformationDecoder.decode(asyncFileChannel,
                                                             QctReader.readPointer(asyncFileChannel,
                                                                                   byteOffset + 0x14L)),
                            QctReader.readStringFromPointer(asyncFileChannel, byteOffset + 0x18L),
                            DigitalMapShopDecoder.decode(asyncFileChannel,
                                                         QctReader.readPointer(asyncFileChannel, byteOffset + 0x1CL)));
  }

  private ExtendedDataDecoder() {
  }
}
