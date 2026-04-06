/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.huffman;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.utils.QctByteStream;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.google.common.base.Preconditions;

/**
 * Decoder of {@link ImageTile}s using {@link ImageTile.Encoding#HUFFMAN_CODING}.
 *
 * @see HuffmanEncoder
 */
public final class HuffmanDecoder {
  public static ImageTile decode(final QctReader qctReader, final int byteOffset) {
    final var qctByteStream = new QctByteStream(qctReader, byteOffset + 1);
    final var huffmanCodeBook = new HuffmanCodeBook(qctByteStream);
    final int[][] paletteIndices;
    if (huffmanCodeBook.size() == 1) {
      paletteIndices = decodeSimple(huffmanCodeBook);
    } else {
      paletteIndices = decodeComplex(huffmanCodeBook, qctByteStream);
    }
    return new ImageTile(ImageTile.Encoding.HUFFMAN_CODING, paletteIndices);
  }

  private static int[][] decodeSimple(final HuffmanCodeBook huffmanCodeBook) {
    Preconditions.checkArgument(huffmanCodeBook.size() == 1);
    final var paletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    final int paletteIndex = huffmanCodeBook.getPaletteIndex(0);
    for (int y = 0; y < ImageTile.HEIGHT; ++y) {
      for (int x = 0; x < ImageTile.WIDTH; ++x) {
        paletteIndices[y][x] = paletteIndex;
      }
    }
    return paletteIndices;
  }

  private static int[][] decodeComplex(final HuffmanCodeBook huffmanCodeBook, final QctByteStream qctByteStream) {
    Preconditions.checkArgument(huffmanCodeBook.size() > 1);
    final var paletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    int currentByte = qctByteStream.nextByte();
    int bitCount = 8;
    int pixelIndex = 0;
    while (pixelIndex < ImageTile.PIXEL_COUNT) {
      if (huffmanCodeBook.isColor()) {
        final int y = pixelIndex / ImageTile.WIDTH;
        final int x = pixelIndex % ImageTile.WIDTH;
        paletteIndices[y][x] = huffmanCodeBook.getPaletteIndex();
        ++pixelIndex;
        huffmanCodeBook.reset();
        continue;
      }
      final boolean bit = (currentByte & 1) == 1;
      huffmanCodeBook.step(bit);
      currentByte >>= 1;
      --bitCount;
      if (bitCount == 0) {
        currentByte = qctByteStream.nextByte();
        bitCount = 8;
      }
    }
    return paletteIndices;
  }

  private HuffmanDecoder() {
  }
}
