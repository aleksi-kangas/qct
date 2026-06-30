/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.encoder;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.ImageTileEncodingCandidate;
import com.github.aleksikangas.qct.core.image.tile.decoder.ImageTileDecoder;
import com.github.aleksikangas.qct.core.image.tile.utils.ImageTileEncodingChooser;
import com.github.aleksikangas.qct.core.image.tile.utils.ImageTileInterlacer;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see ImageTile
 * @see ImageTileDecoder
 */
public final class ImageTileEncoder {
  public static int encode(final QctWriter qctWriter, final ImageTile imageTile) {
    Objects.requireNonNull(imageTile);
    final ImageTile interlacedImageTile = new ImageTile(imageTile.encoding(),
                                                        ImageTileInterlacer.interlaceRows(imageTile.paletteIndices()));
    final ImageTileEncodingCandidate bestCandidate = ImageTileEncodingChooser.chooseEncoding(interlacedImageTile);
    final int tileByteOffset = qctWriter.allocate(bestCandidate.sizeBytes());
    bestCandidate.encode(qctWriter, tileByteOffset);
    return tileByteOffset;
  }

  private ImageTileEncoder() {
  }
}
