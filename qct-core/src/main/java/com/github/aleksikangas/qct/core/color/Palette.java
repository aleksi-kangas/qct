/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.color;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <pre>
 * +--------+--------------+------------------------------+
 * | Offset | Size (Bytes) | Content                      |
 * +--------+--------------+------------------------------+
 * | 0x01A0 | 256 x 4      | Palette - 128 of 256 Colors  |
 * +--------+--------------+------------------------------+
 *
 * +--------+-----------+-------------------------+
 * | Offset | Data Type | Content                 |
 * +--------+-----------+-------------------------+
 * | 0x00   | Byte      | Blue Intensity [0-255]  |
 * | 0x01   | Byte      | Green Intensity [0-255] |
 * | 0x02   | Byte      | Red Intensity [0-255]   |
 * | 0x03   | Byte      | Padding byte, set to 0  |
 * +--------+-----------+-------------------------+
 * </pre>
 */
public record Palette(Color[] colors) {
  public static final int BYTE_OFFSET = 0x01A0;
  public static final int SIZE = 128;

  public Palette {
    Objects.requireNonNull(colors);
    Preconditions.checkState(colors.length == SIZE);
    Arrays.stream(colors).forEach(Objects::requireNonNull);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    final Palette palette = (Palette) o;
    return Objects.deepEquals(colors, palette.colors);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(colors);
  }

  @Nonnull
  @Override
  public String toString() {
    final String colorList = Arrays.stream(colors)
                                   .map(color -> String.format("[%d,%d,%d]",
                                                               color.getRed(),
                                                               color.getGreen(),
                                                               color.getBlue()))
                                   .collect(Collectors.joining(", "));

    return String.format("[%s]", colorList);
  }

  /**
   * Get the {@link Color} of the given palette index.
   *
   * @param paletteIndex to obtain
   * @return {@link Color} of the given palette index
   */
  public Color color(final int paletteIndex) {
    Objects.checkIndex(paletteIndex, Palette.SIZE);
    return colors[paletteIndex];
  }

  public int[] byteValues() {
    final int[] bytes = new int[Palette.SIZE * 4];
    for (int i = 0; i < Palette.SIZE; ++i) {
      bytes[i * 4] = color(i).getBlue();
      bytes[i * 4 + 1] = color(i).getGreen();
      bytes[i * 4 + 2] = color(i).getRed();
      bytes[i * 4 + 3] = 0;
    }
    return bytes;
  }

  public int[] rgbPixels(final int[] paletteIndices) {
    final int n = paletteIndices.length;
    final int[] rgbPixels = new int[n];
    IntStream.range(0, n).parallel().forEach(pixelIndex -> {
      final Color pixelColor = color(paletteIndices[pixelIndex]);
      rgbPixels[pixelIndex] = pixelColor.getRGB();
    });
    return rgbPixels;
  }

  public int[][] rgbPixels2D(final int[][] paletteIndices) {
    final int height = paletteIndices.length;
    final int width = paletteIndices[0].length;
    final int[][] rgbPixels = new int[height][width];
    IntStream.range(0, height).parallel().forEach(y -> {
      for (int x = 0; x < width; x++) {
        final Color pixelColor = color(paletteIndices[y][x]);
        rgbPixels[y][x] = pixelColor.getRGB();
      }
    });
    return rgbPixels;
  }

  public static final class Decoder {
    public static Palette decode(final QctReader qctReader) {
      final int[] bytes = qctReader.readBytes(Palette.BYTE_OFFSET, Palette.SIZE * 4);
      final Color[] colors = new Color[Palette.SIZE];
      for (int i = 0; i < Palette.SIZE; ++i) {
        final int blue = bytes[i * 4];
        final int green = bytes[i * 4 + 1];
        final int red = bytes[i * 4 + 2];
        colors[i] = new Color(red, green, blue);
      }
      return new Palette(colors);
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final Palette palette) {
      qctWriter.writeBytes(Palette.BYTE_OFFSET, palette.byteValues());
    }

    private Encoder() {
    }
  }
}
