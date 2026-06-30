/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.LicenseInformation;
import com.github.aleksikangas.qct.core.meta.decoder.LicenseInformationDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see LicenseInformation
 * @see LicenseInformationDecoder
 */
public final class LicenseInformationEncoder {
  public static void encode(final QctWriter qctWriter,
                            final LicenseInformation licenseInformation,
                            final int byteOffset) {
    Objects.requireNonNull(licenseInformation);

    qctWriter.writeInt(byteOffset, licenseInformation.identifier());
    SerialNumberEncoder.encodeWithPointer(qctWriter,
                                          licenseInformation.serialNumber(),
                                          Math.toIntExact(byteOffset + 0x10L));
  }

  public static void encodeWithPointer(final QctWriter qctWriter,
                                       final LicenseInformation licenseInformation,
                                       final int byteOffset) {
    final int pointer = qctWriter.allocate(LicenseInformation.HEADER_SIZE);
    qctWriter.writePointer(byteOffset, pointer);
    encode(qctWriter, licenseInformation, pointer);
  }

  private LicenseInformationEncoder() {
  }
}
