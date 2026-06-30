/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.encoder;

import com.github.aleksikangas.qct.core.image.ImageIndex;
import com.github.aleksikangas.qct.core.image.decoder.ImageIndexDecoder;
import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.encoder.ImageTileEncoder;
import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * @see ImageIndex
 * @see ImageIndexDecoder
 */
public final class ImageIndexEncoder {
  public static void encode(final QctWriter qctWriter, final ImageIndex imageIndex, final Metadata metadata) {
    Objects.requireNonNull(imageIndex);
    Objects.requireNonNull(metadata);

    final int height = metadata.heightTiles();
    final int width = metadata.widthTiles();
    Preconditions.checkState(height > 0 && width > 0, "height and width must be > 0");
    Preconditions.checkState(imageIndex.heightTiles() == height && imageIndex.widthTiles() == width,
                             "ImageIndex dimensions must match Metadata");

    final int[][] tileOffsets = new int[height][width];
    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        final ImageTile tile = imageIndex.imageTile(y, x);
        tileOffsets[y][x] = ImageTileEncoder.encode(qctWriter, tile);
      }
    }
    writePointerTable(qctWriter, tileOffsets);
  }

  private static void writePointerTable(final QctWriter qctWriter, final int[][] tileOffsets) {
    final int height = tileOffsets.length;
    final int width = tileOffsets[0].length;
    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        final int pointerOffset = Math.toIntExact(ImageIndex.BYTE_OFFSET + ((long) width * y + x) * 0x04L);
        qctWriter.writePointer(pointerOffset, tileOffsets[y][x]);
      }
    }
  }

  private ImageIndexEncoder() {
  }
}
