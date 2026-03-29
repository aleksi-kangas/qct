/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import com.github.aleksikangas.qct.core.utils.QctReader;
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
    try (FileChannel readChannel = FileChannel.open(path, StandardOpenOption.READ)) {
      final var qctReader = new QctReader(readChannel);
      final QctFile originalQctFile = QctFile.Decoder.decode(qctReader);
      final Path tempFile = Files.createTempFile("qct", ".bin");
      try (final var writeChannel = FileChannel.open(tempFile, StandardOpenOption.WRITE)) {
        final var qctWriter = new QctWriter(writeChannel, originalQctFile.headerSize());
        QctFile.Encoder.encode(qctWriter, originalQctFile);
      }
      try (final var readChannel2 = FileChannel.open(tempFile, StandardOpenOption.READ)) {
        final var qctReader2 = new QctReader(readChannel2);
        final QctFile decoded = QctFile.Decoder.decode(qctReader2);
        assertEquals(originalQctFile.metadata(), decoded.metadata());
      }
      Files.deleteIfExists(tempFile);
    }
  }
}