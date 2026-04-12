/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@State(Scope.Benchmark)
public final class QctFileBenchmark {
  private static final String QCT_FILE_NAME = "/wr-41m2-droitwich-canals.qct";

  private Path qctFilePath;

  @Setup(Level.Trial)
  public void setup() throws Exception {
    URL resource = getClass().getResource(QCT_FILE_NAME);
    if (resource == null) {
      throw new RuntimeException("Test resource not found: " + QCT_FILE_NAME);
    }
    qctFilePath = Paths.get(resource.toURI());
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Fork(value = 10, warmups = 3)
  public void decode() throws IOException {
    try (final FileChannel fileChannel = FileChannel.open(qctFilePath, StandardOpenOption.READ)) {
      QctFile.Decoder.decode(fileChannel);
    }
  }

  static void main(String[] args) throws Exception {
    Main.main(args);
  }
}
