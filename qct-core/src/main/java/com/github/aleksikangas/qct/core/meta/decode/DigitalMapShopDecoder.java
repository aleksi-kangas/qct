/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.DigitalMapShop;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.FileChannel;

/**
 * A decoder of {@link DigitalMapShop}.
 *
 * @see DigitalMapShop
 */
public final class DigitalMapShopDecoder {
  public static DigitalMapShop decode(final FileChannel fileChannel, final long byteOffset) {
    return new DigitalMapShop(QctReader.readInt(fileChannel, byteOffset),
                              QctReader.readStringFromPointer(fileChannel, byteOffset + 0x04L));
  }

  private DigitalMapShopDecoder() {
  }
}
