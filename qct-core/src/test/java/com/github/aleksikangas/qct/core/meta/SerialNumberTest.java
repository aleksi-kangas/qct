/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class SerialNumberTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("file-format-version", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new QctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, SerialNumber.SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @Nested
  @DisplayName("SerialNumber")
  class RecordTests {
    @Test
    void constructor() {
      final int[] data = { 1,
                           2,
                           3,
                           4,
                           5,
                           6,
                           7,
                           8,
                           9,
                           10,
                           11,
                           12,
                           13,
                           14,
                           15,
                           16,
                           17,
                           18,
                           19,
                           20,
                           21,
                           22,
                           23,
                           24,
                           25,
                           26,
                           27,
                           28,
                           29,
                           30,
                           31,
                           32 };
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
      final int[] data = { 65,
                           66,
                           67,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0,
                           0 };
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
    void decode() {
      final int[] expectedBytes = new int[32];
      for (int i = 0; i < 32; i++) {
        expectedBytes[i] = (i + 100) % 256; // values 100..131
      }
      qctWriter.writeBytes(0, expectedBytes);
      final SerialNumber serialNumber = SerialNumber.Decoder.decode(qctReader, 0);
      assertArrayEquals(expectedBytes, serialNumber.bytes());
    }

    @Test
    void decodeLargeOffset() {
      final int[] expectedBytes = new int[32];
      for (int i = 0; i < 32; i++) {
        expectedBytes[i] = i * 7 % 200;
      }
      final int offset = 1024 * 1024 + 512; // 1MB + 512 bytes
      qctWriter.writeBytes(offset, expectedBytes);
      final SerialNumber serialNumber = SerialNumber.Decoder.decode(qctReader, offset);
      assertArrayEquals(expectedBytes, serialNumber.bytes());
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encode() {
      final int[] originalData = new int[32];
      for (int i = 0; i < 32; i++) {
        originalData[i] = (i * 11) % 251;
      }
      final var serialNumber = new SerialNumber(originalData);
      SerialNumber.Encoder.encode(qctWriter, serialNumber, 0);
      final SerialNumber decoded = SerialNumber.Decoder.decode(qctReader, 0);
      assertArrayEquals(originalData, decoded.bytes());
    }
  }

  @Test
  void roundTrip() {
    final int byteOffset = 2048;
    final int[] originalData = new int[32];
    for (int i = 0; i < 32; i++) {
      originalData[i] = (i + 50) * 3 % 256;
    }
    final var original = new SerialNumber(originalData);
    SerialNumber.Encoder.encode(qctWriter, original, byteOffset);
    final SerialNumber decoded = SerialNumber.Decoder.decode(qctReader, byteOffset);
    assertEquals(original, decoded);
    assertArrayEquals(originalData, decoded.bytes());
  }
}
