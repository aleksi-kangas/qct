/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.huffman;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.ImageTile.Encoding;
import com.github.aleksikangas.qct.core.utils.DirectQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HuffmanTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("huffman-tile", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, 0);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 42, 127 })
  void singleColor(final int paletteIndex) {
    final var original = new ImageTile(Encoding.HUFFMAN_CODING, createUniformTile(paletteIndex));
    encodeAndAssertRoundTrip(original);
  }

  @Test
  void simpleRepeatingPattern() {
    final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < ImageTile.HEIGHT; y++) {
      for (int x = 0; x < ImageTile.WIDTH; x++) {
        data[y][x] = (x % 3) * 30;   // 0, 30, 60, 0, 30, 60...
      }
    }
    final var original = new ImageTile(Encoding.HUFFMAN_CODING, data);
    encodeAndAssertRoundTrip(original);
  }

  @Test
  void checkerboardPattern() {
    final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < ImageTile.HEIGHT; y++) {
      for (int x = 0; x < ImageTile.WIDTH; x++) {
        data[y][x] = ((y + x) % 2 == 0) ? 17 : 89;
      }
    }
    final var original = new ImageTile(Encoding.HUFFMAN_CODING, data);
    encodeAndAssertRoundTrip(original);
  }

  @Test
  void subPaletteOfSize4() {
    final int[][] data = createTileWithValues(10, 55, 100, 127);
    final var original = new ImageTile(Encoding.HUFFMAN_CODING, data);
    encodeAndAssertRoundTrip(original);
  }

  @Test
  void eightDistinctColors() {
    final var data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < ImageTile.HEIGHT; y++) {
      for (int x = 0; x < ImageTile.WIDTH; x++) {
        data[y][x] = ((y * ImageTile.WIDTH + x) % 8) * 16;
      }
    }
    final var original = new ImageTile(Encoding.HUFFMAN_CODING, data);
    encodeAndAssertRoundTrip(original);
  }

  @Test
  void alternatingSinglePixels() {
    final int[][] data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    for (int y = 0; y < ImageTile.HEIGHT; y++) {
      for (int x = 0; x < ImageTile.WIDTH; x++) {
        data[y][x] = (x % 2 == 0) ? 7 : 42;
      }
    }
    final var original = new ImageTile(Encoding.HUFFMAN_CODING, data);
    encodeAndAssertRoundTrip(original);
  }

  @Test
  void complexTreeWithManyColors() {
    // Forces a deeper tree
    final int[][] data = new int[ImageTile.HEIGHT][ImageTile.WIDTH];
    int value = 0;
    for (int y = 0; y < ImageTile.HEIGHT; y++) {
      for (int x = 0; x < ImageTile.WIDTH; x++) {
        data[y][x] = value % 128;   // all possible palette indices
        value++;
      }
    }
    final var original = new ImageTile(Encoding.HUFFMAN_CODING, data);
    encodeAndAssertRoundTrip(original);
  }

  private void encodeAndAssertRoundTrip(final ImageTile original) {
    HuffmanEncoder.Candidate.of(original).encode(qctWriter, 0);
    final ImageTile decoded = HuffmanDecoder.decode(qctReader, 0);
    assertEquals(Encoding.HUFFMAN_CODING, decoded.encoding());
    assertArrayEquals(original.paletteIndices(), decoded.paletteIndices(), "Decoded tile does not match original");
    assertEquals(original, decoded);
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
