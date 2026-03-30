/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

import com.github.aleksikangas.qct.core.exception.QctRuntimeException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Buffered implementation of {@link QctReader}.
 * <p/>
 * This class wraps a {@link FileChannel} and uses internal buffering to minimize
 * expensive I/O operations when performing many small reads (bytes, ints, doubles, strings)
 * at arbitrary offsets in a QCT file.
 *
 * @see QctReader
 * @see DirectQctReader
 */
public final class BufferedQctReader implements QctReader {
  private final FileChannel fileChannel;
  private final int bufferSize;
  private final ByteBuffer buffer;

  private long bufferStart = -1;   // File offset of the start of the current buffer
  private long bufferEnd = -1;     // bufferStart + buffer.limit()

  /**
   * Creates a new {@code BufferedQctReader} with a default buffer size of 64 KB.
   *
   * @param fileChannel the file channel to read from
   */
  public BufferedQctReader(final FileChannel fileChannel) {
    this(fileChannel, 64 * 1024);
  }

  /**
   * Creates a new {@code BufferedQctReader} with a custom buffer size.
   *
   * @param fileChannel the file channel to read from
   * @param bufferSize  the size of the internal buffer in bytes (minimum 1 KB)
   */
  public BufferedQctReader(final FileChannel fileChannel, final int bufferSize) {
    this.fileChannel = Objects.requireNonNull(fileChannel);
    this.bufferSize = Math.max(1024, bufferSize);
    this.buffer = ByteBuffer.allocateDirect(this.bufferSize);
  }

  @Override
  public int readByte(final int byteOffset) {
    ensureLoaded(byteOffset, 1);
    final int pos = (int) (byteOffset - bufferStart);
    return buffer.get(pos) & 0xFF;
  }

  @Override
  public int[] readBytes(final int byteOffset, final int count) {
    final int[] result = new int[count];
    for (int i = 0; i < count; i++) {
      result[i] = readByte(byteOffset + i);
    }
    return result;
  }

  @Override
  public int[] readBytesSafe(final int byteOffset, final int count) {
    if (count <= 0) return new int[0];
    ensureLoaded(byteOffset, count);
    final int available = (int) Math.max(0, bufferEnd - byteOffset);
    final int toRead = Math.min(count, available);
    final int[] result = new int[toRead];
    final int basePos = (int) (byteOffset - bufferStart);
    for (int i = 0; i < toRead; i++) {
      result[i] = buffer.get(basePos + i) & 0xFF;
    }
    return result;
  }

  @Override
  public double readDouble(final int byteOffset) {
    ensureLoaded(byteOffset, 8);
    final int pos = (int) (byteOffset - bufferStart);
    return buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).getDouble(pos);
  }

  @Override
  public double[] readDoubles(final int byteOffset, final int count) {
    final double[] result = new double[count];
    for (int i = 0; i < count; i++) {
      result[i] = readDouble(byteOffset + i * 8);
    }
    return result;
  }

  @Override
  public int readInt(final int byteOffset) {
    ensureLoaded(byteOffset, 4);
    int pos = (int) (byteOffset - bufferStart);
    return buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).getInt(pos);
  }

  @Override
  public int readPointer(final int byteOffset) {
    return readInt(byteOffset);
  }

  @Override
  public String readString(final int byteOffset) {
    ByteBuffer temp = ByteBuffer.allocate(256);
    long pos = byteOffset & 0xFFFFFFFFL; // Treat int as unsigned 32-bit
    while (true) {
      final int b = readByte((int) pos++);
      if (b == 0) break;

      if (!temp.hasRemaining()) {
        final ByteBuffer newBuf = ByteBuffer.allocate(temp.capacity() * 2);
        temp.flip();
        newBuf.put(temp);
        temp = newBuf;
      }
      temp.put((byte) b);
    }
    temp.flip();
    return StandardCharsets.US_ASCII.decode(temp).toString();
  }

  @Override
  public String readStringFromPointer(final int pointerByteOffset) {
    final int targetOffset = readPointer(pointerByteOffset);
    return targetOffset != 0 ? readString(targetOffset) : "";
  }

  /**
   * Ensures that the requested range of bytes is available in the internal buffer.
   */
  private void ensureLoaded(final long byteOffset, final int length) {
    if (byteOffset >= bufferStart && byteOffset + length <= bufferEnd) {
      return; // Data already in buffer
    }

    // Calculate new buffer window with some overlap
    long newStart = Math.max(0, byteOffset - (bufferSize / 4));

    final long fileSize;
    try {
      fileSize = fileChannel.size();
    } catch (final IOException e) {
      throw new QctRuntimeException("Failed to get file size", e);
    }

    long newEnd = Math.min(fileSize, newStart + bufferSize);
    newStart = Math.max(0, newEnd - bufferSize);

    buffer.clear();
    try {
      int bytesRead = fileChannel.read(buffer, newStart);
      if (bytesRead > 0) {
        buffer.flip();
      } else {
        buffer.limit(0);
      }
    } catch (final IOException e) {
      throw new QctRuntimeException("Failed to read from file at offset " + newStart, e);
    }

    bufferStart = newStart;
    bufferEnd = newStart + buffer.limit();
  }
}