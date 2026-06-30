/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.ExtendedData;
import com.github.aleksikangas.qct.core.meta.decoder.ExtendedDataDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see ExtendedData
 * @see ExtendedDataDecoder
 */
public final class ExtendedDataEncoder {
  public static void encode(final QctWriter qctWriter, final ExtendedData extendedData, final int byteOffset) {
    Objects.requireNonNull(extendedData);

    qctWriter.allocateWriteString(byteOffset, extendedData.mapType());
    DatumShiftEncoder.encodeWithPointer(qctWriter, extendedData.datumShift(), Math.toIntExact(byteOffset + 0x04L));
    qctWriter.allocateWriteString(Math.toIntExact(byteOffset + 0x08L), extendedData.diskName());
    LicenseInformationEncoder.encodeWithPointer(qctWriter,
                                                extendedData.licenseInformation(),
                                                Math.toIntExact(byteOffset + 0x14L));
    qctWriter.allocateWriteString(Math.toIntExact(byteOffset + 0x18L), extendedData.associatedData());
    DigitalMapShopEncoder.encodeWithPointer(qctWriter,
                                            extendedData.digitalMapShop(),
                                            Math.toIntExact(byteOffset + 0x1CL));
  }

  public static void encodeWithPointer(final QctWriter qctWriter,
                                       final ExtendedData extendedData,
                                       final int byteOffset) {
    final int pointer = qctWriter.allocate(ExtendedData.HEADER_SIZE);
    qctWriter.writePointer(byteOffset, pointer);
    encode(qctWriter, extendedData, pointer);
  }

  private ExtendedDataEncoder() {
  }
}
