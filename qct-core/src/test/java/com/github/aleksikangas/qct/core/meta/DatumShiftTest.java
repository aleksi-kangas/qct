/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.DirectQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DatumShiftTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("datum-shift", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, DatumShift.SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @Nested
  @DisplayName("DatumShift")
  class RecordTests {
    @Test
    void constructor() {
      final var datumShift = new DatumShift(12.345, -67.89);
      assertEquals(12.345, datumShift.north());
      assertEquals(-67.89, datumShift.east());
    }

    @Test
    void immutable() {
      final var datumShift = new DatumShift(1.0, 2.0);
      assertEquals(1.0, datumShift.north());
      assertEquals(2.0, datumShift.east());
    }

    @Test
    void equalsAndHashCode() {
      final var a = new DatumShift(5.0, 10.0);
      final var b = new DatumShift(5.0, 10.0);
      final var c = new DatumShift(5.1, 10.0);
      assertEquals(a, b);
      assertNotEquals(a, c);
      assertEquals(a.hashCode(), b.hashCode());
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decode() {
      final double expectedNorth = 123.456789;
      final double expectedEast = -987.654321;
      qctWriter.writeDouble(0, expectedNorth);
      qctWriter.writeDouble(8, expectedEast);

      final DatumShift datumShift = DatumShift.Decoder.decode(qctReader, 0);

      assertEquals(expectedNorth, datumShift.north(), 1e-10);
      assertEquals(expectedEast, datumShift.east(), 1e-10);
    }

    @Test
    void decodeLargeOffset() {
      final double north = 42.0;
      final double east = 24.0;
      final int offset = 1024 * 1024; // 1MB offset
      qctWriter.writeDouble(offset, north);
      qctWriter.writeDouble(offset + 8, east);

      final DatumShift datumShift = DatumShift.Decoder.decode(qctReader, offset);

      assertEquals(north, datumShift.north());
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encode() {
      final var datumShift = new DatumShift(111.222, -333.444);
      DatumShift.Encoder.encode(qctWriter, datumShift, 0);

      final DatumShift decodedDatumShift = DatumShift.Decoder.decode(qctReader, 0);

      assertEquals(datumShift.north(), decodedDatumShift.north(), 1e-10);
      assertEquals(datumShift.east(), decodedDatumShift.east(), 1e-10);
    }
  }

  @Test
  void roundTrip() {
    final int byteOffset = 100;
    final var datumShift = new DatumShift(9876.54321, -1234.56789);

    DatumShift.Encoder.encode(qctWriter, datumShift, byteOffset);
    final DatumShift decodedDatumShift = DatumShift.Decoder.decode(qctReader, byteOffset);

    assertEquals(datumShift, decodedDatumShift);
  }
}
