/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.rle;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.color.SubPalette;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * Decoder of {@link ImageTile}s using {@link ImageTile.Encoding#RUN_LENGTH_ENCODING}.
 *
 * @see RleEncoder
 */
public final class RleDecoder {
  public static ImageTile decode(final QctReader qctReader, final int byteOffset) {
    final SubPalette subPalette = SubPalette.Decoder.decode(qctReader,
                                                            ImageTile.Encoding.RUN_LENGTH_ENCODING,
                                                            byteOffset);
    final int pixelDataOffset = Math.toIntExact(byteOffset + 0x01L + subPalette.size());
    final int[][] paletteIndices = decodePixelData(qctReader, subPalette, pixelDataOffset);
    return new ImageTile(ImageTile.Encoding.RUN_LENGTH_ENCODING, paletteIndices);
  }

  private static int[][] decodePixelData(final QctReader qctReader,
                                         final SubPalette subPalette,
                                         final int pixelDataByteOffset) {
    final var paletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    int byteOffset = pixelDataByteOffset;
    int pixelCount = 0;
    while (pixelCount < ImageTile.PIXEL_COUNT) {
      final int rleRawByte = qctReader.readByte(byteOffset++);
      final RleByte rleByte = RleByte.decode(subPalette, rleRawByte);
      for (int i = 0; i < rleByte.runLength(); ++i) {
        final int y = (pixelCount + i) / ImageTile.WIDTH;
        final int x = (pixelCount + i) % ImageTile.WIDTH;
        paletteIndices[y][x] = rleByte.paletteIndex();
      }
      pixelCount += rleByte.runLength();
    }
    return paletteIndices;
  }

  private RleDecoder() {
  }
}
