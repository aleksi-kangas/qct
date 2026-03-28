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

class MapOutlineTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("file-format-version", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new QctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, 1024);  // Arbitrarily large size
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

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
    void decode() {
      final MapOutline.Point[] expectedPoints = { new MapOutline.Point(60.1699, 24.9384),
                                                  new MapOutline.Point(61.4978, 23.7608),
                                                  new MapOutline.Point(60.4518, 25.1214) };
      qctWriter.writeInt(0, expectedPoints.length);
      qctWriter.writePointer(4, 16);

      final int arrayOffset = 16;
      for (int i = 0; i < expectedPoints.length; ++i) {
        final int pointOffset = arrayOffset + i * 16;
        qctWriter.writeDouble(pointOffset, expectedPoints[i].latitude());
        qctWriter.writeDouble(pointOffset + 8, expectedPoints[i].longitude());
      }

      final MapOutline mapOutline = MapOutline.Decoder.decode(qctReader, 0);

      assertEquals(expectedPoints.length, mapOutline.points().length);
      for (int i = 0; i < expectedPoints.length; ++i) {
        assertEquals(expectedPoints[i].latitude(), mapOutline.points()[i].latitude(), 1e-10);
        assertEquals(expectedPoints[i].longitude(), mapOutline.points()[i].longitude(), 1e-10);
      }
    }

    @Test
    void decodeLargeOffset() {
      final MapOutline.Point[] points = { new MapOutline.Point(65.0, 25.0), new MapOutline.Point(66.0, 26.0) };
      final int baseOffset = 1024 * 1024; // 1MB offset
      qctWriter.writeInt(baseOffset, points.length);
      qctWriter.writePointer(baseOffset + 4, baseOffset + 16);

      final int arrayOffset = baseOffset + 16;
      for (int i = 0; i < points.length; ++i) {
        final int pointOffset = arrayOffset + i * 16;
        qctWriter.writeDouble(pointOffset, points[i].latitude());
        qctWriter.writeDouble(pointOffset + 8, points[i].longitude());
      }

      final MapOutline mapOutline = MapOutline.Decoder.decode(qctReader, baseOffset);

      assertEquals(points.length, mapOutline.points().length);
    }

    @Test
    void decodeEmpty() {
      qctWriter.writeInt(0, 0);
      qctWriter.writePointer(4, 16);

      final MapOutline mapOutline = MapOutline.Decoder.decode(qctReader, 0);

      assertEquals(0, mapOutline.points().length);
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encode() {
      final MapOutline.Point[] points = { new MapOutline.Point(60.1699, 24.9384),
                                          new MapOutline.Point(61.4978, 23.7608) };
      final var mapOutline = new MapOutline(points);
      MapOutline.Encoder.encode(qctWriter, mapOutline, 0);
      final MapOutline decoded = MapOutline.Decoder.decode(qctReader, 0);
      assertEquals(mapOutline, decoded);
    }

    @Test
    void encodeEmpty() {
      final var mapOutline = new MapOutline(new MapOutline.Point[0]);
      MapOutline.Encoder.encode(qctWriter, mapOutline, 0);
      final MapOutline decoded = MapOutline.Decoder.decode(qctReader, 0);
      assertEquals(0, decoded.points().length);
    }
  }

  @Test
  void roundTrip() {
    final int byteOffset = 512;
    final MapOutline.Point[] originalPoints = { new MapOutline.Point(59.43696, 24.75357),
                                                new MapOutline.Point(60.16985, 24.93838),
                                                new MapOutline.Point(61.05437, 28.18871) };
    final var mapOutline = new MapOutline(originalPoints);
    MapOutline.Encoder.encode(qctWriter, mapOutline, byteOffset);
    final MapOutline decoded = MapOutline.Decoder.decode(qctReader, byteOffset);
    assertEquals(mapOutline, decoded);
  }
}
