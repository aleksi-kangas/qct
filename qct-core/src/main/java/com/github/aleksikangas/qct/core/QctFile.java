/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core;

import com.github.aleksikangas.qct.core.color.InterpolationMatrix;
import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.exception.QctRuntimeException;
import com.github.aleksikangas.qct.core.georef.GeoreferencingCoefficients;
import com.github.aleksikangas.qct.core.image.ImageIndex;
import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.utils.BufferedQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
                      InterpolationMatrix interpolationMatrix,
                      ImageIndex imageIndex) {
  private static final Logger LOG = LoggerFactory.getLogger(QctFile.class);

  public QctFile {
    Objects.requireNonNull(metadata);
    Objects.requireNonNull(georeferencingCoefficients);
  }

  @Nonnull
  @Override
  public String toString() {
    return "{" +
           "\n" +
           "Metadata: \n" +
           "\t" +
           metadata +
           "\n" +
           "GeoreferencingCoefficients: \n" +
           georeferencingCoefficients +
           "\n" +
           "Palette: \n" +
           "\t" +
           palette +
           "\n" +
           "InterpolationMatrix: \n" +
           "\t" +
           interpolationMatrix +
           "\n" +
           "}";
  }

  public int headerSize() {
    return 0x45A0 + imageIndex.size();
  }

  public static final class Decoder {
    public static QctFile decode(final QctReader qctReader) {
      final Metadata metadata = Metadata.Decoder.decode(qctReader);
      return new QctFile(metadata,
                         GeoreferencingCoefficients.Decoder.decode(qctReader),
                         Palette.Decoder.decode(qctReader),
                         InterpolationMatrix.Decoder.decode(qctReader),
                         ImageIndex.Decoder.decode(qctReader, metadata));
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final QctFile qctFile) {
      Objects.requireNonNull(qctFile);

      Metadata.Encoder.encode(qctWriter, qctFile.metadata);
      GeoreferencingCoefficients.Encoder.encode(qctWriter, qctFile.georeferencingCoefficients);
      Palette.Encoder.encode(qctWriter, qctFile.palette);
      InterpolationMatrix.Encoder.encode(qctWriter, qctFile.interpolationMatrix);
      ImageIndex.Encoder.encode(qctWriter, qctFile.imageIndex, qctFile.metadata);
    }

    private Encoder() {
    }
  }

  static void main(final String[] args) {
    final Path path = Paths.get(args[0]);
    try (final var fileChannel = FileChannel.open(path, Set.of(StandardOpenOption.READ))) {
      final var qctReader = new BufferedQctReader(fileChannel);
      final QctFile qctFile = QctFile.Decoder.decode(qctReader);
      if (LOG.isInfoEnabled()) {
        LOG.info(qctFile.toString());
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }
}
