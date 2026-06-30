/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.FileFormatVersion;
import com.github.aleksikangas.qct.core.meta.decoder.FileFormatVersionDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see FileFormatVersion
 * @see FileFormatVersionDecoder
 */
public final class FileFormatVersionEncoder {
  public static void encode(final QctWriter qctWriter,
                            final FileFormatVersion fileFormatVersion,
                            final int byteOffset) {
    Objects.requireNonNull(fileFormatVersion);
    qctWriter.writeInt(byteOffset, fileFormatVersion.value());
  }

  private FileFormatVersionEncoder() {
  }
}
