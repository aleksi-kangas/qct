/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

import com.github.aleksikangas.qct.core.exception.QctRuntimeException;
import com.google.common.base.Preconditions;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple append-only allocator and writer for managing the "file body" section of a QCT file.
 * <p>
 * This class is responsible for reserving byte ranges inside a {@link FileChannel}
 * and writing variable-length data such as strings and binary blobs.
 * <p>
 * Features:
 * <ul>
 *   <li>Sequential (append-only) allocation strategy</li>
 *   <li>Configurable alignment for allocated regions</li>
 *   <li>Automatic string deduplication
 *   <li>Convenience methods for writing primitive values and pointers</li>
 *   <li>Thread-safe</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 *   <li>This writer assumes a maximum file size of &lt; 2GB due to 32-bit pointers.</li>
 *   <li>No deallocation or reuse of space is supported.</li>
 * </ul>
 *
 */
@ThreadSafe
public final class QctWriter {
  private final FileChannel fileChannel;
  private int currentOffset;

  /**
   * Cache used for string deduplication.
   * Maps string values to their allocated file offsets.
   */
  private final Map<String, Integer> stringPool = new HashMap<>();

  /**
   * Creates a new allocator starting at the given file offset.
   *
   * @param fileChannel     the file channel to write to
   * @param startByteOffset the starting offset of the file body
   * @throws NullPointerException if {@code fileChannel} is null
   */
  public QctWriter(final FileChannel fileChannel, final int startByteOffset) {
    this.fileChannel = Objects.requireNonNull(fileChannel);
    this.currentOffset = startByteOffset;
  }

  // ============================================================
  // Direct Write Methods (write to a given file offset)
  // ------------------------------------------------------------
  // These methods write values directly at the specified offset
  // in the file. They do NOT allocate space in the file body.
  // Use when you already know the target offset.
  // ============================================================

  /**
   * Writes a single unsigned byte at the given byte offset.
   *
   * @param byteOffset the byte offset where to write
   * @param value      the byte value to write (0-255)
   */
  public void writeByte(final int byteOffset, final int value) {
    writeBytes(byteOffset, new int[]{ value });
  }

  /**
   * Writes multiple unsigned bytes at the given byte offset.
   *
   * @param byteOffset the byte offset of the first byte
   * @param values     array of byte values to write (each 0-255)
   */
  public void writeBytes(final int byteOffset, final int[] values) {
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
   * @param byteOffset the byte offset where to write the double
   * @param value      the double value to write
   */
  public void writeDouble(final int byteOffset, final double value) {
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
   * @param byteOffset byte offset of the first double
   * @param values     array of double values to write
   */
  public void writeDoubles(final int byteOffset, final double[] values) {
    for (int i = 0; i < values.length; ++i) {
      writeDouble(Math.toIntExact(byteOffset + i * 8L), values[i]);
    }
  }

  /**
   * Writes an integer (4-byte, little-endian) at the given byte offset.
   *
   * @param byteOffset the byte offset where to write the integer
   * @param value      the integer value to write
   */
  public void writeInt(final int byteOffset, final int value) {
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
   * @param byteOffset the byte offset where to write the pointer
   * @param pointer    the pointer value (byte offset in the file)
   * @see #writeInt(int, int)
   */
  public void writePointer(final int byteOffset, final int pointer) {
    writeInt(byteOffset, pointer);
  }

  /**
   * Writes a NULL-terminated string at the given byte offset.
   * <p>
   * The string is written in ISO-8859-1 / extended ASCII (one byte per character),
   * followed by a null byte (0x00).
   *
   * @param byteOffset the byte offset where to write the string
   * @param value      the string to write (null is treated as empty string)
   */
  public void writeString(final int byteOffset, final String value) {
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

  // ============================================================
  // Allocate & Write Methods (write to body + return offset)
  // ------------------------------------------------------------
  // These methods first allocate space in the file body, then
  // write the value there. They return the starting offset of
  // the allocated block. Use when you need sequential storage.
  // ============================================================

  /**
   * Allocates a block of memory with no specific alignment (1-byte aligned).
   *
   * @param size number of bytes to allocate (must be &ge; 0)
   * @return the starting offset of the allocated block
   * @throws IllegalArgumentException if {@code size} is negative
   */
  public synchronized int allocate(final int size) {
    return allocate(size, 1);
  }

  /**
   * Allocates a block of memory with the specified alignment.
   * <p>
   * The returned offset will be aligned to the given boundary.
   *
   * @param size      number of bytes to allocate (must be &ge; 0)
   * @param alignment alignment in bytes (must be &gt; 0, typically power of two)
   * @return the starting offset of the allocated block
   * @throws IllegalArgumentException if size &lt; 0 or alignment &le; 0
   */
  public synchronized int allocate(final int size, final int alignment) {
    Preconditions.checkArgument(size >= 0, "size must be greater than or equal to 0");
    Preconditions.checkArgument(alignment > 0, "alignment must be positive");
    currentOffset = align(currentOffset, alignment);
    final int allocatedOffset = currentOffset;
    currentOffset += size;
    return allocatedOffset;
  }

  /**
   * Aligns the given value to the specified boundary.
   *
   * @param value     the value to align
   * @param alignment the alignment (must be a power of two for correct behavior)
   * @return the aligned value
   */
  private static int align(final int value, final int alignment) {
    return (value + alignment - 1) & -alignment;
  }

  /**
   * Allocates and writes a 4-byte little-endian integer to the body of the file.
   *
   * @param value     the integer value to write
   * @param alignment alignment in bytes
   * @return the file offset (pointer) where the integer is stored
   */
  public synchronized int allocateWriteInt(final int value, final int alignment) {
    final int offset = allocate(4, alignment);
    writeInt(offset, value);
    return offset;
  }

  /**
   * Allocates and writes an 8-byte little-endian double to the body of the file.
   *
   * @param value     the double value to write
   * @param alignment alignment in bytes
   * @return the file offset (pointer) where the double is stored
   */
  public synchronized int writePointerToDouble(final double value, final int alignment) {
    final int offset = allocate(8, alignment);
    writeDouble(offset, value);
    return offset;
  }

  /**
   * Allocates and writes a NULL-terminated string using ISO-8859-1 encoding.
   * <p>
   * This method performs string deduplication: if the same string has already
   * been written, the existing offset is returned instead of allocating new space.
   *
   * @param value the string to write (NULL is treated as empty string)
   * @return the file offset where the string is stored
   */
  public synchronized int allocateWriteString(final String value) {
    final String str = (value == null) ? "" : value;
    final Integer existing = stringPool.get(str);
    if (existing != null) {
      return existing;
    }
    final int size = str.length() + 1;  // NULL terminator
    final int offset = allocate(size, 1);
    writeString(offset, str);
    stringPool.put(str, offset);
    return offset;
  }

  /**
   * Allocates a string (with deduplication) and writes a pointer to it.
   *
   * @param pointerByteOffset the file offset where the pointer should be written
   * @param stringValue       the string value
   */
  public void allocateWriteString(final int pointerByteOffset, final String stringValue) {
    final int offset = allocateWriteString(stringValue);
    writePointer(pointerByteOffset, offset);
  }

  /**
   * Returns the current end offset of the allocated region.
   *
   * @return the next free byte offset
   */
  public int getCurrentOffset() {
    return currentOffset;
  }

  /**
   * Returns the number of unique strings stored in the allocator.
   *
   * @return number of deduplicated strings
   */
  public int getUniqueStringCount() {
    return stringPool.size();
  }
}
