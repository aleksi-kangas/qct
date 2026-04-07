/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.rle;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.color.SubPalette;
import com.google.common.base.Preconditions;

/**
 * A RLE-byte.
 *
 * @param paletteIndex main {@link Palette} index
 * @param runLength    run length
 * @see RleDecoder
 * @see RleEncoder
 */
public record RleByte(int paletteIndex,
                      int runLength) {
  public static RleByte decode(final SubPalette subPalette, final int rleRawByte) {
    final int subPaletteIndexMask = (1 << subPalette.bitsRequiredToIndex()) - 1;
    final int subPaletteIndex = rleRawByte & subPaletteIndexMask;
    final int runLength = rleRawByte >> subPalette.bitsRequiredToIndex();
    return new RleByte(subPalette.paletteIndexOf(subPaletteIndex), runLength);
  }

  public static int encode(final SubPalette subPalette, final RleByte rleByte) {
    Preconditions.checkArgument(rleByte.runLength >= 1, "runLength must be >= 1");
    final int bits = subPalette.bitsRequiredToIndex();
    final int subPaletteIndex = subPalette.subPaletteIndexOf(rleByte.paletteIndex);
    return (rleByte.runLength << bits) | (subPaletteIndex & ((1 << bits) - 1));
  }
}
