/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.ExtendedData;
import com.github.aleksikangas.qct.core.meta.encoder.ExtendedDataEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see ExtendedData
 * @see ExtendedDataEncoder
 */
public final class ExtendedDataDecoder {
  public static ExtendedData decode(final QctReader qctReader, final int byteOffset) {
    return new ExtendedData(qctReader.readStringFromPointer(byteOffset),
                            DatumShiftDecoder.decodeFromPointer(qctReader, Math.toIntExact(byteOffset + 0x04L)),
                            qctReader.readStringFromPointer(Math.toIntExact(byteOffset + 0x08L)),
                            LicenseInformationDecoder.decodeFromPointer(qctReader, Math.toIntExact(byteOffset + 0x14L)),
                            qctReader.readStringFromPointer(Math.toIntExact(byteOffset + 0x18L)),
                            DigitalMapShopDecoder.decodeFromPointer(qctReader, Math.toIntExact(byteOffset + 0x1CL)));
  }

  public static ExtendedData decodeFromPointer(final QctReader qctReader, final int byteOffset) {
    final int pointer = qctReader.readPointer(byteOffset);
    return decode(qctReader, pointer);
  }

  private ExtendedDataDecoder() {
  }
}
