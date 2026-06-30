/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.Flag;
import com.github.aleksikangas.qct.core.meta.decoder.FlagDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Set;

/**
 * @see Flag
 * @see FlagDecoder
 */
public final class FlagEncoder {
  public static void encode(final QctWriter qctWriter, final Set<Flag> flags, final int byteOffset) {
    int bitField = 0;
    for (Flag flag : flags) {
      bitField |= flag.mask();
    }
    qctWriter.writeInt(byteOffset, bitField);
  }

  private FlagEncoder() {
  }
}
