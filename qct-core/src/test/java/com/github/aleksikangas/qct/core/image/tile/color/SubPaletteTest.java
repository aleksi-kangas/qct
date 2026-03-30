/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.color;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.utils.DirectQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.github.aleksikangas.qct.core.image.tile.ImageTile.Encoding;
import static org.junit.jupiter.api.Assertions.*;

class SubPaletteTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("sub-palette", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, 2048);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @Nested
  @DisplayName("SubPalette Record")
  class RecordTests {
    @Test
    void constructor() {
      final int[] indices = { 5, 10, 42 };
      final var subPalette = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 3, indices);

      assertEquals(Encoding.RUN_LENGTH_ENCODING, subPalette.encoding());
      assertEquals(3, subPalette.size());
      assertArrayEquals(indices, subPalette.paletteIndices());
      assertThrows(IllegalStateException.class, () -> new SubPalette(Encoding.RUN_LENGTH_ENCODING, 5, indices));
    }

    @Test
    void equalsAndHashCode() {
      final int[] indices1 = { 0, 5, 10 };
      final int[] indices2 = { 0, 5, 10 };
      final int[] indices3 = { 0, 5, 11 };

      final var a = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 3, indices1);
      final var b = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 3, indices2);
      final var c = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 3, indices3);
      final var d = new SubPalette(Encoding.PIXEL_PACKING, 3, indices1);

      assertEquals(a, b);
      assertNotEquals(a, c);
      assertNotEquals(a, d);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringContainsEssentialInfo() {
      final var subPalette = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 2, new int[]{ 7, 15 });
      final String str = subPalette.toString();
      assertTrue(str.contains("size=2"));
      assertTrue(str.contains("paletteIndices=[7, 15]"));
    }
  }

  @Nested
  @DisplayName("Helper Methods")
  class HelperMethodTests {
    @Test
    void sizeByteForRunLengthEncoding() {
      final var subPalette = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 42, new int[42]);
      assertEquals(42, subPalette.sizeByte());
    }

    @Test
    void sizeByteForPixelPacking() {
      final var subPalette = new SubPalette(Encoding.PIXEL_PACKING, 100, new int[100]);
      assertEquals(256 - 100, subPalette.sizeByte());
    }

    @Test
    void bitsRequiredToIndex() {
      assertEquals(0, new SubPalette(Encoding.RUN_LENGTH_ENCODING, 1, new int[]{ 0 }).bitsRequiredToIndex());
      assertEquals(0, new SubPalette(Encoding.RUN_LENGTH_ENCODING, 0, new int[0]).bitsRequiredToIndex());
      assertEquals(1, new SubPalette(Encoding.RUN_LENGTH_ENCODING, 2, new int[]{ 0, 1 }).bitsRequiredToIndex());
      assertEquals(3, new SubPalette(Encoding.RUN_LENGTH_ENCODING, 5, new int[5]).bitsRequiredToIndex());
      assertEquals(8, new SubPalette(Encoding.RUN_LENGTH_ENCODING, 200, new int[200]).bitsRequiredToIndex());
    }

    @Test
    void paletteIndexOf() {
      final int[] indices = { 10, 20, 30 };
      final var subPalette = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 3, indices);
      assertEquals(10, subPalette.paletteIndexOf(0));
      assertEquals(20, subPalette.paletteIndexOf(1));
      assertEquals(30, subPalette.paletteIndexOf(2));
    }

    @Test
    void subPaletteIndexOf() {
      final int[] indices = { 10, 20, 30 };
      final var subPalette = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 3, indices);
      assertEquals(0, subPalette.subPaletteIndexOf(10));
      assertEquals(1, subPalette.subPaletteIndexOf(20));
      assertEquals(2, subPalette.subPaletteIndexOf(30));
    }

    @Test
    void subPaletteIndexOfNotFound() {
      final var subPalette = new SubPalette(Encoding.RUN_LENGTH_ENCODING, 3, new int[]{ 10, 20, 30 });
      assertThrows(IllegalArgumentException.class, () -> subPalette.subPaletteIndexOf(999));
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decodeRunLengthEncoding() {
      final int[] expectedIndices = { 5, 10, 42 };
      qctWriter.writeByte(0, 3);
      qctWriter.writeBytes(1, expectedIndices);

      final SubPalette subPalette = SubPalette.Decoder.decode(qctReader, Encoding.RUN_LENGTH_ENCODING, 0);

      assertEquals(Encoding.RUN_LENGTH_ENCODING, subPalette.encoding());
      assertEquals(3, subPalette.size());
      assertArrayEquals(expectedIndices, subPalette.paletteIndices());
    }

    @Test
    void decodePixelPacking() {
      final int[] expectedIndices = { 0, 1, 2, 3 };
      qctWriter.writeByte(0, 256 - 4);  // sizeByte = 252 → size = 4
      qctWriter.writeBytes(1, expectedIndices);

      final SubPalette subPalette = SubPalette.Decoder.decode(qctReader, Encoding.PIXEL_PACKING, 0);

      assertEquals(Encoding.PIXEL_PACKING, subPalette.encoding());
      assertEquals(4, subPalette.size());
      assertArrayEquals(expectedIndices, subPalette.paletteIndices());
    }

    @Test
    void decodeWithOffset() {
      final int[] indices = { 7, 15 };
      final int offset = 100;
      qctWriter.writeByte(offset, 2);
      qctWriter.writeBytes(offset + 1, indices);

      final SubPalette subPalette = SubPalette.Decoder.decode(qctReader, Encoding.RUN_LENGTH_ENCODING, offset);

      assertEquals(2, subPalette.size());
      assertArrayEquals(indices, subPalette.paletteIndices());
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encodeRunLengthEncoding() {
      final ImageTile tile = createTestImageTile(Encoding.RUN_LENGTH_ENCODING,
                                                 new int[][]{ { 0, 5, 0 }, { 5, 10, 5 } });

      SubPalette.Encoder.encode(qctWriter, tile, 200);
      final SubPalette decodedSubPalette = SubPalette.Decoder.decode(qctReader, Encoding.RUN_LENGTH_ENCODING, 200);

      assertEquals(3, decodedSubPalette.size()); // 0,5,10
      assertArrayEquals(new int[]{ 0, 5, 10 }, decodedSubPalette.paletteIndices());
    }
  }

  @Nested
  @DisplayName("Round-trip")
  class RoundTripTests {
    @Test
    void roundTripRunLengthEncoding() {
      final ImageTile originalTile = createTestImageTile(Encoding.RUN_LENGTH_ENCODING,
                                                         createUniformTileData(0, 1, 2, 3));

      final int byteOffset = 150;
      final SubPalette writtenSubPalette = SubPalette.Encoder.encode(qctWriter, originalTile, byteOffset);
      final SubPalette readSubPalette = SubPalette.Decoder.decode(qctReader, Encoding.RUN_LENGTH_ENCODING, byteOffset);

      assertEquals(writtenSubPalette, readSubPalette);
      assertArrayEquals(writtenSubPalette.paletteIndices(), readSubPalette.paletteIndices());
    }
  }

  private ImageTile createTestImageTile(final Encoding encoding, final int[][] paletteIndices) {
    final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < Math.min(ImageTile.HEIGHT, paletteIndices.length); ++y) {
      System.arraycopy(paletteIndices[y], 0, data[y], 0, Math.min(ImageTile.WIDTH, paletteIndices[y].length));
    }
    return new ImageTile(encoding, data);
  }

  private int[][] createUniformTileData(final int... values) {
    final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    int idx = 0;
    for (int y = 0; y < ImageTile.HEIGHT; y++) {
      for (int x = 0; x < ImageTile.WIDTH; x++) {
        data[y][x] = values[idx % values.length];
        idx++;
      }
    }
    return data;
  }
}
