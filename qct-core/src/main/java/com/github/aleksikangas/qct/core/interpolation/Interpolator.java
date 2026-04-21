/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.interpolation;

import com.google.common.base.Preconditions;

import java.util.Objects;

public final class Interpolator {
  public enum DownscaleMode {
    X1(0),
    X2(1),
    X4(2),
    X8(3),
    X16(4),
    X32(5);

    private final int iterations;

    DownscaleMode(final int iterations) {
      this.iterations = iterations;
    }

    public int iterations() {
      return iterations;
    }
  }

  /**
   * Downscales a 2D array of palette indices according to the given {@link DownscaleMode} in both dimensions using the
   * {@link InterpolationMatrix}.
   * <p/>
   * This is the method you call when you want to render a tile (or any block) at a smaller scale, while preserving
   * roads, text, and other important features.
   *
   * @param interpolationMatrix to downscale with
   * @param downscaleMode       defining downscaling mode
   * @param paletteIndices      rectangular 2D array of palette indices
   * @return new 2D array of the same palette indices, downscaled according to {@link DownscaleMode} in each dimension
   */
  public static int[][] downscale(final InterpolationMatrix interpolationMatrix,
                                  final DownscaleMode downscaleMode,
                                  int[][] paletteIndices) {
    Objects.requireNonNull(interpolationMatrix, "interpolationMatrix");
    Objects.requireNonNull(paletteIndices, "paletteIndices");
    final int originalHeight = paletteIndices.length;
    final int originalWidth = paletteIndices[0].length;
    Preconditions.checkArgument(originalHeight % 2 == 0 && originalWidth % 2 == 0);
    final int downscaleIterations = downscaleMode.iterations();
    for (int i = 0; i < downscaleIterations; ++i) {
      paletteIndices = downscale2x(interpolationMatrix, paletteIndices);
    }
    return paletteIndices;
  }

  /**
   * Downscales a 2D array of palette indices by exactly a factor of 2 in both dimensions using the
   * {@link InterpolationMatrix}.
   * <p/>
   * This is the method you call when you want to render a tile (or any block) at 50% scale. For a 64×64 tile it
   * produces a clean 32×32 result that preserves roads, text, and other important features.
   *
   * @param interpolationMatrix to downscale with
   * @param paletteIndices      rectangular 2D array of palette indices
   * @return new 2D array of the same palette indices, downscaled by 2x in each dimension
   */
  public static int[][] downscale2x(final InterpolationMatrix interpolationMatrix, final int[][] paletteIndices) {
    Objects.requireNonNull(paletteIndices, "paletteIndices");
    final int height = paletteIndices.length;
    final int width = paletteIndices[0].length;
    Preconditions.checkArgument(height % 2 == 0 && width % 2 == 0);
    final int downscaledHeight = height / 2;
    final int downscaledWidth = width / 2;
    final int[][] downscaledPaletteIndices = new int[downscaledHeight][downscaledWidth];
    for (int y = 0; y < downscaledHeight; ++y) {
      for (int x = 0; x < downscaledWidth; ++x) {
        final int topLeft = paletteIndices[2 * y][2 * x];
        final int topRight = paletteIndices[2 * y][2 * x + 1];
        final int bottomLeft = paletteIndices[2 * y + 1][2 * x];
        final int bottomRight = paletteIndices[2 * y + 1][2 * x + 1];
        downscaledPaletteIndices[y][x] = interpolate2x2(interpolationMatrix,
                                                        topLeft,
                                                        topRight,
                                                        bottomLeft,
                                                        bottomRight);
      }
    }
    return downscaledPaletteIndices;
  }

  private static int interpolate2x2(final InterpolationMatrix interpolationMatrix,
                                    final int topLeftPaletteIndex,
                                    final int topRightPaletteIndex,
                                    final int bottomLeftPaletteIndex,
                                    final int bottomRightPaletteIndex) {
    final int topPaletteIndex = interpolationMatrix.paletteIndexOf(topLeftPaletteIndex, topRightPaletteIndex);
    final int bottomPaletteIndex = interpolationMatrix.paletteIndexOf(bottomLeftPaletteIndex, bottomRightPaletteIndex);
    return interpolationMatrix.paletteIndexOf(topPaletteIndex, bottomPaletteIndex);
  }

  private Interpolator() {
  }
}
