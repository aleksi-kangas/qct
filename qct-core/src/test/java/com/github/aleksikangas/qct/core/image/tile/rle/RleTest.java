/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.rle;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.ImageTile.Encoding;
import com.github.aleksikangas.qct.core.utils.DirectQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RleTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("rle-tile", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, 8192);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @Nested
  @DisplayName("RunLengthEncoding")
  class EncodingTests {
    @Test
    void encodeAndDecodeSimpleRun() {
      final var originalImageTile = new ImageTile(Encoding.RUN_LENGTH_ENCODING, createUniformTile(42));
      final int byteOffset = 0;
      RleEncoder.encode(qctWriter, originalImageTile, byteOffset);
      final ImageTile decodedImageTile = RleDecoder.decode(qctReader, byteOffset);
      assertEquals(Encoding.RUN_LENGTH_ENCODING, decodedImageTile.encoding());
      assertArrayEquals(originalImageTile.paletteIndices(), decodedImageTile.paletteIndices());
    }

    @Test
    void encodeAndDecodeMultipleShortRuns() {
      final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
      for (int y = 0; y < ImageTile.HEIGHT; y++) {
        for (int x = 0; x < ImageTile.WIDTH; x++) {
          data[y][x] = (x % 3) * 10;   // pattern: 0,10,20,0,10,20...
        }
      }
      final var originalImageTile = new ImageTile(Encoding.RUN_LENGTH_ENCODING, data);
      final int byteOffset = 100;
      RleEncoder.encode(qctWriter, originalImageTile, byteOffset);
      final ImageTile decodedImageTile = RleDecoder.decode(qctReader, byteOffset);
      assertEquals(originalImageTile, decodedImageTile);
    }

    @Test
    void encodeAndDecodeLongRun() {
      final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
      // Fill first 60 pixels with 5, then switch to 15
      for (int i = 0; i < 60; i++) {
        int y = i / ImageTile.WIDTH;
        int x = i % ImageTile.WIDTH;
        data[y][x] = 5;
      }
      for (int i = 60; i < ImageTile.PIXEL_COUNT; i++) {
        int y = i / ImageTile.WIDTH;
        int x = i % ImageTile.WIDTH;
        data[y][x] = 15;
      }
      final var originalImageTile = new ImageTile(Encoding.RUN_LENGTH_ENCODING, data);
      final int byteOffset = 200;
      RleEncoder.encode(qctWriter, originalImageTile, byteOffset);
      final ImageTile decodedImageTile = RleDecoder.decode(qctReader, byteOffset);
      assertEquals(originalImageTile, decodedImageTile);
    }

    @Test
    void roundTripWithSubPaletteOfSize4() {
      final int[][] data = createTileWithValues(10, 20, 30, 40);
      final var originalImageTile = new ImageTile(Encoding.RUN_LENGTH_ENCODING, data);
      final int byteOffset = 300;
      RleEncoder.encode(qctWriter, originalImageTile, byteOffset);
      final ImageTile decodedImageTile = RleDecoder.decode(qctReader, byteOffset);
      assertEquals(originalImageTile, decodedImageTile);
    }

    @Test
    void roundTripWithManyColors() {
      // 8 unique colors — still fits comfortably in RLE with 3 bits for index
      final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
      for (int y = 0; y < ImageTile.HEIGHT; y++) {
        for (int x = 0; x < ImageTile.WIDTH; x++) {
          data[y][x] = ((y + x) % 8) * 20;
        }
      }
      final var originalImageTile = new ImageTile(Encoding.RUN_LENGTH_ENCODING, data);
      final int byteOffset = 400;
      RleEncoder.encode(qctWriter, originalImageTile, byteOffset);
      final ImageTile decodedImageTile = RleDecoder.decode(qctReader, byteOffset);
      assertEquals(originalImageTile, decodedImageTile);
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {
    @Test
    void singleColorTile() {
      final var originalImageTile = new ImageTile(Encoding.RUN_LENGTH_ENCODING, createUniformTile(255));
      RleEncoder.encode(qctWriter, originalImageTile, 0);
      final ImageTile decodedImageTile = RleDecoder.decode(qctReader, 0);
      assertEquals(originalImageTile, decodedImageTile);
    }

    @Test
    void alternatingSinglePixels() {
      int[][] data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
      for (int y = 0; y < ImageTile.HEIGHT; y++) {
        for (int x = 0; x < ImageTile.WIDTH; x++) {
          data[y][x] = (x % 2 == 0) ? 7 : 13;
        }
      }

      ImageTile original = new ImageTile(Encoding.RUN_LENGTH_ENCODING, data);

      RleEncoder.encode(qctWriter, original, 50);
      ImageTile decoded = RleDecoder.decode(qctReader, 50);

      assertEquals(original, decoded);
    }
  }

  private int[][] createUniformTile(final int paletteIndex) {
    final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < ImageTile.HEIGHT; y++) {
      Arrays.fill(data[y], paletteIndex);
    }
    return data;
  }

  private int[][] createTileWithValues(final int... values) {
    final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    int idx = 0;
    for (int y = 0; y < ImageTile.HEIGHT; y++) {
      for (int x = 0; x < ImageTile.WIDTH; x++) {
        data[y][x] = values[idx++ % values.length];
      }
    }
    return data;
  }
}
