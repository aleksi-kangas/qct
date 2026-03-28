/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalMatchers.geq;
import static org.mockito.Mockito.*;

final class QctReaderTest {
  private FileChannel mockFileChannel;
  private QctReader qctReader;

  @BeforeEach
  void setUp() {
    mockFileChannel = mock(FileChannel.class);
    qctReader = new QctReader(mockFileChannel);
  }

  @Test
  void readByte() throws IOException {
    final int expectedByte = 42;

    when(mockFileChannel.read(any(ByteBuffer.class), eq(0L))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      byteBuffer.put((byte) (expectedByte));
      return 1;
    });

    final int readByte = qctReader.readByte(0);

    assertEquals(expectedByte, readByte);
    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readBytes() throws IOException {
    final var expectedBytes = new int[]{ 4, 8, 15, 16, 23, 42 };

    when(mockFileChannel.read(any(ByteBuffer.class), eq(0L))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      for (final int expectedByte : expectedBytes) {
        byteBuffer.put((byte) (expectedByte));
      }
      return expectedBytes.length;
    });

    final int[] bytes = qctReader.readBytes(0, expectedBytes.length);

    assertArrayEquals(expectedBytes, bytes);
    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readBytesSafe() throws IOException {
    final var expectedBytes = new int[]{ 4, 8, 15, 16 };
    final int requestedCount = 6;

    when(mockFileChannel.read(any(ByteBuffer.class), eq(0L))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      for (final int expectedByte : expectedBytes) {
        byteBuffer.put((byte) (expectedByte));
      }
      return expectedBytes.length;
    });

    final int[] bytes = qctReader.readBytesSafe(0, requestedCount);

    assertArrayEquals(expectedBytes, bytes);
    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readDouble() throws IOException {
    final double expectedDouble = 3.141592653589793;

    when(mockFileChannel.read(any(ByteBuffer.class), eq(0L))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.putDouble(expectedDouble);
      return 8;
    });

    final double readDouble = qctReader.readDouble(0);

    assertEquals(expectedDouble, readDouble, 0.0);
    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readDoubles() throws IOException {
    final var expectedDoubles = new double[]{ 3.141592653589793, 8.0, -1.0, 1.0 };

    when(mockFileChannel.read(any(ByteBuffer.class), anyLong())).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      final long byteOffset = (long) invocation.getArguments()[1];
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.putDouble(expectedDoubles[Math.toIntExact(byteOffset / 8)]);
      return 8;
    });

    final double[] readDoubles = qctReader.readDoubles(0, expectedDoubles.length);

    assertArrayEquals(expectedDoubles, readDoubles);
    verify(mockFileChannel, times(expectedDoubles.length)).read(any(ByteBuffer.class), anyLong());
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readInt() throws IOException {
    final int expectedInt = 42;

    when(mockFileChannel.read(any(ByteBuffer.class), eq(0L))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.putInt(expectedInt);
      return 4;
    });

    final int readInt = qctReader.readInt(0);

    assertEquals(expectedInt, readInt);
    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readPointer() throws IOException {
    final int expectedPointer = 42;

    when(mockFileChannel.read(any(ByteBuffer.class), eq(0L))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.putInt(expectedPointer);
      return 4;
    });

    final int readPointer = qctReader.readPointer(0);

    assertEquals(expectedPointer, readPointer);
    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readString() throws IOException {
    final String expectedString = "Hello, QCT!";

    when(mockFileChannel.read(any(ByteBuffer.class), anyLong())).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      final long byteOffset = (long) invocation.getArguments()[1];
      final int charIndex = (int) byteOffset;
      if (charIndex < expectedString.length()) {
        byteBuffer.put((byte) expectedString.charAt(charIndex));
        return 1;
      } else if (charIndex == expectedString.length()) {
        byteBuffer.put((byte) 0);
        return 1;
      }
      throw new IOException();
    });

    final String result = qctReader.readString(0);

    assertEquals(expectedString, result);
    verify(mockFileChannel, times(expectedString.length() + 1)).read(any(ByteBuffer.class), anyLong());
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readString_empty() throws IOException {
    when(mockFileChannel.read(any(ByteBuffer.class), eq(0L))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      byteBuffer.put((byte) 0);
      return 1;
    });

    final String result = qctReader.readString(0);

    assertEquals("", result);
    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readStringFromPointer() throws IOException {
    final String expectedString = "Pointer Test";
    final int pointerValue = 100;
    final int pointerOffset = 0;

    when(mockFileChannel.read(any(ByteBuffer.class), eq((long) pointerOffset))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArgument(0);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.putInt((int) pointerValue);
      return 4;
    });

    when(mockFileChannel.read(any(ByteBuffer.class), geq((long) pointerValue))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArgument(0);
      final long byteOffset = invocation.getArgument(1);
      final int charIndex = (int) (byteOffset - pointerValue);
      if (charIndex < expectedString.length()) {
        byteBuffer.put((byte) expectedString.charAt(charIndex));
        return 1;
      } else if (charIndex == expectedString.length()) {
        byteBuffer.put((byte) 0);
        return 1;
      }
      throw new IOException();
    });

    final String result = qctReader.readStringFromPointer(pointerOffset);

    assertEquals(expectedString, result);

    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq((long) pointerOffset));
    verify(mockFileChannel, times(expectedString.length() + 1 + 1)).read(any(ByteBuffer.class), anyLong());
    verifyNoMoreInteractions(mockFileChannel);
  }

  @Test
  void readStringFromPointer_nullPointer() throws IOException {
    final int nullPointer = 0;

    when(mockFileChannel.read(any(ByteBuffer.class), eq(0L))).thenAnswer(invocation -> {
      final var byteBuffer = (ByteBuffer) invocation.getArguments()[0];
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.putInt(nullPointer);
      return 4;
    });

    final String result = qctReader.readStringFromPointer(0);

    assertEquals("", result);
    verify(mockFileChannel, times(1)).read(any(ByteBuffer.class), eq(0L));
    verifyNoMoreInteractions(mockFileChannel);
  }
}
