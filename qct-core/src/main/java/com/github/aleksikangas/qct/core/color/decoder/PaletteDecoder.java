/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.color.decoder;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.color.encoder.PaletteEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.awt.*;

/**
 * @see Palette
 * @see PaletteEncoder
 */
public final class PaletteDecoder {
  public static Palette decode(final QctReader qctReader) {
    final int[] bytes = qctReader.readBytes(Palette.BYTE_OFFSET, Palette.SIZE * 4);
    final Color[] colors = new Color[Palette.SIZE];
    for (int i = 0; i < Palette.SIZE; ++i) {
      final int blue = bytes[i * 4];
      final int green = bytes[i * 4 + 1];
      final int red = bytes[i * 4 + 2];
      colors[i] = new Color(red, green, blue);
    }
    return new Palette(colors);
  }

  private PaletteDecoder() {
  }
}
