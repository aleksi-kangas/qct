/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.model;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.ui.async.DecodeQctFileWorker;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ThreadSafe
public final class QctModel {
  public static final String QCT_FILE_PATH = "QCT_FILE_PATH";
  public static final String QCT_FILE = "QCT_FILE";

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private final Lock lock = new ReentrantLock();

  @GuardedBy("lock")
  @Nullable
  private Path qctFilePath = null;
  @GuardedBy("lock")
  @Nullable
  private QctFile qctFile = null;

  public void addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    pcs.addPropertyChangeListener(propertyChangeListener);
  }

  public void removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    pcs.removePropertyChangeListener(propertyChangeListener);
  }

  public Optional<Path> getQctFilePath() {
    lock.lock();
    try {
      return Optional.ofNullable(qctFilePath);
    } finally {
      lock.unlock();
    }
  }

  public Optional<QctFile> getQctFile() {
    lock.lock();
    try {
      return Optional.ofNullable(qctFile);
    } finally {
      lock.unlock();
    }
  }

  public void onQctFilePath(@Nullable final Path qctFilePath) {
    lock.lock();
    final Path oldQctFilePath = this.qctFilePath;
    final QctFile oldQctFile = this.qctFile;
    try {
      this.qctFilePath = qctFilePath;
      this.qctFile = null;
    } finally {
      lock.unlock();
    }
    pcs.firePropertyChange(QCT_FILE_PATH, oldQctFilePath, qctFilePath);
    pcs.firePropertyChange(QCT_FILE, oldQctFile, null);

    if (qctFilePath != null) {
      new DecodeQctFileWorker(qctFilePath, this::onQctFile).execute();
    }
  }

  public void onQctFile(@Nullable final QctFile qctFile) {
    lock.lock();
    final QctFile oldQctFile = this.qctFile;
    try {
      this.qctFile = qctFile;
    } finally {
      lock.unlock();
    }
    pcs.firePropertyChange(QCT_FILE, oldQctFile, qctFile);
  }
}
