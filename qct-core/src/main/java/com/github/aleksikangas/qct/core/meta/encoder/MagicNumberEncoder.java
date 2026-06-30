/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.MagicNumber;
import com.github.aleksikangas.qct.core.meta.decoder.MagicNumberDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see MagicNumber
 * @see MagicNumberDecoder
 */
public final class MagicNumberEncoder {
  public static void encode(final QctWriter qctWriter, final MagicNumber magicNumber, final int byteOffset) {
    Objects.requireNonNull(magicNumber);
    qctWriter.writeInt(byteOffset, magicNumber.value());
  }

  private MagicNumberEncoder() {
  }
}
