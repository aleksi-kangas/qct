/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import com.github.aleksikangas.qct.core.image.ImageIndex;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QctFileTest {
  static Stream<Path> qctFilesProvider() throws Exception {
    return Files.list(Path.of("src/test/resources")).filter(path -> path.toString().endsWith(".qct"));
  }

  @ParameterizedTest
  @MethodSource("qctFilesProvider")
  void roundTripTest(final Path path) throws Exception {
    try (FileChannel originalReadFileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
      final QctFile originalQctFile = QctFile.Decoder.decode(originalReadFileChannel);
      final Path tempFile = Files.createTempFile("qct", ".bin");
      try (final var tempWriteFileChannel = FileChannel.open(tempFile, StandardOpenOption.WRITE)) {
        final var tempQctWriter = new QctWriter(tempWriteFileChannel, originalQctFile.headerSizeBytes());
        QctFile.Encoder.encode(tempQctWriter, originalQctFile);
      }
      try (final var tempReadFileChannel = FileChannel.open(tempFile, StandardOpenOption.READ)) {
        final QctFile decodedQctFile = QctFile.Decoder.decode(tempReadFileChannel);
        assertEquals(originalQctFile.metadata(), decodedQctFile.metadata());
        assertEquals(originalQctFile.georeferencingCoefficients(), decodedQctFile.georeferencingCoefficients());
        assertEquals(originalQctFile.palette(), decodedQctFile.palette());
        assertEquals(originalQctFile.interpolationMatrix(), decodedQctFile.interpolationMatrix());
        assertImageIndex(originalQctFile.imageIndex(), decodedQctFile.imageIndex());
      }
      Files.deleteIfExists(tempFile);
    }
  }

  private static void assertImageIndex(final ImageIndex expectedImageIndex, final ImageIndex actualImageIndex) {
    assertEquals(expectedImageIndex.heightTiles(), actualImageIndex.heightTiles());
    assertEquals(expectedImageIndex.widthTiles(), actualImageIndex.widthTiles());
    for (int yTile = 0; yTile < expectedImageIndex.heightTiles(); yTile++) {
      for (int xTile = 0; xTile < expectedImageIndex.widthTiles(); xTile++) {
        final int[][] expectedPaletteIndices = expectedImageIndex.imageTile(yTile, xTile).paletteIndices();
        final int[][] actualPaletteIndices = actualImageIndex.imageTile(yTile, xTile).paletteIndices();
        assertArrayEquals(expectedPaletteIndices, actualPaletteIndices);
      }
    }
  }
}