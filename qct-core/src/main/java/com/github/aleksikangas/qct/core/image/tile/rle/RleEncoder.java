/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.rle;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.ImageTileEncodingCandidate;
import com.github.aleksikangas.qct.core.image.tile.color.SubPalette;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

/**
 * Encoder of {@link ImageTile}s using {@link ImageTile.Encoding#RUN_LENGTH_ENCODING}.
 *
 * @see RleDecoder
 */
public final class RleEncoder {
  /**
   * A candidate for {@link ImageTile} encoding.
   *
   * @param subPalette the effective {@link SubPalette}
   * @param pixelBytes pixel data bytes
   */
  public record Candidate(SubPalette subPalette,
                          int[] pixelBytes) implements ImageTileEncodingCandidate {
    public Candidate {
      Objects.requireNonNull(subPalette);
      Objects.requireNonNull(pixelBytes);
    }

    @Override
    public ImageTile.Encoding encoding() {
      return ImageTile.Encoding.RUN_LENGTH_ENCODING;
    }

    @Override
    public int sizeBytes() {
      return subPalette.sizeBytes() + pixelBytes.length;
    }

    @Override
    public void encode(final QctWriter qctWriter, final int byteOffset) {
      SubPalette.Encoder.encode(qctWriter, subPalette, byteOffset);
      final int pixelDataOffset = Math.toIntExact(byteOffset + 0x01L + subPalette.size());
      qctWriter.writeBytes(pixelDataOffset, pixelBytes);
    }

    @Override
    public boolean equals(final Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      final Candidate that = (Candidate) o;
      return Objects.deepEquals(pixelBytes, that.pixelBytes) && Objects.equals(subPalette, that.subPalette);
    }

    @Override
    public int hashCode() {
      return Objects.hash(subPalette, Arrays.hashCode(pixelBytes));
    }

    @Nonnull
    @Override
    public String toString() {
      return "Candidate{" + "subPalette=" + subPalette + ", pixelBytes=" + Arrays.toString(pixelBytes) + '}';
    }

    public static Candidate of(final ImageTile imageTile) {
      final int[][] paletteIndices = imageTile.paletteIndices();
      final SubPalette subPalette = SubPalette.forRunLengthEncoding(imageTile);
      final int[] bytes = encodePixelData(subPalette, paletteIndices);
      return new Candidate(subPalette, bytes);
    }
  }

  public static void encode(final QctWriter qctWriter, final ImageTile imageTile, final int byteOffset) {
    Candidate.of(imageTile).encode(qctWriter, byteOffset);
  }

  private static int[] encodePixelData(final SubPalette subPalette, final int[][] paletteIndices) {
    // Initially unknown how many bytes are needed to represent the encoded RLE data,
    // assume the worst case of one byte per pixel (64 x 64 = 4096 bytes).
    final var rleBytes = new int[ImageTile.PIXEL_COUNT];
    int rleIndex = 0;
    int pixelCount = 0;
    while (pixelCount < ImageTile.PIXEL_COUNT) {
      final int currentPaletteIndex = paletteIndexAt(paletteIndices, pixelCount);
      int runLength = 1;
      final int maxRunLength = (1 << (8 - subPalette.bitsRequiredToIndex())) - 1;  // e.g. if 4 bits index -> max run 15

      while (pixelCount + runLength < ImageTile.PIXEL_COUNT &&
             paletteIndexAt(paletteIndices, pixelCount + runLength) == currentPaletteIndex &&
             runLength < maxRunLength) {
        runLength++;
      }
      final RleByte rleByte = new RleByte(currentPaletteIndex, runLength);
      final int encoded = RleByte.encode(subPalette, rleByte);
      rleBytes[rleIndex++] = encoded;

      pixelCount += runLength;
    }
    final var effectiveBytes = new int[rleIndex];
    System.arraycopy(rleBytes, 0, effectiveBytes, 0, rleIndex);
    return effectiveBytes;
  }

  private static int paletteIndexAt(final int[][] paletteIndices, final int pixelCount) {
    final int y = pixelCount / ImageTile.WIDTH;
    final int x = pixelCount % ImageTile.WIDTH;
    return paletteIndices[y][x];
  }

  private RleEncoder() {
  }
}
