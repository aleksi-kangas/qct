/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.Flag;
import com.github.aleksikangas.qct.core.meta.encoder.FlagEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.util.EnumSet;
import java.util.Set;

/**
 * @see Flag
 * @see FlagEncoder
 */
public final class FlagDecoder {
  public static Set<Flag> decode(final QctReader qctReader, final int byteOffset) {
    final int value = qctReader.readInt(byteOffset);
    final Set<Flag> flags = EnumSet.noneOf(Flag.class);
    if ((value & Flag.MUST_HAVE_ORIGINAL_FILE.mask()) != 0) {
      flags.add(Flag.MUST_HAVE_ORIGINAL_FILE);
    }
    if ((value & Flag.ALLOW_CALIBRATION.mask()) != 0) {
      flags.add(Flag.ALLOW_CALIBRATION);
    }
    return flags;
  }

  private FlagDecoder() {
  }
}
