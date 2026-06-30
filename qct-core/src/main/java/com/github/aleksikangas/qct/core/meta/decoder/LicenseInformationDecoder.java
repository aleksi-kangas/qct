/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.LicenseInformation;
import com.github.aleksikangas.qct.core.meta.encoder.LicenseInformationEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see LicenseInformation
 * @see LicenseInformationEncoder
 */
public final class LicenseInformationDecoder {
  public static LicenseInformation decode(final QctReader qctReader, final int byteOffset) {
    return new LicenseInformation(qctReader.readInt(byteOffset),
                                  SerialNumberDecoder.decodeFromPointer(qctReader,
                                                                        Math.toIntExact(byteOffset + 0x10L)));
  }

  public static LicenseInformation decodeFromPointer(final QctReader qctReader, final int byteOffset) {
    final int pointer = qctReader.readPointer(byteOffset);
    return decode(qctReader, pointer);
  }

  private LicenseInformationDecoder() {
  }
}
