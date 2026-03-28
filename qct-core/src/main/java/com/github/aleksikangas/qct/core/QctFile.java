/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import com.github.aleksikangas.qct.core.exception.QctRuntimeException;
import com.github.aleksikangas.qct.core.meta.Metadata;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Set;

/**
 * <pre>
 * +--------+---------------+---------------------------------------------------+
 * | Offset | Size (Bytes) | Content                                            |
 * +--------+--------------+----------------------------------------------------+
 * | 0x0000 | 24 x 4       | Metadata - 24 Integers/Pointers                    |
 * | 0x0060 | 40 x 8       | Geographical Referencing Coefficients - 40 Doubles |
 * | 0x01A0 | 256 x 4      | Palette - 128 of 256 Colors                        |
 * | 0x05A0 | 128 x 128    | Interpolation Matrix                               |
 * | 0x45A0 | w x h x 4    | Image Index Pointers - QC3 Files Omit This         |
 * | -      | -            | File Body - Text Strings and Compressed Image Data |
 * +--------+--------------+----------------------------------------------------+
 * </pre>
 */
public record QctFile(Metadata metadata) {
  public QctFile {
    Objects.requireNonNull(metadata);
  }

  public static final class Decoder {
    public static QctFile decode(final FileChannel fileChannel) {
      return new QctFile(Metadata.Decoder.decode(fileChannel));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctFile qctFile, final FileChannel fileChannel) {
      Objects.requireNonNull(qctFile);
      throw new UnsupportedOperationException("Not implemented");
    }

    private Encoder() {
    }
  }

  static void main(final String[] args) {
    final Path path = Paths.get(args[0]);
    try (final var fileChannel = FileChannel.open(path, Set.of(StandardOpenOption.READ))) {
      final QctFile qctFile = QctFile.Decoder.decode(fileChannel);
      System.out.println(qctFile);
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }
}
