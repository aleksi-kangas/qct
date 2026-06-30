/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.color.decoder;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.color.SubPalette;
import com.github.aleksikangas.qct.core.image.tile.color.encoder.SubPaletteEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see SubPalette
 * @see SubPaletteEncoder
 */
public final class SubPaletteDecoder {
  public static SubPalette decode(final QctReader qctReader, final ImageTile.Encoding encoding, final int byteOffset) {
    final int size = switch (encoding) {
      case RUN_LENGTH_ENCODING -> qctReader.readByte(byteOffset);
      case PIXEL_PACKING -> 256 - qctReader.readByte(byteOffset);
      default -> throw new IllegalStateException("Unsupported encoding " + encoding);
    };
    final int[] paletteIndices = qctReader.readBytes(byteOffset + 0x01, size);
    return new SubPalette(encoding, paletteIndices);
  }

  private SubPaletteDecoder() {
  }
}