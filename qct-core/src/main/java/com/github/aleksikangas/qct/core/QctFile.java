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
import com.github.aleksikangas.qct.core.utils.MappedQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

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

  public int headerSizeBytes() {
    return 0x45A0 + imageIndex.size();
  }

  public int heightPixels() {
    return imageIndex.heightPixels();
  }

  public int widthPixels() {
    return imageIndex.widthPixels();
  }

  public int[] rgbPixels() {
    final int[] rgbPixels = new int[imageIndex.pixelCount()];
    IntStream.range(0, imageIndex.pixelCount()).parallel().forEach(pixelIndex -> {
      final int y = pixelIndex / imageIndex.widthPixels();
      final int x = pixelIndex % imageIndex.widthPixels();
      final Color pixelColor = imageIndex.pixelColor(palette, y, x);
      rgbPixels[pixelIndex] = pixelColor.getRGB();
    });
    return rgbPixels;
  }

  public int[][] rgbPixels2D() {
    final int[][] pixels = new int[heightPixels()][widthPixels()];
    IntStream.range(0, heightPixels()).parallel().forEach(y -> {
      for (int x = 0; x < widthPixels(); x++) {
        final Color pixelColor = imageIndex.pixelColor(palette, y, x);
        pixels[y][x] = pixelColor.getRGB();
      }
    });
    return pixels;
  }

  public static final class Decoder {
    public static QctFile decode(final FileChannel fileChannel) {
      final QctReader qctReader = new MappedQctReader(fileChannel);
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

  @SuppressWarnings("java:S106")
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
      final QctFile qctFile = QctFile.Decoder.decode(readFileChannel);
      if (LOG.isInfoEnabled()) {
        LOG.info(qctFile.toString());
      }
      if (Files.notExists(writePath)) {
        Files.createFile(writePath);
      }
      try (final var writeFileChannel = FileChannel.open(writePath, Set.of(StandardOpenOption.WRITE))) {
        final var qctWriter = new QctWriter(writeFileChannel, qctFile.headerSizeBytes());
        QctFile.Encoder.encode(qctWriter, qctFile);
      }
    } catch (final IOException e) {
      throw new QctRuntimeException(e);
    }
  }
}
