/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.decoder;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.core.color.decoder.PaletteDecoder;
import com.github.aleksikangas.qct.core.encoder.QctFileEncoder;
import com.github.aleksikangas.qct.core.georef.decoder.GeoreferencingCoefficientsDecoder;
import com.github.aleksikangas.qct.core.image.decoder.ImageIndexDecoder;
import com.github.aleksikangas.qct.core.interpolation.decoder.InterpolationMatrixDecoder;
import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.meta.decoder.MetadataDecoder;
import com.github.aleksikangas.qct.core.utils.MappedQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.FileChannel;

/**
 * @see QctFile
 * @see QctFileEncoder
 */
public final class QctFileDecoder {
  public static QctFile decode(final FileChannel fileChannel) {
    final QctReader qctReader = new MappedQctReader(fileChannel);
    final Metadata metadata = MetadataDecoder.decode(qctReader);
    return new QctFile(metadata,
                       GeoreferencingCoefficientsDecoder.decode(qctReader),
                       PaletteDecoder.decode(qctReader),
                       InterpolationMatrixDecoder.decode(qctReader),
                       ImageIndexDecoder.decode(qctReader, metadata));
  }

  private QctFileDecoder() {
  }
}
