/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.convert.awt;

import com.github.aleksikangas.qct.core.QctFile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

/**
 * A converter for {@link QctFile} -> {@link BufferedImage}.
 */
public final class BufferedImageConverter {
  public static BufferedImage convert(final QctFile qctFile) {
    final int[] rgbPixels = qctFile.rgbPixels();
    final var bufferedImage = new BufferedImage(qctFile.widthPixels(),
                                                qctFile.heightPixels(),
                                                BufferedImage.TYPE_INT_RGB);
    final WritableRaster writableRaster = bufferedImage.getRaster();
    final var dataBufferInt = (DataBufferInt) writableRaster.getDataBuffer();
    System.arraycopy(rgbPixels, 0, dataBufferInt.getData(), 0, rgbPixels.length);
    return bufferedImage;
  }

  private BufferedImageConverter() {
  }
}
