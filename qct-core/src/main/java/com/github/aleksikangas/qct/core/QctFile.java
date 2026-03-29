/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import com.github.aleksikangas.qct.core.color.InterpolationMatrix;
import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.exception.QctRuntimeException;
import com.github.aleksikangas.qct.core.georef.GeoreferencingCoefficients;
import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

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
public record QctFile(Metadata metadata,
                      GeoreferencingCoefficients georeferencingCoefficients,
                      Palette palette,
                      InterpolationMatrix interpolationMatrix) {
  public QctFile {
    Objects.requireNonNull(metadata);
    Objects.requireNonNull(georeferencingCoefficients);
  }

  public int headerSize() {
    // TODO
    return 0x45A0;
  }

  public static final class Decoder {
    public static QctFile decode(final QctReader qctReader) {
      return new QctFile(Metadata.Decoder.decode(qctReader),
                         GeoreferencingCoefficients.Decoder.decode(qctReader),
                         Palette.Decoder.decode(qctReader),
                         InterpolationMatrix.Decoder.decode(qctReader));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final QctFile qctFile) {
      Objects.requireNonNull(qctFile);

      Metadata.Encoder.encode(qctWriter, qctFile.metadata());
      GeoreferencingCoefficients.Encoder.encode(qctWriter, qctFile.georeferencingCoefficients());
      Palette.Encoder.encode(qctWriter, qctFile.palette());
      InterpolationMatrix.Encoder.encode(qctWriter, qctFile.interpolationMatrix());
    }

    private Encoder() {
    }
  }

  static void main(final String[] args) {
    final Path path = Paths.get(args[0]);
    try (final var fileChannel = FileChannel.open(path, Set.of(StandardOpenOption.READ))) {
      final var qctReader = new QctReader(fileChannel);
      final QctFile qctFile = QctFile.Decoder.decode(qctReader);
      System.out.println(qctFile);
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }
}
