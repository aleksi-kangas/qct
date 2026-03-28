/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class SerialNumberTest {

  @Nested
  @DisplayName("SerialNumber")
  class RecordTests {

    @Test
    void constructor() {
      final int[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 };
      final var serialNumber = new SerialNumber(data);

      assertArrayEquals(data, serialNumber.bytes());
      assertThrows(NullPointerException.class, () -> new SerialNumber(null));
    }

    @Test
    void equalsAndHashCode() {
      final int[] data1 = new int[32];
      final int[] data2 = new int[32];
      final int[] data3 = new int[32];

      for (int i = 0; i < 32; i++) {
        data1[i] = i * 3;
        data2[i] = i * 3;
        data3[i] = i * 5;
      }

      final var a = new SerialNumber(data1);
      final var b = new SerialNumber(data2);
      final var c = new SerialNumber(data3);

      assertEquals(a, b);
      assertNotEquals(a, c);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringTest() {
      final int[] data = { 65, 66, 67, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
      final var serialNumber = new SerialNumber(data);
      final String str = serialNumber.toString();

      assertTrue(str.contains("[65, 66, 67, 0,"));
      assertTrue(str.endsWith("]"));
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decode() throws IOException {
      final int[] expectedBytes = new int[32];
      for (int i = 0; i < 32; i++) {
        expectedBytes[i] = (i + 100) % 256; // values 100..131
      }
      final Path tempFile = Files.createTempFile("serial-number-decode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeBytes(fileChannel, 0, expectedBytes);

        final SerialNumber serialNumber = SerialNumber.Decoder.decode(fileChannel, 0);

        assertArrayEquals(expectedBytes, serialNumber.bytes());
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeLargeOffset() throws IOException {
      final int[] expectedBytes = new int[32];
      for (int i = 0; i < 32; i++) {
        expectedBytes[i] = i * 7 % 200;
      }
      final Path tempFile = Files.createTempFile("serial-number-decode-offset", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        final long offset = 1024 * 1024 + 512; // 1MB + 512 bytes
        QctWriter.writeBytes(fileChannel, offset, expectedBytes);

        final SerialNumber serialNumber = SerialNumber.Decoder.decode(fileChannel, offset);

        assertArrayEquals(expectedBytes, serialNumber.bytes());
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encode() throws IOException {
      final int[] originalData = new int[32];
      for (int i = 0; i < 32; i++) {
        originalData[i] = (i * 11) % 251;
      }
      final var serialNumber = new SerialNumber(originalData);
      final Path tempFile = Files.createTempFile("serial-number-encode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        SerialNumber.Encoder.encode(serialNumber, fileChannel, 0);

        final SerialNumber decoded = SerialNumber.Decoder.decode(fileChannel, 0);

        assertArrayEquals(originalData, decoded.bytes());
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Test
  void roundTrip() throws IOException {
    final long byteOffset = 2048;
    final int[] originalData = new int[32];
    for (int i = 0; i < 32; i++) {
      originalData[i] = (i + 50) * 3 % 256;
    }
    final var original = new SerialNumber(originalData);
    final Path tempFile = Files.createTempFile("serial-number-round-trip", ".bin");
    try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {

      SerialNumber.Encoder.encode(original, fileChannel, byteOffset);
      final SerialNumber decoded = SerialNumber.Decoder.decode(fileChannel, byteOffset);

      assertEquals(original, decoded);
      assertArrayEquals(originalData, decoded.bytes());
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
