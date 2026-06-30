/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import com.github.aleksikangas.qct.core.decoder.QctFileDecoder;
import com.github.aleksikangas.qct.core.encoder.QctFileEncoder;
import com.github.aleksikangas.qct.core.exception.QctRuntimeException;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@SuppressWarnings("java:S106")
public final class QctFileMain {
  static void main(final String[] args) {
    if (args.length == 0) {
      System.err.println("Usage: java ... QctFile <input.qct> [output.qct]");
      System.err.println("       If no output is specified, '-test.qct' will be appended to the input filename.");
      System.exit(1);
    }
    final Path inputPath = Paths.get(args[0]);

    final Path outputPath;
    if (args.length >= 2) {
      outputPath = Paths.get(args[1]);
    } else {
      final String inputFileName = inputPath.getFileName().toString();
      final String baseName = inputFileName.replaceFirst("[.][^.]+$", ""); // remove extension
      final String extension = inputFileName.substring(inputFileName.lastIndexOf('.'));
      final String outputFileName = baseName + "-test" + extension;

      outputPath = inputPath.getParent() != null
                   ? inputPath.getParent().resolve(outputFileName)
                   : Paths.get(outputFileName);
    }

    decodeEncode(inputPath, outputPath);
  }

  private static void decodeEncode(final Path readPath, final Path writePath) {
    try (final var readFileChannel = FileChannel.open(readPath, Set.of(StandardOpenOption.READ))) {
      final QctFile qctFile = QctFileDecoder.decode(readFileChannel);
      System.out.println(qctFile);
      if (Files.notExists(writePath)) {
        Files.createFile(writePath);
      }
      try (final var writeFileChannel = FileChannel.open(writePath, Set.of(StandardOpenOption.WRITE))) {
        final var qctWriter = new QctWriter(writeFileChannel, qctFile.headerSizeBytes());
        QctFileEncoder.encode(qctWriter, qctFile);
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }

  private QctFileMain() {
  }
}
