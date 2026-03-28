/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

import com.github.aleksikangas.qct.core.exception.QctRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A utility class for reading from QCT file.
 */
public final class QctReader {
  private final FileChannel fileChannel;

  public QctReader(final FileChannel fileChannel) {
    this.fileChannel = Objects.requireNonNull(fileChannel);
  }

  /**
   * Reads a single unsigned byte from the given byte offset.
   *
   * @param byteOffset the byte offset of the byte
   * @return read byte
   */
  public int readByte(final int byteOffset) {
    return readBytes(byteOffset, 1)[0];
  }

  /**
   * Reads multiple unsigned bytes from the given byte offset.
   *
   * @param byteOffset the byte offset of the first byte
   * @param count      bytes to read
   * @return read bytes
   */
  public int[] readBytes(final int byteOffset, final int count) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(count);
    try {
      if (fileChannel.read(byteBuffer, byteOffset) == count) {
        byteBuffer.flip();
        final int[] bytes = new int[count];
        for (int i = 0; i < count; ++i) {
          bytes[i] = byteBuffer.get(i) & 0xFF;
        }
        return bytes;
      } else {
        throw new QctRuntimeException(String.format("Failed to read %d bytes from: %d", count, byteOffset));
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Reads multiple unsigned bytes from the given byte offset, until count or EOF.
   *
   * @param byteOffset the byte offset of the first byte
   * @param count      bytes to read
   * @return read bytes, until count or EOF
   */
  public int[] readBytesSafe(final int byteOffset, final int count) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(count);
    try {
      final int readCount = fileChannel.read(byteBuffer, byteOffset);
      if (readCount <= 0) {
        return new int[0];
      }
      byteBuffer.flip();
      final int[] bytes = new int[readCount];
      for (int i = 0; i < readCount; ++i) {
        bytes[i] = byteBuffer.get(i) & 0xFF;
      }
      return bytes;
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Reads a double (8 byte IEEE-754) from the given byte offset.
   *
   * @param byteOffset the byte offset of the double
   * @return read double
   */
  public double readDouble(final int byteOffset) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    try {
      if (fileChannel.read(byteBuffer, byteOffset) == 8) {
        return byteBuffer.flip().getDouble();
      } else {
        throw new QctRuntimeException(String.format("Failed to read 8 byte double from: %d", byteOffset));
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Reads multiple doubles (8 byte IEEE-754) from the given byte offset.
   *
   * @param byteOffset byte offset of the first double
   * @param count      doubles to read
   * @return read doubles
   */
  public double[] readDoubles(final int byteOffset, final int count) {
    final double[] doubles = new double[count];
    for (int i = 0; i < count; ++i) {
      doubles[i] = readDouble(byteOffset + i * 0x08);
    }
    return doubles;
  }

  /**
   * Reads an integer (4-byte) stored as little-endian from the given byte offset.
   *
   * @param byteOffset the byte offset of the integer
   * @return read integer
   */
  public int readInt(final int byteOffset) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    try {
      if (fileChannel.read(byteBuffer, byteOffset) == 4) {
        return byteBuffer.flip().getInt();
      } else {
        throw new QctRuntimeException(String.format("Failed to read 4 byte little endian integer from: %d",
                                                    byteOffset));
      }
    } catch (IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Reads an integer (4-byte) pointer stored as little-endian from the given byte offset. Pointers are essentially byte
   * offsets within the file.
   *
   * @param byteOffset the byte offset of the pointer
   * @return read pointer
   * @see #readInt(int)
   */
  public int readPointer(final int byteOffset) {
    return readInt(byteOffset);
  }

  /**
   * Reads a NULL-terminated string from the given byte offset.
   *
   * @param byteOffset byte offset of the string
   * @return read string
   */
  public String readString(final int byteOffset) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try {
      int position = byteOffset;
      while (fileChannel.read(byteBuffer, position) == 1) {
        byteBuffer.flip();
        byte b = byteBuffer.get();
        byteBuffer.clear();
        if (b == 0) {
          break;  // NULL
        }
        byteStream.write(b);
        position++;
      }
      return byteStream.toString(StandardCharsets.US_ASCII);
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Reads a NULL-terminated string by first reading the string pointer from the given byte offset, and then reading the
   * string from the pointed byte offset.
   *
   * @param pointerByteOffset byte offset of the string pointer
   * @return read string
   */
  public String readStringFromPointer(final int pointerByteOffset) {
    final int byteOffset = readPointer(pointerByteOffset);
    if (byteOffset != 0) {
      return readString(byteOffset);
    }
    return "";
  }
}
