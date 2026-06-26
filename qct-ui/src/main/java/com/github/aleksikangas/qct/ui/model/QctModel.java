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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ThreadSafe
public final class QctModel {
  public static final String QCT_FILE_PATH = "QCT_FILE_PATH";
  public static final String QCT_FILE = "QCT_FILE";

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();

  @GuardedBy("readWriteLock")
  @Nullable
  private Path qctFilePath = null;
  @GuardedBy("readWriteLock")
  @Nullable
  private QctFile qctFile = null;

  public void addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    pcs.addPropertyChangeListener(propertyChangeListener);
  }

  public void removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    pcs.removePropertyChangeListener(propertyChangeListener);
  }

  public Optional<Path> getQctFilePath() {
    readLock.lock();
    try {
      return Optional.ofNullable(qctFilePath);
    } finally {
      readLock.unlock();
    }
  }

  public void setQctFilePath(@Nullable final Path qctFilePath) {
    final Path oldQctFilePath;
    final QctFile oldQctFile;
    writeLock.lock();
    try {
      oldQctFilePath = this.qctFilePath;
      oldQctFile = this.qctFile;
      this.qctFilePath = qctFilePath;
      this.qctFile = null;
    } finally {
      writeLock.unlock();
    }
    pcs.firePropertyChange(QCT_FILE_PATH, oldQctFilePath, qctFilePath);
    pcs.firePropertyChange(QCT_FILE, oldQctFile, null);
    if (qctFilePath != null) {
      new DecodeQctFileWorker(qctFilePath, this::setQctFile).execute();
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

  public void setQctFile(@Nullable final QctFile qctFile) {
    final QctFile oldQctFile;
    writeLock.lock();
    try {
      oldQctFile = this.qctFile;
      this.qctFile = qctFile;
    } finally {
      writeLock.unlock();
    }
    pcs.firePropertyChange(QCT_FILE, oldQctFile, qctFile);
  }
}
