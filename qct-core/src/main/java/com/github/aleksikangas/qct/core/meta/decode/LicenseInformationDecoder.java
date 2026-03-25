/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.LicenseInformation;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.AsynchronousFileChannel;

/**
 * A decoder of {@link LicenseInformation}.
 *
 * @see LicenseInformation
 */
public final class LicenseInformationDecoder {
  public static LicenseInformation decode(final AsynchronousFileChannel asyncFileChannel, final long byteOffset) {
    return new LicenseInformation(QctReader.readInt(asyncFileChannel, byteOffset),
                                  QctReader.readString(asyncFileChannel, byteOffset + 0x0CL),
                                  SerialNumberDecoder.decode(asyncFileChannel,
                                                             QctReader.readPointer(asyncFileChannel,
                                                                                   byteOffset + 0x10L)));
  }

  private LicenseInformationDecoder() {
  }
}
