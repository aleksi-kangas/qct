/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import com.github.aleksikangas.qct.core.utils.BufferedQctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QctFileTest {
  static Stream<Path> qctFilesProvider() throws Exception {
    return Files.list(Path.of("src/test/resources")).filter(path -> path.toString().endsWith(".qct"));
  }

  @ParameterizedTest
  @MethodSource("qctFilesProvider")
  void roundTripTest(final Path path) throws Exception {
    try (FileChannel originalReadFileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
      final var originalQctReader = new BufferedQctReader(originalReadFileChannel);
      final QctFile originalQctFile = QctFile.Decoder.decode(originalQctReader);
      final Path tempFile = Files.createTempFile("qct", ".bin");
      try (final var tempWriteFileChannel = FileChannel.open(tempFile, StandardOpenOption.WRITE)) {
        final var tempQctWriter = new QctWriter(tempWriteFileChannel, originalQctFile.headerSizeBytes());
        QctFile.Encoder.encode(tempQctWriter, originalQctFile);
      }
      try (final var tempReadFileChannel = FileChannel.open(tempFile, StandardOpenOption.READ)) {
        final var tempQctReader = new BufferedQctReader(tempReadFileChannel);
        final QctFile decodedQctFile = QctFile.Decoder.decode(tempQctReader);
        assertEquals(originalQctFile.metadata(), decodedQctFile.metadata());
        assertEquals(originalQctFile.georeferencingCoefficients(), decodedQctFile.georeferencingCoefficients());
        assertEquals(originalQctFile.palette(), decodedQctFile.palette());
        assertEquals(originalQctFile.interpolationMatrix(), decodedQctFile.interpolationMatrix());
      }
      Files.deleteIfExists(tempFile);
    }
  }
}