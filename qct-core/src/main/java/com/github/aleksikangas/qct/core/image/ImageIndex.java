/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image;

import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

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

  public static final class Decoder {
    public static ImageIndex decode(final QctReader qctReader, final Metadata metadata) {
      Objects.requireNonNull(metadata);
      final int height = metadata.heightTiles();
      final int width = metadata.widthTiles();
      Preconditions.checkState(height > 0, "height must be > 0");
      Preconditions.checkState(width > 0, "width must be > 0");

      final ImageTile[][] imageTiles = new ImageTile[height][width];
      for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
          final int imageTilePointerOffset = Math.toIntExact(ImageIndex.BYTE_OFFSET +
                                                             ((long) metadata.widthTiles() * y + x) * 0x04L);
          final int imageTilePointer = qctReader.readPointer(Math.toIntExact(imageTilePointerOffset));
          imageTiles[y][x] = new ImageTile(ImageTile.Encoding.HUFFMAN_CODING,
                                           new int[ImageTile.HEIGHT][ImageTile.WIDTH]);  // TODO
        }
      }

      return new ImageIndex(imageTiles);
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final ImageIndex imageIndex) {
      throw new UnsupportedOperationException("Not implemented");
    }

    private Encoder() {
    }
  }
}
