/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

import com.github.aleksikangas.qct.core.exception.QctRuntimeException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * A utility class for writing to QCT file.
 */
public final class QctWriter {
  /**
   * Writes a single unsigned byte at the given byte offset.
   *
   * @param fileChannel to write to
   * @param byteOffset  the byte offset where to write
   * @param value       the byte value to write (0-255)
   */
  public static void writeByte(final FileChannel fileChannel, final int byteOffset, final int value) {
    writeBytes(fileChannel, byteOffset, new int[]{ value });
  }

  /**
   * Writes multiple unsigned bytes at the given byte offset.
   *
   * @param fileChannel to write to
   * @param byteOffset  the byte offset of the first byte
   * @param values      array of byte values to write (each 0-255)
   */
  public static void writeBytes(final FileChannel fileChannel, final int byteOffset, final int[] values) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(values.length);
    for (int b : values) {
      byteBuffer.put((byte) (b & 0xFF));
    }
    byteBuffer.flip();
    try {
      final int written = fileChannel.write(byteBuffer, byteOffset);
      if (written != values.length) {
        throw new QctRuntimeException(String.format("Failed to write %d bytes to offset %d (only wrote %d)",
                                                    values.length,
                                                    byteOffset,
                                                    written));
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Writes a double (8-byte IEEE-754, little-endian) at the given byte offset.
   *
   * @param fileChannel to write to
   * @param byteOffset  the byte offset where to write the double
   * @param value       the double value to write
   */
  public static void writeDouble(final FileChannel fileChannel, final int byteOffset, final double value) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer.putDouble(value);
    byteBuffer.flip();
    try {
      if (fileChannel.write(byteBuffer, byteOffset) != 8) {
        throw new QctRuntimeException(String.format("Failed to write 8-byte double to offset: %d", byteOffset));
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Writes multiple doubles (8-byte IEEE-754, little-endian) starting at the given byte offset.
   *
   * @param fileChannel to write to
   * @param byteOffset  byte offset of the first double
   * @param values      array of double values to write
   */
  public static void writeDoubles(final FileChannel fileChannel, final int byteOffset, final double[] values) {
    for (int i = 0; i < values.length; ++i) {
      writeDouble(fileChannel, Math.toIntExact(byteOffset + i * 8L), values[i]);
    }
  }

  /**
   * Writes an integer (4-byte, little-endian) at the given byte offset.
   *
   * @param fileChannel to write to
   * @param byteOffset  the byte offset where to write the integer
   * @param value       the integer value to write
   */
  public static void writeInt(final FileChannel fileChannel, final int byteOffset, final int value) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer.putInt(value);
    byteBuffer.flip();
    try {
      if (fileChannel.write(byteBuffer, byteOffset) != 4) {
        throw new QctRuntimeException(String.format("Failed to write 4-byte little-endian integer to offset: %d",
                                                    byteOffset));
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Writes an integer pointer (4-byte little-endian byte offset) at the given byte offset.
   *
   * @param fileChannel to write to
   * @param byteOffset  the byte offset where to write the pointer
   * @param pointer     the pointer value (byte offset in the file)
   * @see #writeInt(FileChannel, int, int)
   */
  public static void writePointer(final FileChannel fileChannel, final int byteOffset, final int pointer) {
    writeInt(fileChannel, byteOffset, pointer);
  }

  /**
   * Writes a NULL-terminated string at the given byte offset.
   * <p>
   * The string is written in ISO-8859-1 / extended ASCII (one byte per character),
   * followed by a null byte (0x00).
   *
   * @param fileChannel to write to
   * @param byteOffset  the byte offset where to write the string
   * @param value       the string to write (null is treated as empty string)
   */
  public static void writeString(final FileChannel fileChannel, final int byteOffset, final String value) {
    final String str = (value == null) ? "" : value;
    final int length = str.length();
    final ByteBuffer byteBuffer = ByteBuffer.allocate(length + 1); // +1 for null terminator

    for (int i = 0; i < length; ++i) {
      char c = str.charAt(i);
      if (c > 255) {
        throw new QctRuntimeException(String.format(
                "Character '%c' (U+%04X) cannot be written as single byte in string at offset %d",
                c,
                (int) c,
                byteOffset));
      }
      byteBuffer.put((byte) c);
    }
    byteBuffer.put((byte) 0); // null terminator
    byteBuffer.flip();
    try {
      final int written = fileChannel.write(byteBuffer, byteOffset);
      if (written != length + 1) {
        throw new QctRuntimeException(String.format("Failed to write %d-byte null-terminated string to offset %d",
                                                    length + 1,
                                                    byteOffset));
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  /**
   * Writes a NULL-terminated string by first writing the string data at a target location,
   * then writing the pointer to that location.
   * <p>
   * Useful when you need to write the pointer first (or in a header) and the actual string later.
   *
   * @param fileChannel       to write to
   * @param pointerByteOffset byte offset where the pointer should be written
   * @param stringByteOffset  byte offset where the actual string data will be written
   * @param value             the string to write
   */
  public static void writeStringWithPointer(final FileChannel fileChannel,
                                            final int pointerByteOffset,
                                            final int stringByteOffset,
                                            final String value) {
    writeString(fileChannel, stringByteOffset, value);
    writePointer(fileChannel, pointerByteOffset, (int) stringByteOffset);
  }

  private QctWriter() {
  }
}
