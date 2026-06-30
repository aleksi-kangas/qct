/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.decoder;

import com.github.aleksikangas.qct.core.image.ImageIndex;
import com.github.aleksikangas.qct.core.image.encoder.ImageIndexEncoder;
import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.decoder.ImageTileDecoder;
import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @see ImageIndex
 * @see ImageIndexEncoder
 */
public final class ImageIndexDecoder {
  public static ImageIndex decode(final QctReader qctReader, final Metadata metadata) {
    Objects.requireNonNull(metadata);
    final int height = metadata.heightTiles();
    final int width = metadata.widthTiles();
    Preconditions.checkState(height > 0, "height must be > 0");
    Preconditions.checkState(width > 0, "width must be > 0");

    final ImageTile[][] imageTiles = new ImageTile[height][width];
    final List<CompletableFuture<Void>> imageTileFutures = new ArrayList<>();
    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        final int yTile = y;
        final int xTile = x;
        imageTileFutures.add(CompletableFuture.runAsync(() -> {
          final int imageTilePointerOffset = Math.toIntExact(ImageIndex.BYTE_OFFSET +
                                                             ((long) metadata.widthTiles() * yTile + xTile) * 0x04L);
          final int imageTilePointer = qctReader.readPointer(Math.toIntExact(imageTilePointerOffset));
          imageTiles[yTile][xTile] = ImageTileDecoder.decode(qctReader, imageTilePointer);
        }));
      }
    }
    imageTileFutures.forEach(CompletableFuture::join);
    return new ImageIndex(imageTiles);
  }

  private ImageIndexDecoder() {
  }
}
