/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.color;

import com.github.aleksikangas.qct.core.utils.DirectQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class InterpolationMatrixTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("interpolation-matrix", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, InterpolationMatrix.SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @Nested
  @DisplayName("InterpolationMatrix")
  class RecordTests {

    @Test
    void constructor() {
      final int[] indices = createIdentityMatrix();
      final var matrix = new InterpolationMatrix(indices);
      assertEquals(InterpolationMatrix.SIZE, matrix.indices().length);
      assertThrows(NullPointerException.class, () -> new InterpolationMatrix(null));
      assertThrows(IllegalStateException.class, () -> new InterpolationMatrix(new int[127]));
      assertThrows(IllegalStateException.class, () -> new InterpolationMatrix(new int[129 * 128]));
    }

    @Test
    void offsetOf() {
      assertEquals(0, InterpolationMatrix.offsetOf(0, 0));
      assertEquals(1, InterpolationMatrix.offsetOf(0, 1));
      assertEquals(128, InterpolationMatrix.offsetOf(1, 0));
      assertEquals(128 * 127 + 127, InterpolationMatrix.offsetOf(127, 127));
    }

    @Test
    void paletteIndexOf() {
      final int[] indices = createTestMatrix();
      final var matrix = new InterpolationMatrix(indices);
      assertEquals(13, matrix.paletteIndexOf(0, 0));
      assertEquals(83, matrix.paletteIndexOf(5, 10));
      assertEquals(6, matrix.paletteIndexOf(127, 127));
    }

    @Test
    void paletteIndexOfOutOfBounds() {
      final var matrix = new InterpolationMatrix(createIdentityMatrix());
      assertThrows(IndexOutOfBoundsException.class, () -> matrix.paletteIndexOf(-1, 0));
      assertThrows(IndexOutOfBoundsException.class, () -> matrix.paletteIndexOf(0, 128));
      assertThrows(IndexOutOfBoundsException.class, () -> matrix.paletteIndexOf(128, 0));
    }

    @Test
    void equalsAndHashCode() {
      final int[] data1 = createTestMatrix();
      final int[] data2 = createTestMatrix();
      final int[] data3 = createTestMatrix();
      data3[100] = 123;

      final var a = new InterpolationMatrix(data1);
      final var b = new InterpolationMatrix(data2);
      final var c = new InterpolationMatrix(data3);

      assertEquals(a, b);
      assertNotEquals(a, c);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringTest() {
      final var matrix = new InterpolationMatrix(createTestMatrix());
      final String str = matrix.toString();
      assertNotNull(str);
      assertTrue(str.startsWith("["));
      assertTrue(str.endsWith("]"));
      assertTrue(str.contains("0"));
      assertTrue(str.contains("127"));
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decode() {
      final int[] expectedIndices = createTestMatrix();
      qctWriter.writeBytes(InterpolationMatrix.BYTE_OFFSET, expectedIndices);

      final InterpolationMatrix matrix = InterpolationMatrix.Decoder.decode(qctReader);

      assertArrayEquals(expectedIndices, matrix.indices());
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {

    @Test
    void encode() {
      final int[] data = createTestMatrix();
      final var matrix = new InterpolationMatrix(data);

      InterpolationMatrix.Encoder.encode(qctWriter, matrix);

      final InterpolationMatrix decoded = InterpolationMatrix.Decoder.decode(qctReader);

      assertArrayEquals(data, decoded.indices());
    }
  }

  @Test
  void roundTrip() {
    final int[] originalData = createTestMatrix();
    final var originalMatrix = new InterpolationMatrix(originalData);

    InterpolationMatrix.Encoder.encode(qctWriter, originalMatrix);
    final InterpolationMatrix decodedMatrix = InterpolationMatrix.Decoder.decode(qctReader);

    assertEquals(originalMatrix, decodedMatrix);
    assertArrayEquals(originalData, decodedMatrix.indices());
  }

  private int[] createIdentityMatrix() {
    final int[] indices = new int[InterpolationMatrix.SIZE];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = i % 128;   // simple identity-like mapping
    }
    return indices;
  }

  private int[] createTestMatrix() {
    final int[] indices = new int[InterpolationMatrix.SIZE];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = (i * 7 + 13) % 128;   // deterministic, varied values 0-127
    }
    return indices;
  }
}
