/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * <pre>
 * +--------+-------------------+--------------------------------------------+
 * | Offset | Size (Bytes)      | Content                                    |
 * +--------+-------------------+--------------------------------------------+
 * | 0x45A0 | w x h x 4         | Image Index Pointers - QC3 files omit this |
 * +--------+-------------------+--------------------------------------------+
 * </pre>
 */
public record ImageIndex(ImageTile[][] imageTiles) {
  public static final int BYTE_OFFSET = 0x45A0;

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    final ImageIndex that = (ImageIndex) o;
    return Objects.deepEquals(imageTiles, that.imageTiles);
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(imageTiles);
  }

  @Nonnull
  @Override
  public String toString() {
    return "ImageIndex{" + "imageTiles=" + Arrays.toString(imageTiles) + '}';
  }

  public int size() {
    return heightTiles() * widthTiles() * 0x04;
  }

  public int heightTiles() {
    return imageTiles.length;
  }

  public int widthTiles() {
    return imageTiles[0].length;
  }

  public int heightPixels() {
    return heightTiles() * ImageTile.HEIGHT;
  }

  public int widthPixels() {
    return widthTiles() * ImageTile.WIDTH;
  }

  public int pixelCount() {
    return Math.multiplyExact(heightPixels(), widthPixels());
  }

  public ImageTile imageTile(final int yTile, final int xTile) {
    Objects.checkIndex(yTile, heightTiles());
    Objects.checkIndex(xTile, widthTiles());
    return imageTiles[yTile][xTile];
  }

  public Color pixelColor(final Palette palette, final int yPixel, final int xPixel) {
    Objects.checkIndex(yPixel, heightPixels());
    Objects.checkIndex(xPixel, widthPixels());
    final int yTile = yPixel / ImageTile.HEIGHT;
    final int xTile = xPixel / ImageTile.WIDTH;
    final int yTilePixel = yPixel % ImageTile.HEIGHT;
    final int xTilePixel = xPixel % ImageTile.WIDTH;
    return imageTile(yTile, xTile).pixelColor(palette, yTilePixel, xTilePixel);
  }

  public static final class Decoder {
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
            imageTiles[yTile][xTile] = ImageTile.Decoder.decode(qctReader, imageTilePointer);
          }));
        }
      }
      imageTileFutures.forEach(CompletableFuture::join);
      return new ImageIndex(imageTiles);
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
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
          tileOffsets[y][x] = ImageTile.Encoder.encode(qctWriter, tile);
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

    private Encoder() {
    }
  }
}
