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

class MapOutlineTest {
  @Nested
  @DisplayName("MapOutline")
  class RecordTests {
    @Test
    void constructor() {
      final var points = new MapOutline.Point[]{ new MapOutline.Point(60.0, 24.0), new MapOutline.Point(61.0, 25.0) };
      final var mapOutline = new MapOutline(points);

      assertEquals(2, mapOutline.points().length);
      assertEquals(60.0, mapOutline.points()[0].latitude());
      assertEquals(24.0, mapOutline.points()[0].longitude());
      assertEquals(61.0, mapOutline.points()[1].latitude());
      assertEquals(25.0, mapOutline.points()[1].longitude());
    }

    @Test
    void immutable() {
      final var points = new MapOutline.Point[]{ new MapOutline.Point(50.0, 10.0) };
      final var mapOutline = new MapOutline(points);

      assertEquals(1, mapOutline.points().length);
    }

    @Test
    void equalsAndHashCode() {
      final var points1 = new MapOutline.Point[]{ new MapOutline.Point(60.1699, 24.9384),
                                                  new MapOutline.Point(60.1700, 24.9385) };
      final var points2 = new MapOutline.Point[]{ new MapOutline.Point(60.1699, 24.9384),
                                                  new MapOutline.Point(60.1700, 24.9385) };
      final var points3 = new MapOutline.Point[]{ new MapOutline.Point(60.1699, 24.9384) };

      final var a = new MapOutline(points1);
      final var b = new MapOutline(points2);
      final var c = new MapOutline(points3);

      assertEquals(a, b);
      assertNotEquals(a, c);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringTest() {
      final var points = new MapOutline.Point[]{ new MapOutline.Point(60.0, 24.0) };
      final var mapOutline = new MapOutline(points);

      assertTrue(mapOutline.toString().contains("60.0"));
      assertTrue(mapOutline.toString().contains("24.0"));
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decode() throws IOException {
      final MapOutline.Point[] expectedPoints = { new MapOutline.Point(60.1699, 24.9384),
                                                  new MapOutline.Point(61.4978, 23.7608),
                                                  new MapOutline.Point(60.4518, 25.1214) };

      final Path tempFile = Files.createTempFile("map-outline-decode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, expectedPoints.length);
        QctWriter.writePointer(fileChannel, 4, 16);

        long arrayOffset = 16;
        for (int i = 0; i < expectedPoints.length; ++i) {
          final long pointOffset = arrayOffset + i * 16L;
          QctWriter.writeDouble(fileChannel, pointOffset, expectedPoints[i].latitude());
          QctWriter.writeDouble(fileChannel, pointOffset + 8, expectedPoints[i].longitude());
        }

        final MapOutline mapOutline = MapOutline.Decoder.decode(fileChannel, 0);

        assertEquals(expectedPoints.length, mapOutline.points().length);
        for (int i = 0; i < expectedPoints.length; ++i) {
          assertEquals(expectedPoints[i].latitude(), mapOutline.points()[i].latitude(), 1e-10);
          assertEquals(expectedPoints[i].longitude(), mapOutline.points()[i].longitude(), 1e-10);
        }
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeLargeOffset() throws IOException {
      final MapOutline.Point[] points = { new MapOutline.Point(65.0, 25.0), new MapOutline.Point(66.0, 26.0) };
      final Path tempFile = Files.createTempFile("map-outline-decode-offset", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        final long baseOffset = 1024 * 1024; // 1MB offset
        QctWriter.writeInt(fileChannel, baseOffset, points.length);
        QctWriter.writePointer(fileChannel, baseOffset + 4, (int) (baseOffset + 16));

        long arrayOffset = baseOffset + 16;
        for (int i = 0; i < points.length; ++i) {
          final long pointOffset = arrayOffset + i * 16L;
          QctWriter.writeDouble(fileChannel, pointOffset, points[i].latitude());
          QctWriter.writeDouble(fileChannel, pointOffset + 8, points[i].longitude());
        }

        final MapOutline mapOutline = MapOutline.Decoder.decode(fileChannel, baseOffset);

        assertEquals(points.length, mapOutline.points().length);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeEmpty() throws IOException {
      final Path tempFile = Files.createTempFile("map-outline-empty", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0);
        QctWriter.writePointer(fileChannel, 4, 16);

        final MapOutline mapOutline = MapOutline.Decoder.decode(fileChannel, 0);

        assertEquals(0, mapOutline.points().length);
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
      final MapOutline.Point[] points = { new MapOutline.Point(60.1699, 24.9384),
                                          new MapOutline.Point(61.4978, 23.7608) };
      final var mapOutline = new MapOutline(points);
      final Path tempFile = Files.createTempFile("map-outline-encode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {

        MapOutline.Encoder.encode(mapOutline, fileChannel, 0);

        final MapOutline decoded = MapOutline.Decoder.decode(fileChannel, 0);

        assertEquals(mapOutline, decoded);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void encodeEmpty() throws IOException {
      final var mapOutline = new MapOutline(new MapOutline.Point[0]);
      final Path tempFile = Files.createTempFile("map-outline-encode-empty", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {

        MapOutline.Encoder.encode(mapOutline, fileChannel, 0);

        final MapOutline decoded = MapOutline.Decoder.decode(fileChannel, 0);

        assertEquals(0, decoded.points().length);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Test
  void roundTrip() throws IOException {
    final long byteOffset = 512;
    final MapOutline.Point[] originalPoints = { new MapOutline.Point(59.43696, 24.75357),
                                                new MapOutline.Point(60.16985, 24.93838),
                                                new MapOutline.Point(61.05437, 28.18871) };
    final var mapOutline = new MapOutline(originalPoints);
    final Path tempFile = Files.createTempFile("map-outline-round-trip", ".bin");
    try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {

      MapOutline.Encoder.encode(mapOutline, fileChannel, byteOffset);
      final MapOutline decoded = MapOutline.Decoder.decode(fileChannel, byteOffset);

      assertEquals(mapOutline, decoded);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
