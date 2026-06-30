/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.georef.decoder;

import com.github.aleksikangas.qct.core.georef.GeoreferencingCoefficients;
import com.github.aleksikangas.qct.core.georef.encoder.GeoreferencingCoefficientsEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see GeoreferencingCoefficients
 * @see GeoreferencingCoefficientsEncoder
 */
public final class GeoreferencingCoefficientsDecoder {
  public static GeoreferencingCoefficients decode(final QctReader qctReader) {
    final double[] easDoubles = qctReader.readDoubles(GeoreferencingCoefficients.BYTE_OFFSET, 10);
    final double[] norDoubles = qctReader.readDoubles(Math.toIntExact(GeoreferencingCoefficients.BYTE_OFFSET + 0x50L),
                                                      10);
    final double[] latDoubles = qctReader.readDoubles(Math.toIntExact(GeoreferencingCoefficients.BYTE_OFFSET + 0xA0L),
                                                      10);
    final double[] lonDoubles = qctReader.readDoubles(Math.toIntExact(GeoreferencingCoefficients.BYTE_OFFSET + 0xF0L),
                                                      10);

    return new GeoreferencingCoefficients(
            // eas
            easDoubles[0],
            easDoubles[1],
            easDoubles[2],
            easDoubles[3],
            easDoubles[4],
            easDoubles[5],
            easDoubles[6],
            easDoubles[7],
            easDoubles[8],
            easDoubles[9],
            // nor
            norDoubles[0],
            norDoubles[1],
            norDoubles[2],
            norDoubles[3],
            norDoubles[4],
            norDoubles[5],
            norDoubles[6],
            norDoubles[7],
            norDoubles[8],
            norDoubles[9],
            // lat
            latDoubles[0],
            latDoubles[1],
            latDoubles[2],
            latDoubles[3],
            latDoubles[4],
            latDoubles[5],
            latDoubles[6],
            latDoubles[7],
            latDoubles[8],
            latDoubles[9],
            // lon
            lonDoubles[0],
            lonDoubles[1],
            lonDoubles[2],
            lonDoubles[3],
            lonDoubles[4],
            lonDoubles[5],
            lonDoubles[6],
            lonDoubles[7],
            lonDoubles[8],
            lonDoubles[9]);
  }

  private GeoreferencingCoefficientsDecoder() {
  }
}
