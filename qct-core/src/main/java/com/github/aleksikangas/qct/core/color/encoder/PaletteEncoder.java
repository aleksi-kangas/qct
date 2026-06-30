/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.color.encoder;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.color.decoder.PaletteDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

/**
 * @see Palette
 * @see PaletteDecoder
 */
public final class PaletteEncoder {
  public static void encode(final QctWriter qctWriter, final Palette palette) {
    qctWriter.writeBytes(Palette.BYTE_OFFSET, palette.byteValues());
  }

  private PaletteEncoder() {
  }
}
