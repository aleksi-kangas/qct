/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.huffman.HuffmanCoding;
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
      final ImageTile decodedImageTile = switch (encoding) {
        case HUFFMAN_CODING -> HuffmanCoding.decode(qctReader, byteOffset);
        case PIXEL_PACKING -> placeholderImageTile(Encoding.PIXEL_PACKING);
        case RUN_LENGTH_ENCODING -> RunLengthEncoding.decode(qctReader, byteOffset);
      };
      return new ImageTile(decodedImageTile.encoding, deinterlaceRows(decodedImageTile.paletteIndices));
    }

    /**
     * Deinterlaces rows of an {@link ImageTile}. The pixel content of each tile is scanned from left to right in rows of
     * 64 pixels. The rows however are not in top to bottom order. Instead, they are interlaced using a bit-reverse
     * sequence. The decompressed tile data must be scanned out row at a time using this row sequence. See Chapter 7.1 in
     * the specification.
     *
     * @param paletteIndices of the {@link ImageTile}
     * @return deinterlaced {@link com.github.aleksikangas.qct.core.color.Palette} indices of the {@link ImageTile}
     */
    private static int[][] deinterlaceRows(final int[][] paletteIndices) {
      final var deinterlacedPaletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
      for (int i = 0; i < ImageTile.HEIGHT; ++i) {
        deinterlacedPaletteIndices[DEINTERLACED_ROW_SEQUENCE[i]] = paletteIndices[i];
      }
      return deinterlacedPaletteIndices;
    }

    private static int[] deinterlacedRowSequence() {
      final int[] deinterlacedRowSequence = new int[ImageTile.HEIGHT];
      for (int i = 0; i < ImageTile.HEIGHT; ++i) {
        deinterlacedRowSequence[INTERLACED_ROW_SEQUENCE[i]] = i;
      }
      return deinterlacedRowSequence;
    }

    private static final int[] DEINTERLACED_ROW_SEQUENCE = deinterlacedRowSequence();

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final ImageTile imageTile, final int byteOffset) {
      Objects.requireNonNull(imageTile);
      final ImageTile interlacedImageTile = new ImageTile(imageTile.encoding(),
                                                          interlaceRows(imageTile.paletteIndices()));
      switch (interlacedImageTile.encoding()) {
        case RUN_LENGTH_ENCODING -> RunLengthEncoding.encode(qctWriter, interlacedImageTile, byteOffset);
        case PIXEL_PACKING -> throw new UnsupportedOperationException("PIXEL_PACKING encoding not yet implemented");
        case HUFFMAN_CODING -> throw new UnsupportedOperationException("HUFFMAN_CODING encoding not yet implemented");
        default -> throw new IllegalArgumentException("Unknown encoding: " + interlacedImageTile.encoding());
      }
    }

    /**
     * Interlaces rows using the same bit-reverse order used by the decoder.
     * This must be done before any compression (RLE, Huffman, etc.).
     */
    private static int[][] interlaceRows(final int[][] paletteIndices) {
      final var interlaced = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
      for (int i = 0; i < ImageTile.HEIGHT; ++i) {
        interlaced[i] = paletteIndices[INTERLACED_ROW_SEQUENCE[i]];
      }
      return interlaced;
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

  private static final int[] INTERLACED_ROW_SEQUENCE = new int[]{ 0,
                                                                  32,
                                                                  16,
                                                                  48,
                                                                  8,
                                                                  40,
                                                                  24,
                                                                  56,
                                                                  4,
                                                                  36,
                                                                  20,
                                                                  52,
                                                                  12,
                                                                  44,
                                                                  28,
                                                                  60,
                                                                  2,
                                                                  34,
                                                                  18,
                                                                  50,
                                                                  10,
                                                                  42,
                                                                  26,
                                                                  58,
                                                                  6,
                                                                  38,
                                                                  22,
                                                                  54,
                                                                  14,
                                                                  46,
                                                                  30,
                                                                  62,
                                                                  1,
                                                                  33,
                                                                  17,
                                                                  49,
                                                                  9,
                                                                  41,
                                                                  25,
                                                                  57,
                                                                  5,
                                                                  37,
                                                                  21,
                                                                  53,
                                                                  13,
                                                                  45,
                                                                  29,
                                                                  61,
                                                                  3,
                                                                  35,
                                                                  19,
                                                                  51,
                                                                  11,
                                                                  43,
                                                                  27,
                                                                  59,
                                                                  7,
                                                                  39,
                                                                  23,
                                                                  55,
                                                                  15,
                                                                  47,
                                                                  31,
                                                                  63 };
}
