/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.rle.RunLengthEncoding;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a single tile of an image. Each tile within the image is compressed to reduce the size of the image file.
 * Three main compression algorithms ({@link Encoding}) are used to compress the image data, and all three
 * algorithms rely on the fact that an image tile will likely contain fewer colors than the overall image.
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
    return paletteIndices[y][x];
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

  public static final class Decoder {
    public static ImageTile decode(final QctReader qctReader, final int byteOffset) {
      final Encoding encoding = encodingOf(qctReader, byteOffset);
      return switch (encoding) {
        case HUFFMAN_CODING -> placeholderImageTile(Encoding.HUFFMAN_CODING);
        case PIXEL_PACKING -> placeholderImageTile(Encoding.PIXEL_PACKING);
        case RUN_LENGTH_ENCODING -> RunLengthEncoding.decode(qctReader, byteOffset);
      };
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final ImageTile imageTile, final int byteOffset) {
      Objects.requireNonNull(imageTile);

      switch (imageTile.encoding()) {
        case RUN_LENGTH_ENCODING -> RunLengthEncoding.encode(qctWriter, imageTile, byteOffset);
        case PIXEL_PACKING -> {
          throw new UnsupportedOperationException("PIXEL_PACKING encoding not yet implemented");
        }
        case HUFFMAN_CODING -> {
          throw new UnsupportedOperationException("HUFFMAN_CODING encoding not yet implemented");
        }
        default -> throw new IllegalArgumentException("Unknown encoding: " + imageTile.encoding());
      }
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
