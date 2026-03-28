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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DatumShiftTest {
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
    void decode() throws IOException {
      final double expectedNorth = 123.456789;
      final double expectedEast = -987.654321;
      final Path tempFile = Files.createTempFile("datum-shift-decode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeDouble(fileChannel, 0, expectedNorth);
        QctWriter.writeDouble(fileChannel, 8, expectedEast);

        final DatumShift datumShift = DatumShift.Decoder.decode(fileChannel, 0);

        assertEquals(expectedNorth, datumShift.north(), 1e-10);
        assertEquals(expectedEast, datumShift.east(), 1e-10);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeLargeOffset() throws IOException {
      final double north = 42.0;
      final double east = 24.0;
      final Path tempFile = Files.createTempFile("datum-shift-decode-offset", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        long offset = 1024 * 1024; // 1MB offset
        QctWriter.writeDouble(fileChannel, offset, north);
        QctWriter.writeDouble(fileChannel, offset + 8, east);

        final DatumShift datumShift = DatumShift.Decoder.decode(fileChannel, offset);

        assertEquals(north, datumShift.north());
        assertEquals(east, datumShift.east());
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
      final var datumShift = new DatumShift(111.222, -333.444);
      final Path tempFile = Files.createTempFile("datum-shift-encode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        DatumShift.Encoder.encode(datumShift, fileChannel, 0);

        final DatumShift decodedDatumShift = DatumShift.Decoder.decode(fileChannel, 0);

        assertEquals(datumShift.north(), decodedDatumShift.north(), 1e-10);
        assertEquals(datumShift.east(), decodedDatumShift.east(), 1e-10);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Test
  void roundTrip() throws IOException {
    final long byteOffset = 100;
    final var datumShift = new DatumShift(9876.54321, -1234.56789);
    final Path tempFile = Files.createTempFile("datum-shift-round-trip", ".bin");
    try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      DatumShift.Encoder.encode(datumShift, fileChannel, byteOffset);
      final DatumShift decodedDatumShift = DatumShift.Decoder.decode(fileChannel, byteOffset);

      assertEquals(datumShift, decodedDatumShift);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
