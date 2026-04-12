/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.georef;


import com.github.aleksikangas.qct.core.meta.DatumShift;
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

class GeoreferencingCoefficientsTest {

  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  private static final double EPS = 1e-6;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("georef", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, GeoreferencingCoefficients.HEADER_SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  private GeoreferencingCoefficients createSimpleCoefficients() {
    return new GeoreferencingCoefficients(
            // eas
            0, 0, 1, 0, 0, 0, 0, 0, 0, 0,

            // nor
            0, 1, 0, 0, 0, 0, 0, 0, 0, 0,

            // lat
            0, 1, 1, 0, 0, 0, 0, 0, 0, 0,

            // lon
            0, 1, -1, 0, 0, 0, 0, 0, 0, 0);
  }

  @Nested
  @DisplayName("Record")
  class RecordTests {
    @Test
    void constructorAndAccessors() {
      final GeoreferencingCoefficients coefficients = createSimpleCoefficients();
      assertEquals(0, coefficients.eas());
      assertEquals(1, coefficients.easX());
      assertEquals(1, coefficients.latX());
      assertEquals(-1, coefficients.lonY());
    }

    @Test
    void equalsAndHashCode() {
      final GeoreferencingCoefficients a = createSimpleCoefficients();
      final GeoreferencingCoefficients b = createSimpleCoefficients();
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }
  }

  @Nested
  @DisplayName("Transformations")
  class TransformationTests {
    @Test
    void toWgs84() {
      final GeoreferencingCoefficients coefficients = createSimpleCoefficients();
      final var datumShift = new DatumShift(0, 0);
      final var imageCoordinates = new ImageCoordinates(2, 3);

      final var wgs84Coordinates = coefficients.toWgs84(imageCoordinates, datumShift);

      assertEquals(5.0, wgs84Coordinates.latitude(), EPS);   // x + y
      assertEquals(1.0, wgs84Coordinates.longitude(), EPS);  // x - y
    }

    @Test
    void toImage() {
      final GeoreferencingCoefficients coefficients = createSimpleCoefficients();
      final var datumShift = new DatumShift(0, 0);
      final var wgs84Coordinates = new Wgs84Coordinates(5, 1);

      final var imageCoordinates = coefficients.toImage(wgs84Coordinates, datumShift);

      assertEquals(5.0, imageCoordinates.y(), EPS);
      assertEquals(1.0, imageCoordinates.x(), EPS);
    }

    @Test
    void roundTrip() {
      final GeoreferencingCoefficients coefficients = new GeoreferencingCoefficients(
              // eas (x = 0.5·φ + 0.5·λ)
              0, 0.5, 0.5, 0, 0, 0, 0, 0, 0, 0,
              // nor (y = 0.5·φ - 0.5·λ)
              0, 0.5, -0.5, 0, 0, 0, 0, 0, 0, 0,
              // lat / lon (unchanged – forward)
              0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0);

      final var datumShift = new DatumShift(0, 0);
      final var imageCoordinates = new ImageCoordinates(4.5, 2.5);

      final var wgs84Coordinates = coefficients.toWgs84(imageCoordinates, datumShift);
      final var resultImageCoordinates = coefficients.toImage(wgs84Coordinates, datumShift);

      assertEquals(imageCoordinates.y(), resultImageCoordinates.y(), EPS);
      assertEquals(imageCoordinates.x(), resultImageCoordinates.x(), EPS);
    }

    @Test
    void appliesDatumShift() {
      final GeoreferencingCoefficients coefficients = createSimpleCoefficients();
      final var datumShift = new DatumShift(10, 20);
      final var imageCoordinates = new ImageCoordinates(2, 3);

      final var wgs84Coordinates = coefficients.toWgs84(imageCoordinates, datumShift);

      assertEquals(15.0, wgs84Coordinates.latitude(), EPS);
      assertEquals(21.0, wgs84Coordinates.longitude(), EPS);
    }
  }

  @Nested
  @DisplayName("Encoder / Decoder")
  class CodecTests {
    @Test
    void encodeAndDecode() {
      final GeoreferencingCoefficients coefficients = createSimpleCoefficients();

      GeoreferencingCoefficients.Encoder.encode(qctWriter, coefficients);
      final GeoreferencingCoefficients decodedCoefficients = GeoreferencingCoefficients.Decoder.decode(qctReader);

      assertEquals(coefficients, decodedCoefficients);
    }

    @Test
    void encodeWritesCorrectOffsets() {
      final GeoreferencingCoefficients coefficients = createSimpleCoefficients();

      GeoreferencingCoefficients.Encoder.encode(qctWriter, coefficients);

      final QctReader qctReader = new DirectQctReader(fileChannel);
      final double[] eas = qctReader.readDoubles(GeoreferencingCoefficients.BYTE_OFFSET, 10);
      final double[] nor = qctReader.readDoubles(GeoreferencingCoefficients.BYTE_OFFSET + 0x50, 10);
      final double[] lat = qctReader.readDoubles(GeoreferencingCoefficients.BYTE_OFFSET + 0xA0, 10);
      final double[] lon = qctReader.readDoubles(GeoreferencingCoefficients.BYTE_OFFSET + 0xF0, 10);

      assertEquals(coefficients.eas(), eas[0]);
      assertEquals(coefficients.nor(), nor[0]);
      assertEquals(coefficients.lat(), lat[0]);
      assertEquals(coefficients.lon(), lon[0]);
    }

    @Test
    void decodeWithLargeOffsetData() {
      final int byteOffset = GeoreferencingCoefficients.BYTE_OFFSET;

      for (int i = 0; i < 10; i++) {
        qctWriter.writeDouble(byteOffset + i * 8, i);
        qctWriter.writeDouble(byteOffset + 0x50 + i * 8, i + 10);
        qctWriter.writeDouble(byteOffset + 0xA0 + i * 8, i + 20);
        qctWriter.writeDouble(byteOffset + 0xF0 + i * 8, i + 30);
      }

      final GeoreferencingCoefficients coefficients = GeoreferencingCoefficients.Decoder.decode(qctReader);

      assertEquals(0.0, coefficients.eas());
      assertEquals(10.0, coefficients.nor());
      assertEquals(20.0, coefficients.lat());
      assertEquals(30.0, coefficients.lon());
    }
  }
}
