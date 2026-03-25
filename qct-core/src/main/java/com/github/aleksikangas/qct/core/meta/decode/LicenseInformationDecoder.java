/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.LicenseInformation;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.FileChannel;

/**
 * A decoder of {@link LicenseInformation}.
 *
 * @see LicenseInformation
 */
public final class LicenseInformationDecoder {
  public static LicenseInformation decode(final FileChannel fileChannel, final long byteOffset) {
    return new LicenseInformation(QctReader.readInt(fileChannel, byteOffset),
                                  QctReader.readString(fileChannel, byteOffset + 0x0CL),
                                  SerialNumberDecoder.decode(fileChannel,
                                                             QctReader.readPointer(fileChannel, byteOffset + 0x10L)));
  }

  private LicenseInformationDecoder() {
  }
}
