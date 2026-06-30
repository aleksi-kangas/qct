/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.encoder;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.core.color.encoder.PaletteEncoder;
import com.github.aleksikangas.qct.core.decoder.QctFileDecoder;
import com.github.aleksikangas.qct.core.georef.encoder.GeoreferencingCoefficientsEncoder;
import com.github.aleksikangas.qct.core.image.encoder.ImageIndexEncoder;
import com.github.aleksikangas.qct.core.interpolation.encoder.InterpolationMatrixEncoder;
import com.github.aleksikangas.qct.core.meta.encoder.MetadataEncoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see QctFile
 * @see QctFileDecoder
 */
public final class QctFileEncoder {
  public static void encode(final QctWriter qctWriter, final QctFile qctFile) {
    Objects.requireNonNull(qctFile);

    MetadataEncoder.encode(qctWriter, qctFile.metadata());
    GeoreferencingCoefficientsEncoder.encode(qctWriter, qctFile.georeferencingCoefficients());
    PaletteEncoder.encode(qctWriter, qctFile.palette());
    InterpolationMatrixEncoder.encode(qctWriter, qctFile.interpolationMatrix());
    ImageIndexEncoder.encode(qctWriter, qctFile.imageIndex(), qctFile.metadata());
  }

  private QctFileEncoder() {
  }
}
