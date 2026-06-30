/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.FileFormatVersion;
import com.github.aleksikangas.qct.core.meta.encoder.FileFormatVersionEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.util.Arrays;

/**
 * @see FileFormatVersion
 * @see FileFormatVersionEncoder
 */
public final class FileFormatVersionDecoder {
  public static FileFormatVersion decode(final QctReader qctReader, final int byteOffset) {
    final int value = qctReader.readInt(byteOffset);
    return Arrays.stream(FileFormatVersion.values())
                 .filter(f -> f.value() == value)
                 .findFirst()
                 .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown FileFormatVersion: %d",
                                                                               value)));
  }

  private FileFormatVersionDecoder() {
  }
}
