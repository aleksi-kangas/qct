/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.convert.png;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;
import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.core.georef.GeoreferencingCoefficients;
import org.geotools.data.WorldFileWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PngConverter {
  private static final Logger LOG = LoggerFactory.getLogger(PngConverter.class);

  public record Options(GeoreferencingMode georeferencingMode) {
    public enum GeoreferencingMode {
      /**
       * Georeferencing disabled, projection ({@code .prj}) and world ({@code .pgw}) files shall not be created.
       */
      DISABLED,
      /**
       * Affine transformation shall be used. Affine transformation may not be accurate when the second and third order
       * {@link GeoreferencingCoefficients} are non-zero.
       */
      AFFINE,
    }
  }

  @SuppressWarnings("java:S1301")
  public static void convertPng(final QctFile qctFile, final Path exportPath, final Options options) {
    try {
      exportPng(qctFile, exportPath);
      switch (options.georeferencingMode) {
        case AFFINE -> {
          final ReferencedEnvelope referencedEnvelope = referencedEnvelope(qctFile);
          exportProjectionFile(referencedEnvelope, exportPath);
          exportWorldFile(qctFile, exportPath);
        }
        case DISABLED -> LOG.debug("Georeferencing mode disabled");
      }
    } catch (final IOException e) {
      LOG.error("Failed to convert to a PNG file.", e);
    }
  }

  private static void exportPng(final QctFile qctFile, final Path exportPath) {
    final var imageInfo = new ImageInfo(qctFile.widthPixels(), qctFile.heightPixels(), 8, false);
    final var pngWriter = new PngWriter(exportPath.toFile(), imageInfo);
    final var imageLineInt = new ImageLineInt(imageInfo);
    final int[][] rgbPixels2D = qctFile.rgbPixels2D();
    for (int y = 0; y < qctFile.heightPixels(); ++y) {
      ImageLineHelper.setPixelsRGB8(imageLineInt, rgbPixels2D[y]);
      pngWriter.writeRow(imageLineInt, y);
    }
    pngWriter.end();
  }

  private static ReferencedEnvelope referencedEnvelope(final QctFile qctFile) {
    final var topLeftWgs84Coordinates = GeoreferencingCoefficients.topLeftWgs84(qctFile);
    final var bottomRightWgs84Coordinates = GeoreferencingCoefficients.bottomRightWgs84(qctFile);
    return new ReferencedEnvelope(topLeftWgs84Coordinates.longitude(),
                                  bottomRightWgs84Coordinates.longitude(),
                                  topLeftWgs84Coordinates.latitude(),
                                  bottomRightWgs84Coordinates.latitude(),
                                  DefaultGeographicCRS.WGS84);
  }

  private static Path associatedFileExportPath(final Path exportPath, final String extension) {
    final String fileName = exportPath.getFileName().toString();
    final String fileNameWithoutExtension = fileName.replaceFirst("[.][^.]+$", "");
    return exportPath.getParent().resolve(fileNameWithoutExtension + extension);
  }

  private static void exportProjectionFile(final ReferencedEnvelope referencedEnvelope, final Path exportPath)
          throws IOException {
    try (final var outputStream = Files.newOutputStream(associatedFileExportPath(exportPath, ".prj"))) {
      try (final var printWriter = new PrintWriter(outputStream)) {
        printWriter.println(referencedEnvelope.getCoordinateReferenceSystem().toWKT());
      }
    }
  }

  private static void exportWorldFile(final QctFile qctFile, final Path exportPath) throws IOException {
    final AffineTransform affineTransformation = GeoreferencingCoefficients.toApproximateAffineTransform(qctFile);
    try (final var outputStream = Files.newOutputStream(associatedFileExportPath(exportPath, ".pgw"))) {
      new WorldFileWriter(outputStream, affineTransformation);
    }
  }

  private PngConverter() {
  }
}
