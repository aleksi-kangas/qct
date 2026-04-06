/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.huffman.HuffmanDecoder;
import com.github.aleksikangas.qct.core.image.tile.rle.RleDecoder;
import com.github.aleksikangas.qct.core.image.tile.utils.ImageTileEncodingChooser;
import com.github.aleksikangas.qct.core.image.tile.utils.ImageTileInterlacer;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a single tile of an image. Each tile within the image is compressed to reduce the size of the image file.
 * Three main compression algorithms ({@link Encoding}) are used to compress the image data, and all three algorithms
 * rely on the fact that an image tile will likely contain fewer colors than the overall image.
 *
 * @param encoding       {@link Encoding} of the image tile
 * @param paletteIndices palette indices of each pixel
 */
public record ImageTile(Encoding encoding,
                        int[][] paletteIndices) {
  public enum Encoding {
    HUFFMAN_CODING,
    PIXEL_PACKING,
    RUN_LENGTH_ENCODING
  }

  public static final int HEIGHT = 64;
  public static final int WIDTH = 64;
  public static final int PIXEL_COUNT = HEIGHT * WIDTH;

  public ImageTile {
    Objects.requireNonNull(encoding);
    Objects.requireNonNull(paletteIndices);
    Preconditions.checkState(paletteIndices.length == ImageTile.HEIGHT && paletteIndices[0].length == ImageTile.WIDTH);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ImageTile imageTile = (ImageTile) o;
    return Objects.deepEquals(paletteIndices, imageTile.paletteIndices) && encoding == imageTile.encoding;
  }

  @Override
  public int hashCode() {
    return Objects.hash(encoding, Arrays.deepHashCode(paletteIndices));
  }

  @Nonnull
  @Override
  public String toString() {
    return "ImageTile{" + "encoding=" + encoding + ", paletteIndices=" + Arrays.toString(paletteIndices) + '}';
  }

  /**
   * Returns the {@link Palette} index of the pixel color value at the given location.
   *
   * @param y y-coordinate, in tile coordinate space
   * @param x x-coordinate, in tile coordinate space
   * @return the {@link Palette} index of the pixel color value
   */
  public int pixelPaletteIndex(final int y, final int x) {
    Objects.checkIndex(y, HEIGHT);
    Objects.checkIndex(x, WIDTH);
    return paletteIndices[y][x];
  }

  /**
   * Returns the pixel {@link Color} at the given location.
   *
   * @param palette to use
   * @param y       y-coordinate, in tile coordinate space
   * @param x       x-coordinate, in tile coordinate space
   * @return the pixel {@link Color
   */
  public Color pixelColor(final Palette palette, final int y, final int x) {
    return palette.color(pixelPaletteIndex(y, x));
  }

  public static final class Decoder {
    public static ImageTile decode(final QctReader qctReader, final int byteOffset) {
      final Encoding encoding = encodingOf(qctReader, byteOffset);
      final ImageTile decodedImageTile = switch (encoding) {
        case HUFFMAN_CODING -> HuffmanDecoder.decode(qctReader, byteOffset);
        case PIXEL_PACKING -> placeholderImageTile(Encoding.PIXEL_PACKING);
        case RUN_LENGTH_ENCODING -> RleDecoder.decode(qctReader, byteOffset);
      };
      return new ImageTile(decodedImageTile.encoding,
                           ImageTileInterlacer.deinterlaceRows(decodedImageTile.paletteIndices));
    }

    private static Encoding encodingOf(final QctReader qctReader, final int byteOffset) {
      final int firstByte = qctReader.readByte(byteOffset);
      if (firstByte == 0 || firstByte == 255) {
        return Encoding.HUFFMAN_CODING;
      }
      if (firstByte > 127) {
        return Encoding.PIXEL_PACKING;
      }
      return Encoding.RUN_LENGTH_ENCODING;
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static int encode(final QctWriter qctWriter, final ImageTile imageTile) {
      Objects.requireNonNull(imageTile);
      final ImageTile interlacedImageTile = new ImageTile(imageTile.encoding(),
                                                          ImageTileInterlacer.interlaceRows(imageTile.paletteIndices()));
      final ImageTileEncodingCandidate bestCandidate = ImageTileEncodingChooser.chooseEncoding(interlacedImageTile);
      final int tileByteOffset = qctWriter.allocate(bestCandidate.sizeBytes());
      bestCandidate.encode(qctWriter, tileByteOffset);
      return tileByteOffset;
    }

    private Encoder() {
    }
  }

  private static ImageTile placeholderImageTile(final Encoding encoding) {
    final var paletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < ImageTile.HEIGHT; ++y) {
      for (int x = 0; x < ImageTile.WIDTH; ++x) {
        paletteIndices[y][x] = 0;
      }
    }
    return new ImageTile(encoding, paletteIndices);
  }
}
