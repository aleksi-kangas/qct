/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.color.encoder;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.color.SubPalette;
import com.github.aleksikangas.qct.core.image.tile.color.decoder.SubPaletteDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.annotations.VisibleForTesting;

/**
 * @see SubPalette
 * @see SubPaletteDecoder
 */
public final class SubPaletteEncoder {
  public static void encode(final QctWriter qctWriter, final SubPalette subPalette, final int byteOffset) {
    qctWriter.writeByte(byteOffset, subPalette.sizeByte());
    qctWriter.writeBytes(byteOffset + 0x01, subPalette.paletteIndices());
  }

  @VisibleForTesting
  public static SubPalette encode(final QctWriter qctWriter,
                                  final ImageTile imageTile,
                                  final ImageTile.Encoding encoding,
                                  final int byteOffset) {
    final var subPalette = SubPalette.forEncoding(imageTile, encoding);
    encode(qctWriter, subPalette, byteOffset);
    return subPalette;
  }

  private SubPaletteEncoder() {
  }
}
