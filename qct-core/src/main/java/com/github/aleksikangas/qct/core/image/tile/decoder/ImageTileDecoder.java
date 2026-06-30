/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.decoder;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.encoder.ImageTileEncoder;
import com.github.aleksikangas.qct.core.image.tile.huffman.HuffmanDecoder;
import com.github.aleksikangas.qct.core.image.tile.rle.RleDecoder;
import com.github.aleksikangas.qct.core.image.tile.utils.ImageTileInterlacer;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see ImageTile
 * @see ImageTileEncoder
 */
public final class ImageTileDecoder {
  public static ImageTile decode(final QctReader qctReader, final int byteOffset) {
    final ImageTile.Encoding encoding = encodingOf(qctReader, byteOffset);
    final ImageTile decodedImageTile = switch (encoding) {
      case HUFFMAN_CODING -> HuffmanDecoder.decode(qctReader, byteOffset);
      case PIXEL_PACKING -> placeholderImageTile(ImageTile.Encoding.PIXEL_PACKING);
      case RUN_LENGTH_ENCODING -> RleDecoder.decode(qctReader, byteOffset);
    };
    return new ImageTile(decodedImageTile.encoding(),
                         ImageTileInterlacer.deinterlaceRows(decodedImageTile.paletteIndices()));
  }

  private static ImageTile.Encoding encodingOf(final QctReader qctReader, final int byteOffset) {
    final int firstByte = qctReader.readByte(byteOffset);
    if (firstByte == 0 || firstByte == 255) {
      return ImageTile.Encoding.HUFFMAN_CODING;
    }
    if (firstByte > 127) {
      return ImageTile.Encoding.PIXEL_PACKING;
    }
    return ImageTile.Encoding.RUN_LENGTH_ENCODING;
  }

  private static ImageTile placeholderImageTile(final ImageTile.Encoding encoding) {
    final var paletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < ImageTile.HEIGHT; ++y) {
      for (int x = 0; x < ImageTile.WIDTH; ++x) {
        paletteIndices[y][x] = 0;
      }
    }
    return new ImageTile(encoding, paletteIndices);
  }

  private ImageTileDecoder() {
  }
}
