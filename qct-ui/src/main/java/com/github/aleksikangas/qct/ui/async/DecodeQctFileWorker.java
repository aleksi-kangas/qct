/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.async;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.ui.toast.QctToast;

import javax.swing.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class DecodeQctFileWorker extends SwingWorker<QctFile, Void> {
  private final Path qctFilePath;
  private final Consumer<QctFile> qctFileConsumer;

  public DecodeQctFileWorker(final Path qctFilePath, final Consumer<QctFile> qctFileConsumer) {
    this.qctFilePath = Objects.requireNonNull(qctFilePath);
    this.qctFileConsumer = Objects.requireNonNull(qctFileConsumer);
  }

  @Override
  protected QctFile doInBackground() throws Exception {
    try (final var readFileChannel = FileChannel.open(qctFilePath, Set.of(StandardOpenOption.READ))) {
      return QctFile.Decoder.decode(readFileChannel);
    }
  }

  @Override
  protected void done() {
    try {
      qctFileConsumer.accept(get());
      QctToast.show(QctToast.Type.SUCCESS, "Decode success");
    } catch (final ExecutionException _) {
      QctToast.show(QctToast.Type.ERROR, "Decode failed");
    } catch (final InterruptedException _) {
      Thread.currentThread().interrupt();
      QctToast.show(QctToast.Type.ERROR, "Decode interrupted");
    }
  }
}
