/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.huffman;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.utils.QctByteStream;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * Decoder of {@link ImageTile}s using {@link ImageTile.Encoding#HUFFMAN_CODING}.
 *
 * @see HuffmanEncoder
 */
public final class HuffmanDecoder {
  public static ImageTile decode(final QctReader qctReader, final int byteOffset) {
    final var qctByteStream = new QctByteStream(qctReader, byteOffset + 1);
    final var codeBook = HuffmanCodeBook.from(qctByteStream);
    final int[][] paletteIndices = codeBook.isSingleColor()
                                   ? decodeSingleColor(codeBook)
                                   : decodeWithTree(codeBook, qctByteStream);
    return new ImageTile(ImageTile.Encoding.HUFFMAN_CODING, paletteIndices);
  }

  private static int[][] decodeSingleColor(final HuffmanCodeBook codeBook) {
    final int paletteIndex = codeBook.getSinglePaletteIndex();
    final var paletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < ImageTile.HEIGHT; ++y) {
      for (int x = 0; x < ImageTile.WIDTH; ++x) {
        paletteIndices[y][x] = paletteIndex;
      }
    }
    return paletteIndices;
  }

  private static int[][] decodeWithTree(final HuffmanCodeBook codeBook, final QctByteStream qctByteStream) {
    final var paletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    int currentByte = qctByteStream.nextByte();
    int bitsLeft = 8;
    int pixelIndex = 0;
    while (pixelIndex < ImageTile.PIXEL_COUNT) {
      HuffmanTreeNode currentNode = codeBook.getRoot();
      while (currentNode instanceof HuffmanTreeNode.ParentNode parentNode) {
        if (bitsLeft == 0) {
          currentByte = qctByteStream.nextByte();
          bitsLeft = 8;
        }
        final boolean goRight = (currentByte & 1) == 1;
        currentByte >>= 1;
        bitsLeft--;
        currentNode = goRight ? parentNode.right() : parentNode.left();
      }
      final HuffmanTreeNode.LeafNode leafNode = (HuffmanTreeNode.LeafNode) currentNode;
      final int y = pixelIndex / ImageTile.WIDTH;
      final int x = pixelIndex % ImageTile.WIDTH;
      paletteIndices[y][x] = leafNode.paletteIndex();
      pixelIndex++;
    }
    return paletteIndices;
  }

  private HuffmanDecoder() {
  }
}
