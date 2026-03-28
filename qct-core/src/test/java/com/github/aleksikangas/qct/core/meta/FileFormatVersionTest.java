/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class FileFormatVersionTest {
  @Nested
  @DisplayName("FileFormatVersion")
  class EnumTests {
    @Test
    void constants() {
      assertEquals(0x00000002, FileFormatVersion.QUICK_CHART.value());
      assertEquals(0x00000004, FileFormatVersion.QUICK_CHART_SUPPORTING_LICENSE_MANAGEMENT.value());
      assertEquals(0x20000001, FileFormatVersion.QC3.value());
    }

    @Test
    void toStringHumanReadable() {
      assertEquals("Quick Chart", FileFormatVersion.QUICK_CHART.toString());
      assertEquals("Quick Chart supporting License Management",
                   FileFormatVersion.QUICK_CHART_SUPPORTING_LICENSE_MANAGEMENT.toString());
      assertEquals("QC3 Format", FileFormatVersion.QC3.toString());
    }

    @Test
    void value() {
      assertEquals(0x00000002, FileFormatVersion.QUICK_CHART.value());
      assertEquals(0x00000004, FileFormatVersion.QUICK_CHART_SUPPORTING_LICENSE_MANAGEMENT.value());
      assertEquals(0x20000001, FileFormatVersion.QC3.value());
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decodeQuickChart() throws IOException {
      final Path tempFile = Files.createTempFile("file-format-version-decode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0x00000002);

        final FileFormatVersion version = FileFormatVersion.Decoder.decode(fileChannel, 0);

        assertEquals(FileFormatVersion.QUICK_CHART, version);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeQuickChartWithLicenseManagement() throws IOException {
      final Path tempFile = Files.createTempFile("file-format-version-decode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0x00000004);

        final FileFormatVersion version = FileFormatVersion.Decoder.decode(fileChannel, 0);

        assertEquals(FileFormatVersion.QUICK_CHART_SUPPORTING_LICENSE_MANAGEMENT, version);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeQC3() throws IOException {
      final Path tempFile = Files.createTempFile("file-format-version-decode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0x20000001);

        final FileFormatVersion version = FileFormatVersion.Decoder.decode(fileChannel, 0);

        assertEquals(FileFormatVersion.QC3, version);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeUnknownValueThrowsException() throws IOException {
      final Path tempFile = Files.createTempFile("file-format-version-unknown", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0xFFFFFFFF); // unknown value

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> FileFormatVersion.Decoder.decode(fileChannel, 0));

        assertTrue(exception.getMessage().contains("Unknown FileFormatVersion"));
        assertTrue(exception.getMessage().contains("-1"));
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeLargeOffset() throws IOException {
      final Path tempFile = Files.createTempFile("file-format-version-large-offset", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        final int offset = 1024 * 1024; // 1MB offset
        QctWriter.writeInt(fileChannel, offset, 0x20000001);

        final FileFormatVersion version = FileFormatVersion.Decoder.decode(fileChannel, offset);

        assertEquals(FileFormatVersion.QC3, version);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encodeQuickChart() throws IOException {
      final Path tempFile = Files.createTempFile("file-format-version-encode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        FileFormatVersion.Encoder.encode(FileFormatVersion.QUICK_CHART, fileChannel, 0);

        final FileFormatVersion decoded = FileFormatVersion.Decoder.decode(fileChannel, 0);
        assertEquals(FileFormatVersion.QUICK_CHART, decoded);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void encodeAllVariants() throws IOException {
      final Path tempFile = Files.createTempFile("file-format-version-encode-all", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        for (FileFormatVersion version : FileFormatVersion.values()) {
          final int offset = version.ordinal() * 8;  // spaced out

          FileFormatVersion.Encoder.encode(version, fileChannel, offset);

          final FileFormatVersion decoded = FileFormatVersion.Decoder.decode(fileChannel, offset);
          assertEquals(version, decoded);
          assertEquals(version.value(), decoded.value());
        }
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Test
  void roundTrip() throws IOException {
    final int byteOffset = 512;
    final Path tempFile = Files.createTempFile("file-format-version-round-trip", ".bin");
    try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      for (FileFormatVersion original : FileFormatVersion.values()) {
        FileFormatVersion.Encoder.encode(original, fileChannel, byteOffset);
        final FileFormatVersion decoded = FileFormatVersion.Decoder.decode(fileChannel, byteOffset);

        assertEquals(original, decoded);
        assertEquals(original.value(), decoded.value());
      }
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
