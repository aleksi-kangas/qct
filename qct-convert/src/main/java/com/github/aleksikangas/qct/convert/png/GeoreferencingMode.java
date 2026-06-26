/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.convert.png;

import com.github.aleksikangas.qct.core.georef.GeoreferencingCoefficients;

public enum GeoreferencingMode {
  /**
   * Georeferencing disabled, projection ({@code .prj}) and world ({@code .pgw}) files shall not be created.
   */
  DISABLED,
  /**
   * Affine transformation shall be used. Affine transformation may not be accurate when the second and third order
   * {@link GeoreferencingCoefficients} are non-zero.
   */
  AFFINE,
}
