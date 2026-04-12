/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

import com.github.aleksikangas.qct.core.exception.QctRuntimeException;
import com.google.common.base.Preconditions;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A memory-mapped implementation of {@link QctReader} for reading QCT files efficiently.
 * <p>
 * This class maps the entire QCT file into memory using {@link MappedByteBuffer}, providing fast random access to the
 * file's contents without the overhead of repeated system calls. All read operations are performed directly on the
 * mapped buffer.
 *
 * @see QctReader
 * @see java.nio.MappedByteBuffer
 */
@ThreadSafe
public final class MappedQctReader implements QctReader {
  private final long fileSize;
  private final MappedByteBuffer mappedByteBuffer;

  /**
   * Creates a new {@code MappedQctReader} by memory-mapping the given file channel.
   *
   * @param fileChannel the {@link FileChannel} to map. Must be open and readable.
   * @throws QctRuntimeException   if the file cannot be mapped (e.g., I/O error)
   * @throws IllegalStateException if the file size exceeds {@link Integer#MAX_VALUE} bytes
   */
  public MappedQctReader(final FileChannel fileChannel) {
    Objects.requireNonNull(fileChannel);
    try {
      fileSize = fileChannel.size();
      Preconditions.checkState(fileSize <= Integer.MAX_VALUE);
      mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
      mappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    } catch (final IOException e) {
      throw new QctRuntimeException("Failed to map QCT file buffer", e);
    }
  }

  @Override
  public int readByte(final int byteOffset) {
    Preconditions.checkArgument(0 <= byteOffset && byteOffset < fileSize);
    return mappedByteBuffer.get(byteOffset) & 0xFF;
  }

  @Override
  public int[] readBytes(final int byteOffset, final int count) {
    Preconditions.checkArgument(0 <= byteOffset && byteOffset < fileSize);
    final int[] bytes = new int[count];
    for (int i = 0; i < count; ++i) {
      if (byteOffset + i >= fileSize) {
        throw new QctRuntimeException("End of file");
      }
      bytes[i] = readByte(byteOffset + i);
    }
    return bytes;
  }

  @Override
  public int[] readBytesSafe(final int byteOffset, final int count) {
    Preconditions.checkArgument(0 <= byteOffset && byteOffset < fileSize);
    final int readCount = Math.min((int) fileSize - byteOffset, count);
    if (readCount <= 0) {
      return new int[0];
    }
    final int[] bytes = new int[readCount];
    for (int i = 0; i < readCount; ++i) {
      bytes[i] = readByte(byteOffset + i);
    }
    return bytes;
  }

  @Override
  public double readDouble(final int byteOffset) {
    Preconditions.checkArgument(0 <= byteOffset && byteOffset < fileSize);
    return mappedByteBuffer.getDouble(byteOffset);
  }

  @Override
  public double[] readDoubles(final int byteOffset, final int count) {
    Preconditions.checkArgument(0 <= byteOffset && byteOffset < fileSize);
    final double[] doubles = new double[count];
    for (int i = 0; i < count; ++i) {
      doubles[i] = readDouble(byteOffset + i * 0x08);
    }
    return doubles;
  }

  @Override
  public int readInt(final int byteOffset) {
    Preconditions.checkArgument(0 <= byteOffset && byteOffset < fileSize);
    return mappedByteBuffer.getInt(byteOffset);
  }

  @Override
  public int readPointer(final int byteOffset) {
    Preconditions.checkArgument(0 <= byteOffset && byteOffset < fileSize);
    return readInt(byteOffset);
  }

  @Override
  public String readString(final int byteOffset) {
    Preconditions.checkArgument(0 <= byteOffset && byteOffset < fileSize);
    if (byteOffset == 0) {  // NULL
      return "";
    }
    int length = 0;
    while (byteOffset + length < fileSize && mappedByteBuffer.get(byteOffset + length) != 0) {
      ++length;
    }
    if (length == 0) {
      return "";
    }
    final byte[] bytes = new byte[length];
    for (int i = 0; i < length; ++i) {
      bytes[i] = mappedByteBuffer.get(byteOffset + i);
    }
    return new String(bytes, StandardCharsets.US_ASCII);
  }

  @Override
  public String readStringFromPointer(final int pointerByteOffset) {
    Preconditions.checkArgument(0 <= pointerByteOffset && pointerByteOffset < fileSize);
    final int byteOffset = readInt(pointerByteOffset);
    return readString(byteOffset);
  }
}
