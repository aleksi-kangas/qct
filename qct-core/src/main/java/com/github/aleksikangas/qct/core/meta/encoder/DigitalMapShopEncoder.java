/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.DigitalMapShop;
import com.github.aleksikangas.qct.core.meta.decoder.DigitalMapShopDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see DigitalMapShop
 * @see DigitalMapShopDecoder
 */
public final class DigitalMapShopEncoder {
  public static void encode(final QctWriter qctWriter, final DigitalMapShop digitalMapShop, final int byteOffset) {
    Objects.requireNonNull(digitalMapShop);

    qctWriter.writeInt(byteOffset, digitalMapShop.size());
    qctWriter.allocateWriteString(Math.toIntExact(byteOffset + 0x04L), digitalMapShop.qc3Url());
  }

  public static void encodeWithPointer(final QctWriter qctWriter,
                                       final DigitalMapShop digitalMapShop,
                                       final int byteOffset) {
    final int pointer = qctWriter.allocate(DigitalMapShop.HEADER_SIZE);
    qctWriter.writePointer(byteOffset, pointer);
    encode(qctWriter, digitalMapShop, pointer);
  }

  private DigitalMapShopEncoder() {
  }
}
