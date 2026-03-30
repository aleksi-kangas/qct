/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

/**
 * A simple, efficient sequential byte stream reader on top of a {@link QctReader}.
 */
public final class QctByteStream {
  private final QctReader reader;
  private long position;

  /**
   * Creates a new byte stream starting at the given offset.
   *
   * @param reader      the underlying QCT reader
   * @param startOffset starting byte offset in the file
   */
  public QctByteStream(QctReader reader, int startOffset) {
    this.reader = reader;
    this.position = startOffset;
  }

  /**
   * Reads the next unsigned byte and advances the position.
   *
   * @return next byte as unsigned int (0-255)
   */
  public int nextByte() {
    return reader.readByte((int) position++);
  }

  /**
   * Reads the next byte without advancing the position (peek).
   */
  public int peekByte() {
    return reader.readByte((int) position);
  }

  /**
   * Returns current position in the file.
   */
  public long position() {
    return position;
  }

  /**
   * Sets the current position.
   */
  public void position(long newPosition) {
    this.position = newPosition;
  }

  /**
   * Skips given number of bytes.
   */
  public void skip(int bytes) {
    position += bytes;
  }
}
