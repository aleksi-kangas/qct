/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.MagicNumber;
import com.github.aleksikangas.qct.core.meta.encoder.MagicNumberEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.util.Arrays;

/**
 * @see MagicNumber
 * @see MagicNumberEncoder
 */
public final class MagicNumberDecoder {
  public static MagicNumber decode(final QctReader qctReader, final int byteOffset) {
    final int value = qctReader.readInt(byteOffset);
    return Arrays.stream(MagicNumber.values())
                 .filter(f -> f.value() == value)
                 .findFirst()
                 .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown MagicNumber: %d", value)));
  }

  private MagicNumberDecoder() {
  }
}
