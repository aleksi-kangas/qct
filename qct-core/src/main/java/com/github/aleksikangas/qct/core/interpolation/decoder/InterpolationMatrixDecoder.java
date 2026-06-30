/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.interpolation.decoder;

import com.github.aleksikangas.qct.core.interpolation.InterpolationMatrix;
import com.github.aleksikangas.qct.core.interpolation.encoder.InterpolationMatrixEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see InterpolationMatrix
 * @see InterpolationMatrixEncoder
 */
public final class InterpolationMatrixDecoder {
  public static InterpolationMatrix decode(final QctReader qctReader) {
    return new InterpolationMatrix(qctReader.readBytes(InterpolationMatrix.BYTE_OFFSET, InterpolationMatrix.SIZE));
  }

  private InterpolationMatrixDecoder() {
  }
}
