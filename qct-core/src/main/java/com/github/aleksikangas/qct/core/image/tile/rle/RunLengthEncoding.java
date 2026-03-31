/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.rle;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.ImageTile.Encoding;
import com.github.aleksikangas.qct.core.image.tile.ImageTileEncodingCandidate;
import com.github.aleksikangas.qct.core.image.tile.color.SubPalette;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

/**
 * Decoding & encoding functionality for {@link ImageTile}s using {@link Encoding#RUN_LENGTH_ENCODING}.
 */
public final class RunLengthEncoding {
  public static ImageTile decode(final QctReader qctReader, final int byteOffset) {
    final SubPalette subPalette = SubPalette.Decoder.decode(qctReader, Encoding.RUN_LENGTH_ENCODING, byteOffset);
    final int pixelDataOffset = Math.toIntExact(byteOffset + 0x01L + subPalette.size());
    // In order to avoid reading one byte at a time from the file,
    // read bytes into a buffer assuming the worst case of one byte per pixel (64 x 64 = 4096 bytes),
    // which practically should never occur.
    final int[] bytes = qctReader.readBytesSafe(pixelDataOffset, ImageTile.PIXEL_COUNT);
    final int[][] paletteIndices = decodePixelData(subPalette, bytes);
    return new ImageTile(Encoding.RUN_LENGTH_ENCODING, paletteIndices);
  }

  /**
   * A candidate for {@link ImageTile} encoding.
   *
   * @param encoding   {@link Encoding#RUN_LENGTH_ENCODING}
   * @param subPalette the effective {@link SubPalette}
   * @param pixelBytes pixel data bytes
   */
  public record RleImageTileEncodingCandidate(Encoding encoding,
                                              SubPalette subPalette,
                                              int[] pixelBytes) implements ImageTileEncodingCandidate {
    public RleImageTileEncodingCandidate(final SubPalette subPalette, final int[] pixelBytes) {
      this(Encoding.RUN_LENGTH_ENCODING, subPalette, pixelBytes);
    }

    public RleImageTileEncodingCandidate {
      Objects.requireNonNull(encoding);
      Objects.requireNonNull(subPalette);
      Objects.requireNonNull(pixelBytes);
    }

    @Override
    public int sizeBytes() {
      return subPalette.size() + pixelBytes.length;
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
      final RleImageTileEncodingCandidate that = (RleImageTileEncodingCandidate) o;
      return Objects.deepEquals(pixelBytes, that.pixelBytes) &&
             encoding == that.encoding &&
             Objects.equals(subPalette, that.subPalette);
    }

    @Override
    public int hashCode() {
      return Objects.hash(encoding, subPalette, Arrays.hashCode(pixelBytes));
    }

    @Nonnull
    @Override
    public String toString() {
      return "RleImageTileEncodingCandidate{" +
             "encoding=" +
             encoding +
             ", subPalette=" +
             subPalette +
             ", pixelBytes=" +
             Arrays.toString(pixelBytes) +
             '}';
    }
  }

  public static RleImageTileEncodingCandidate candidateOf(final ImageTile imageTile) {
    final int[][] paletteIndices = imageTile.paletteIndices();
    final SubPalette subPalette = SubPalette.of(imageTile);
    final int[] bytes = encodePixelData(subPalette, paletteIndices);
    return new RleImageTileEncodingCandidate(subPalette, bytes);
  }

  public static void encode(final QctWriter qctWriter, final ImageTile imageTile, final int byteOffset) {
    candidateOf(imageTile).encode(qctWriter, byteOffset);
  }

  private static int[][] decodePixelData(final SubPalette subPalette, final int[] bytes) {
    final var paletteIndices = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    int byteIndex = 0;
    int pixelCount = 0;
    while (pixelCount < ImageTile.PIXEL_COUNT) {
      final int rleRawByte = bytes[byteIndex++];
      final RleByte rleByte = RleByte.decode(subPalette, rleRawByte);
      for (int i = 0; i < rleByte.runLength; ++i) {
        final int y = (pixelCount + i) / ImageTile.WIDTH;
        final int x = (pixelCount + i) % ImageTile.WIDTH;
        paletteIndices[y][x] = rleByte.paletteIndex;
      }
      pixelCount += rleByte.runLength;
    }
    return paletteIndices;
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

  /**
   * A RLE-byte.
   *
   * @param paletteIndex main {@link Palette} index
   * @param runLength    run length
   */
  private record RleByte(int paletteIndex,
                         int runLength) {
    public static RleByte decode(final SubPalette subPalette, final int rleRawByte) {
      final int subPaletteIndexMask = (1 << subPalette.bitsRequiredToIndex()) - 1;
      final int subPaletteIndex = rleRawByte & subPaletteIndexMask;
      final int rumLength = rleRawByte >> subPalette.bitsRequiredToIndex();
      return new RleByte(subPalette.paletteIndexOf(subPaletteIndex), rumLength);
    }

    public static int encode(final SubPalette subPalette, final RleByte rleByte) {
      Preconditions.checkArgument(rleByte.runLength >= 1, "runLength must be >= 1");
      final int bits = subPalette.bitsRequiredToIndex();
      final int subPaletteIndex = subPalette.subPaletteIndexOf(rleByte.paletteIndex);
      return (rleByte.runLength << bits) | (subPaletteIndex & ((1 << bits) - 1));
    }
  }

  private RunLengthEncoding() {
  }
}
