/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

public interface QctReader {
  /**
   * Reads a single unsigned byte from the given byte offset.
   *
   * @param byteOffset the byte offset of the byte
   * @return read byte
   */
  int readByte(final int byteOffset);

  /**
   * Reads multiple unsigned bytes from the given byte offset.
   *
   * @param byteOffset the byte offset of the first byte
   * @param count      bytes to read
   * @return read bytes
   */
  int[] readBytes(final int byteOffset, final int count);

  /**
   * Reads multiple unsigned bytes from the given byte offset, until count or EOF.
   *
   * @param byteOffset the byte offset of the first byte
   * @param count      bytes to read
   * @return read bytes, until count or EOF
   */
  int[] readBytesSafe(final int byteOffset, final int count);

  /**
   * Reads a double (8 byte IEEE-754) from the given byte offset.
   *
   * @param byteOffset the byte offset of the double
   * @return read double
   */
  double readDouble(final int byteOffset);

  /**
   * Reads multiple doubles (8 byte IEEE-754) from the given byte offset.
   *
   * @param byteOffset byte offset of the first double
   * @param count      doubles to read
   * @return read doubles
   */
  double[] readDoubles(final int byteOffset, final int count);

  /**
   * Reads an integer (4-byte) stored as little-endian from the given byte offset.
   *
   * @param byteOffset the byte offset of the integer
   * @return read integer
   */
  int readInt(final int byteOffset);

  /**
   * Reads an integer (4-byte) pointer stored as little-endian from the given byte offset. Pointers are essentially byte
   * offsets within the file.
   *
   * @param byteOffset the byte offset of the pointer
   * @return read pointer
   * @see #readInt(int)
   */
  int readPointer(final int byteOffset);

  /**
   * Reads a NULL-terminated string from the given byte offset.
   *
   * @param byteOffset byte offset of the string
   * @return read string
   */
  String readString(final int byteOffset);

  /**
   * Reads a NULL-terminated string by first reading the string pointer from the given byte offset, and then reading the
   * string from the pointed byte offset.
   *
   * @param pointerByteOffset byte offset of the string pointer
   * @return read string
   */
  String readStringFromPointer(final int pointerByteOffset);
}
