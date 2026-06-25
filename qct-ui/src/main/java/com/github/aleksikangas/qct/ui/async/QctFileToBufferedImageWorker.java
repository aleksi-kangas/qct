/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.async;

import com.github.aleksikangas.qct.convert.awt.BufferedImageConverter;
import com.github.aleksikangas.qct.core.QctFile;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;

public final class QctFileToBufferedImageWorker extends SwingWorker<BufferedImage, Void> {
  private final QctFile qctFile;
  private final Consumer<BufferedImage> bufferedImageConsumer;

  public QctFileToBufferedImageWorker(final QctFile qctFile, final Consumer<BufferedImage> bufferedImageConsumer) {
    this.qctFile = Objects.requireNonNull(qctFile);
    this.bufferedImageConsumer = Objects.requireNonNull(bufferedImageConsumer);
  }

  @Override
  protected BufferedImage doInBackground() throws Exception {
    return BufferedImageConverter.convert(qctFile);
  }

  @Override
  protected void done() {
    try {
      bufferedImageConsumer.accept(get());
    } catch (final Exception e) {
      // TODO
    }
  }
}
