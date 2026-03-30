/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.color;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.ImageTile.Encoding;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Sub-palette containing indices to the main {@link Palette}, used in tiles with {@link Encoding#PIXEL_PACKING} and
 * {@link Encoding#RUN_LENGTH_ENCODING}.
 *
 * @param encoding       encoding of the tile using the sub-palette
 * @param size           size of the sub-palette
 * @param paletteIndices indices pointing to the main {@link Palette}
 */
public record SubPalette(Encoding encoding,
                         int size,
                         int[] paletteIndices) {
  public SubPalette {
    Objects.requireNonNull(encoding);
    Objects.requireNonNull(paletteIndices);
    Preconditions.checkState(paletteIndices.length == size);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    final SubPalette that = (SubPalette) o;
    return size == that.size && encoding == that.encoding && Objects.deepEquals(paletteIndices, that.paletteIndices);
  }

  @Override
  public int hashCode() {
    return Objects.hash(encoding, size, Arrays.hashCode(paletteIndices));
  }

  @Nonnull
  @Override
  public String toString() {
    return "SubPalette{" + "size=" + size + ", paletteIndices=" + Arrays.toString(paletteIndices) + '}';
  }

  public int sizeByte() {
    return switch (encoding) {
      case RUN_LENGTH_ENCODING -> size;
      case PIXEL_PACKING -> 256 - size;
      default -> throw new IllegalStateException("Unsupported encoding " + encoding);
    };
  }

  /**
   * The number of bits required to index the sub-palette.
   *
   * @return the number of bits required to index the sub-palette
   */
  public int bitsRequiredToIndex() {
    if (size == 0) return 0;
    if (size == 1) return 1;
    return (int) Math.ceil((Math.log(size) / Math.log(2)));
  }

  /**
   * The main {@link Palette} index of the given sub-palette index.
   *
   * @param subPaletteIndex of interest
   * @return the main {@link Palette} index
   */
  public int paletteIndexOf(final int subPaletteIndex) {
    return paletteIndices[subPaletteIndex];
  }

  public int subPaletteIndexOf(final int mainPaletteIndex) {
    for (int i = 0; i < size; i++) {
      if (paletteIndices[i] == mainPaletteIndex) {
        return i;
      }
    }
    throw new IllegalArgumentException("Palette index " + mainPaletteIndex + " not found in sub-palette");
  }

  public static final class Decoder {
    public static SubPalette decode(final QctReader qctReader, final Encoding encoding, final int byteOffset) {
      final int size = switch (encoding) {
        case RUN_LENGTH_ENCODING -> qctReader.readByte(byteOffset);
        case PIXEL_PACKING -> 256 - qctReader.readByte(byteOffset);
        default -> throw new IllegalStateException("Unsupported encoding " + encoding);
      };
      final int[] paletteIndices = qctReader.readBytes(byteOffset + 0x01, size);
      return new SubPalette(encoding, size, paletteIndices);
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static SubPalette encode(final QctWriter qctWriter, final ImageTile imageTile, final int byteOffset) {
      final Set<Integer> uniquePaletteIndices = new LinkedHashSet<>();
      for (int y = 0; y < ImageTile.HEIGHT; ++y) {
        for (int x = 0; x < ImageTile.WIDTH; ++x) {
          uniquePaletteIndices.add(imageTile.paletteIndices()[y][x]);
        }
      }
      Preconditions.checkState(!uniquePaletteIndices.isEmpty());

      final int size = uniquePaletteIndices.size();
      final int[] paletteIndices = uniquePaletteIndices.stream().mapToInt(Integer::intValue).toArray();
      Arrays.sort(paletteIndices);
      final SubPalette subPalette = new SubPalette(imageTile.encoding(), size, paletteIndices);

      qctWriter.writeByte(byteOffset, subPalette.sizeByte());
      qctWriter.writeBytes(byteOffset + 0x01, subPalette.paletteIndices());
      return subPalette;
    }

    private Encoder() {
    }
  }
}
