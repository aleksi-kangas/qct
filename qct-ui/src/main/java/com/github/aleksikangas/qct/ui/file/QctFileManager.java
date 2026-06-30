/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.file;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.core.decoder.QctFileDecoder;
import com.github.aleksikangas.qct.core.exception.QctRuntimeException;
import com.github.aleksikangas.qct.ui.toast.QctToast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ThreadSafe
public final class QctFileManager {
  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
  private final Set<QctFileListener> qctFileListeners = new CopyOnWriteArraySet<>();
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();

  @GuardedBy("readWriteLock")
  @Nonnull
  private Path qctFilePath = Path.of("");
  @GuardedBy("readWriteLock")
  @Nullable
  private QctFile qctFile = null;

  public void addListener(final QctFileListener qctFileListener) {
    qctFileListeners.add(Objects.requireNonNull(qctFileListener));
  }

  public void removeListener(final QctFileListener qctFileListener) {
    qctFileListeners.remove(Objects.requireNonNull(qctFileListener));
  }

  public Path getQctFilePath() {
    readLock.lock();
    try {
      return qctFilePath;
    } finally {
      readLock.unlock();
    }
  }

  public Optional<QctFile> getQctFile() {
    readLock.lock();
    try {
      return Optional.ofNullable(qctFile);
    } finally {
      readLock.unlock();
    }
  }

  public void setSelectedQctFile(final Path qctFilePath) {
    writeLock.lock();
    try {
      this.qctFilePath = qctFilePath;
      this.qctFile = null;
    } finally {
      writeLock.unlock();
    }
    notifyListeners(null);
    executorService.submit(() -> {
      QctFile decodedQctFile = null;
      try {
        decodedQctFile = decodeQctFile(qctFilePath);
        QctToast.show(QctToast.Type.SUCCESS, String.format("Decode successful: %s", decodedQctFile.metadata().name()));
      } catch (final Exception e) {
        QctToast.show(QctToast.Type.ERROR, String.format("Decode failed: %s", e.getMessage()));
      }
      writeLock.lock();
      try {
        this.qctFile = decodedQctFile;
      } finally {
        writeLock.unlock();
      }
      notifyListeners(decodedQctFile);
    });
  }

  private QctFile decodeQctFile(final Path qctFilePath) throws IOException, QctRuntimeException {
    try (final var readFileChannel = FileChannel.open(qctFilePath, Set.of(StandardOpenOption.READ))) {
      return QctFileDecoder.decode(readFileChannel);
    }
  }

  private void notifyListeners(@Nullable final QctFile qctFile) {
    qctFileListeners.forEach(qctFileListener -> qctFileListener.onQctFile(qctFile));
  }
}
