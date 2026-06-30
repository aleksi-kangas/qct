/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.georef.GeoreferencingCoefficients;
import com.github.aleksikangas.qct.core.image.ImageIndex;
import com.github.aleksikangas.qct.core.interpolation.InterpolationMatrix;
import com.github.aleksikangas.qct.core.meta.Metadata;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * <pre>
 * +--------+---------------+---------------------------------------------------+
 * | Offset | Size (Bytes) | Content                                            |
 * +--------+--------------+----------------------------------------------------+
 * | 0x0000 | 24 x 4       | Metadata - 24 Integers/Pointers                    |
 * | 0x0060 | 40 x 8       | Geographical Referencing Coefficients - 40 Doubles |
 * | 0x01A0 | 256 x 4      | Palette - 128 of 256 Colors                        |
 * | 0x05A0 | 128 x 128    | Interpolation Matrix                               |
 * | 0x45A0 | w x h x 4    | Image Index Pointers - QC3 Files Omit This         |
 * | -      | -            | File Body - Text Strings and Compressed Image Data |
 * +--------+--------------+----------------------------------------------------+
 * </pre>
 */
public record QctFile(Metadata metadata,
                      GeoreferencingCoefficients georeferencingCoefficients,
                      Palette palette,
                      InterpolationMatrix interpolationMatrix,
                      ImageIndex imageIndex) {
  public QctFile {
    Objects.requireNonNull(metadata);
    Objects.requireNonNull(georeferencingCoefficients);
  }

  @Nonnull
  @Override
  public String toString() {
    return "{" +
           "\n" +
           "Metadata: \n" +
           "\t" +
           metadata +
           "\n" +
           "GeoreferencingCoefficients: \n" +
           georeferencingCoefficients +
           "\n" +
           "Palette: \n" +
           "\t" +
           palette +
           "\n" +
           "InterpolationMatrix: \n" +
           "\t" +
           interpolationMatrix +
           "\n" +
           "}";
  }

  public int headerSizeBytes() {
    return 0x45A0 + imageIndex.size();
  }

  public int heightPixels() {
    return imageIndex.heightPixels();
  }

  public int widthPixels() {
    return imageIndex.widthPixels();
  }

  public int[] paletteIndices() {
    final int[] paletteIndices = new int[imageIndex.pixelCount()];
    IntStream.range(0, imageIndex.pixelCount()).parallel().forEach(pixelIndex -> {
      final int y = pixelIndex / imageIndex.widthPixels();
      final int x = pixelIndex % imageIndex.widthPixels();
      paletteIndices[pixelIndex] = imageIndex.pixelPaletteIndex(y, x);
    });
    return paletteIndices;
  }

  public int[][] paletteIndices2D() {
    final int[][] paletteIndices = new int[heightPixels()][widthPixels()];
    IntStream.range(0, heightPixels()).parallel().forEach(y -> {
      for (int x = 0; x < widthPixels(); x++) {
        paletteIndices[y][x] = imageIndex.pixelPaletteIndex(y, x);
      }
    });
    return paletteIndices;
  }

  public int[] rgbPixels() {
    final int[] rgbPixels = new int[imageIndex.pixelCount()];
    IntStream.range(0, imageIndex.pixelCount()).parallel().forEach(pixelIndex -> {
      final int y = pixelIndex / imageIndex.widthPixels();
      final int x = pixelIndex % imageIndex.widthPixels();
      final Color pixelColor = imageIndex.pixelColor(palette, y, x);
      rgbPixels[pixelIndex] = pixelColor.getRGB();
    });
    return rgbPixels;
  }

  public int[][] rgbPixels2D() {
    final int[][] pixels = new int[heightPixels()][widthPixels()];
    IntStream.range(0, heightPixels()).parallel().forEach(y -> {
      for (int x = 0; x < widthPixels(); x++) {
        final Color pixelColor = imageIndex.pixelColor(palette, y, x);
        pixels[y][x] = pixelColor.getRGB();
      }
    });
    return pixels;
  }
}
