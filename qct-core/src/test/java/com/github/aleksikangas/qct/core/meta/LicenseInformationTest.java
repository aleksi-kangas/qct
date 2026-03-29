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

class LicenseInformationTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("license-info", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new QctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, LicenseInformation.HEADER_SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  private int[] serialBytes() {
    final int[] bytes = new int[SerialNumber.SIZE];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = i;
    }
    return bytes;
  }

  private LicenseInformation createSample() {
    return new LicenseInformation(12345, new SerialNumber(serialBytes()));
  }

  @Nested
  @DisplayName("LicenseInformation")
  class RecordTests {

    @Test
    void constructor() {
      final var li = createSample();

      assertEquals(12345, li.identifier());
      assertArrayEquals(serialBytes(), li.serialNumber().bytes());
    }

    @Test
    void equalsAndHashCode() {
      final var a = createSample();
      final var b = createSample();

      final var different = new LicenseInformation(999, new SerialNumber(serialBytes()));

      assertEquals(a, b);
      assertNotEquals(a, different);
      assertEquals(a.hashCode(), b.hashCode());
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {

    @Test
    void decode() {
      final int byteOffset = 0;
      final int[] serial = serialBytes();

      qctWriter.writeInt(byteOffset, 12345);

      qctWriter.allocateWriteString(Math.toIntExact(byteOffset + 0x0CL), "Test License");

      final int serialPtr = qctWriter.allocate(SerialNumber.SIZE);
      qctWriter.writePointer(Math.toIntExact(byteOffset + 0x10L), serialPtr);
      qctWriter.writeBytes(serialPtr, serial);

      final LicenseInformation decoded = LicenseInformation.Decoder.decode(qctReader, byteOffset);

      assertEquals(12345, decoded.identifier());
      assertArrayEquals(serial, decoded.serialNumber().bytes());
    }

    @Test
    void decodeLargeOffset() {
      final int byteOffset = 1024 * 1024;

      qctWriter.writeInt(byteOffset, 42);
      qctWriter.allocateWriteString(byteOffset + 0x0C, "Large Offset");

      final int serialPtr = qctWriter.allocate(SerialNumber.SIZE);
      qctWriter.writePointer(byteOffset + 0x10, serialPtr);
      qctWriter.writeBytes(serialPtr, serialBytes());

      final LicenseInformation decoded = LicenseInformation.Decoder.decode(qctReader, byteOffset);

      assertEquals(42, decoded.identifier());
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encode() {
      final var original = createSample();

      LicenseInformation.Encoder.encode(qctWriter, original, 0);
      final var decoded = LicenseInformation.Decoder.decode(qctReader, 0);

      assertEquals(original.identifier(), decoded.identifier());
      assertEquals(original.serialNumber(), decoded.serialNumber());
    }

    @Test
    void zeroPadding() {
      final var original = createSample();

      LicenseInformation.Encoder.encode(qctWriter, original, 0);

      // 16 bytes at 0x18
      for (int i = 0; i < 16; i++) {
        assertEquals(0, qctReader.readByte(0x18 + i));
      }

      // 64 bytes at 0x28
      for (int i = 0; i < 64; i++) {
        assertEquals(0, qctReader.readByte(0x28 + i));
      }
    }
  }

  @Test
  void roundTrip() {
    final int byteOffset = 0;
    final var original = createSample();

    LicenseInformation.Encoder.encode(qctWriter, original, byteOffset);
    final var decoded = LicenseInformation.Decoder.decode(qctReader, byteOffset);

    assertEquals(original, decoded);
  }
}