/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.georef.encoder;

import com.github.aleksikangas.qct.core.georef.GeoreferencingCoefficients;
import com.github.aleksikangas.qct.core.georef.decoder.GeoreferencingCoefficientsDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see GeoreferencingCoefficients
 * @see GeoreferencingCoefficientsDecoder
 */
public final class GeoreferencingCoefficientsEncoder {
  public static void encode(final QctWriter qctWriter, final GeoreferencingCoefficients georeferencingCoefficients) {
    Objects.requireNonNull(georeferencingCoefficients);

    qctWriter.writeDoubles(GeoreferencingCoefficients.BYTE_OFFSET, georeferencingCoefficients.easValues());
    qctWriter.writeDoubles(Math.toIntExact(GeoreferencingCoefficients.BYTE_OFFSET + 0x50L),
                           georeferencingCoefficients.norValues());
    qctWriter.writeDoubles(Math.toIntExact(GeoreferencingCoefficients.BYTE_OFFSET + 0xA0L),
                           georeferencingCoefficients.latValues());
    qctWriter.writeDoubles(Math.toIntExact(GeoreferencingCoefficients.BYTE_OFFSET + 0xF0L),
                           georeferencingCoefficients.lonValues());
  }

  private GeoreferencingCoefficientsEncoder() {
  }
}
