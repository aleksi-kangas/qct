/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.DigitalMapShop;
import com.github.aleksikangas.qct.core.meta.encoder.DigitalMapShopEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

/**
 * @see DigitalMapShop
 * @see DigitalMapShopEncoder
 */
public final class DigitalMapShopDecoder {
  public static DigitalMapShop decode(final QctReader qctReader, final int byteOffset) {
    return new DigitalMapShop(qctReader.readInt(byteOffset),
                              qctReader.readStringFromPointer(Math.toIntExact(byteOffset + 0x04L)));
  }

  public static DigitalMapShop decodeFromPointer(final QctReader qctReader, final int byteOffset) {
    final int pointer = qctReader.readPointer(byteOffset);
    return decode(qctReader, pointer);
  }

  private DigitalMapShopDecoder() {
  }
}
