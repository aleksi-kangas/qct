/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.ImageTile;

import javax.annotation.Nonnull;
import java.awt.*;
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

  public int pixelPaletteIndex(final int yPixel, final int xPixel) {
    Objects.checkIndex(yPixel, heightPixels());
    Objects.checkIndex(xPixel, widthPixels());
    final int yTile = yPixel / ImageTile.HEIGHT;
    final int xTile = xPixel / ImageTile.WIDTH;
    final int yTilePixel = yPixel % ImageTile.HEIGHT;
    final int xTilePixel = xPixel % ImageTile.WIDTH;
    return imageTile(yTile, xTile).pixelPaletteIndex(yTilePixel, xTilePixel);
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
}
