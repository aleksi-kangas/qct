/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

import com.github.aleksikangas.qct.core.exception.QctRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

final class QctWriterTest {
  private FileChannel mockFileChannel;

  @BeforeEach
  void setUp() {
    mockFileChannel = mock(FileChannel.class);
  }

  @Test
  void writeByte() throws IOException {
    final int value = 42;
    when(mockFileChannel.write(any(ByteBuffer.class), eq(0L))).thenReturn(1);

    QctWriter.writeByte(mockFileChannel, 0, value);

    verify(mockFileChannel, times(1)).write(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void writeBytes() throws IOException {
    final int[] values = { 4, 8, 15, 16, 23, 42 };
    when(mockFileChannel.write(any(ByteBuffer.class), eq(0L))).thenReturn(values.length);

    QctWriter.writeBytes(mockFileChannel, 0, values);

    verify(mockFileChannel, times(1)).write(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void writeBytes_partialWrite_throwsException() throws IOException {
    final int[] values = { 1, 2, 3 };
    when(mockFileChannel.write(any(ByteBuffer.class), eq(10L))).thenReturn(2);

    QctRuntimeException exception = assertThrows(QctRuntimeException.class,
                                                 () -> QctWriter.writeBytes(mockFileChannel, 10, values));

    assertTrue(exception.getMessage().contains("Failed to write 3 bytes"));
    verify(mockFileChannel, times(1)).write(any(ByteBuffer.class), eq(10L));
  }

  @Test
  void writeDouble() throws IOException {
    final double value = 3.141592653589793;
    when(mockFileChannel.write(any(ByteBuffer.class), eq(0L))).thenReturn(8);

    QctWriter.writeDouble(mockFileChannel, 0, value);

    verify(mockFileChannel).write(argThat(byteBuffer -> byteBuffer.order() == ByteOrder.LITTLE_ENDIAN &&
                                                        Math.abs(byteBuffer.getDouble(0) - value) < 1e-10), eq(0L));

    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void writeDoubles() throws IOException {
    final double[] values = { 3.141592653589793, 2.71828, -1.0, 42.0 };
    when(mockFileChannel.write(any(ByteBuffer.class), anyLong())).thenReturn(8);

    QctWriter.writeDoubles(mockFileChannel, 100, values);

    verify(mockFileChannel, times(values.length)).write(any(ByteBuffer.class), anyLong());
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void writeInt() throws IOException {
    final int value = 0x12345678;
    when(mockFileChannel.write(any(ByteBuffer.class), eq(0L))).thenReturn(4);

    QctWriter.writeInt(mockFileChannel, 0, value);

    verify(mockFileChannel).write(argThat(byteBuffer -> byteBuffer.order() == ByteOrder.LITTLE_ENDIAN &&
                                                        byteBuffer.getInt(0) == value), eq(0L));

    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void writePointer() throws IOException {
    final int pointer = 0xABCDEF00;
    when(mockFileChannel.write(any(ByteBuffer.class), eq(64L))).thenReturn(4);

    QctWriter.writePointer(mockFileChannel, 64, pointer);

    verify(mockFileChannel, times(1)).write(any(ByteBuffer.class), eq(64L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void writeString() throws IOException {
    final String value = "Hello, QCT!";
    when(mockFileChannel.write(any(ByteBuffer.class), eq(0L))).thenReturn(value.length() + 1);

    QctWriter.writeString(mockFileChannel, 0, value);

    verify(mockFileChannel).write(argThat(byteBuffer -> {
      final ByteBuffer duplicate = byteBuffer.duplicate();
      final StringBuilder stringBuilder = new StringBuilder();
      while (duplicate.hasRemaining()) {
        byte b = duplicate.get();
        if (b == 0) {
          break;
        }
        stringBuilder.append((char) (b & 0xFF));
      }
      return stringBuilder.toString().equals(value);
    }), eq(0L));

    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void writeString_empty() throws IOException {
    when(mockFileChannel.write(any(ByteBuffer.class), eq(50L))).thenReturn(1);

    QctWriter.writeString(mockFileChannel, 50, "");

    verify(mockFileChannel, times(1)).write(any(ByteBuffer.class), eq(50L));
  }

  @Test
  void writeString_null_treatedAsEmpty() throws IOException {
    when(mockFileChannel.write(any(ByteBuffer.class), eq(100L))).thenReturn(1);

    QctWriter.writeString(mockFileChannel, 100, null);

    verify(mockFileChannel, times(1)).write(any(ByteBuffer.class), eq(100L));
  }

  @Test
  void writeString_nonAsciiCharacter_throwsException() {
    String badString = "Hello 😊";

    QctRuntimeException exception = assertThrows(QctRuntimeException.class,
                                                 () -> QctWriter.writeString(mockFileChannel, 0, badString));

    assertTrue(exception.getMessage().contains("cannot be written as single byte"));
  }

  @Test
  void writeString_partialWrite_throwsException() throws IOException {
    final String value = "Test";
    when(mockFileChannel.write(any(ByteBuffer.class), eq(0))).thenReturn(3);

    QctRuntimeException ex = assertThrows(QctRuntimeException.class,
                                          () -> QctWriter.writeString(mockFileChannel, 0, value));

    assertTrue(ex.getMessage().contains("Failed to write"));
  }

  @Test
  void writeStringWithPointer() throws IOException {
    final String value = "Pointer String Test";
    final int pointerOffset = 0;
    final int stringOffset = 100;
    when(mockFileChannel.write(any(ByteBuffer.class), anyLong())).thenReturn(value.length() + 1).thenReturn(4);

    QctWriter.writeStringWithPointer(mockFileChannel, pointerOffset, stringOffset, value);

    verify(mockFileChannel, times(1)).write(any(ByteBuffer.class), eq((long) stringOffset));
    verify(mockFileChannel, times(1)).write(any(ByteBuffer.class), eq((long) pointerOffset));

    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void writeFailsWithIOException_wrappedInQctRuntimeException() throws IOException {
    when(mockFileChannel.write(any(ByteBuffer.class), anyLong())).thenThrow(new IOException("Disk full"));

    QctRuntimeException ex = assertThrows(QctRuntimeException.class, () -> QctWriter.writeInt(mockFileChannel, 0, 42));

    assertInstanceOf(IOException.class, ex.getCause());
    assertEquals("Disk full", ex.getCause().getMessage());
  }
}
