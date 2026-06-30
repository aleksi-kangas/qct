/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.interpolation.encoder;

import com.github.aleksikangas.qct.core.interpolation.InterpolationMatrix;
import com.github.aleksikangas.qct.core.interpolation.decoder.InterpolationMatrixDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

/**
 * @see InterpolationMatrix
 * @see InterpolationMatrixDecoder
 */
public final class InterpolationMatrixEncoder {
  public static void encode(final QctWriter qctWriter, final InterpolationMatrix interpolationMatrix) {
    qctWriter.writeBytes(InterpolationMatrix.BYTE_OFFSET, interpolationMatrix.indices());
  }

  private InterpolationMatrixEncoder() {
  }
}
