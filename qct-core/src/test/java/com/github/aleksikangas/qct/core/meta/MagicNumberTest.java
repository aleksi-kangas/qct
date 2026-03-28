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

class MagicNumberTest {
  @Nested
  @DisplayName("MagicNumber")
  class EnumTests {
    @Test
    void constants() {
      assertEquals(0x1423D5FE, MagicNumber.QUICK_CHART_INFORMATION.value());
      assertEquals(0x1423D5FF, MagicNumber.QUICK_CHART_MAP.value());
    }

    @Test
    void toStringHumanReadable() {
      assertEquals("Quick Chart Information", MagicNumber.QUICK_CHART_INFORMATION.toString());
      assertEquals("Quick Chart Map", MagicNumber.QUICK_CHART_MAP.toString());
    }

    @Test
    void value() {
      assertEquals(0x1423D5FE, MagicNumber.QUICK_CHART_INFORMATION.value());
      assertEquals(0x1423D5FF, MagicNumber.QUICK_CHART_MAP.value());
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decodeQuickChartInformation() throws IOException {
      final Path tempFile = Files.createTempFile("magic-number-decode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0x1423D5FE);

        final MagicNumber magic = MagicNumber.Decoder.decode(fileChannel, 0);

        assertEquals(MagicNumber.QUICK_CHART_INFORMATION, magic);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeQuickChartMap() throws IOException {
      final Path tempFile = Files.createTempFile("magic-number-decode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0x1423D5FF);

        final MagicNumber magic = MagicNumber.Decoder.decode(fileChannel, 0);

        assertEquals(MagicNumber.QUICK_CHART_MAP, magic);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeUnknownValueThrowsException() throws IOException {
      final Path tempFile = Files.createTempFile("magic-number-unknown", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0xDEADBEEF); // unknown magic

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> MagicNumber.Decoder.decode(fileChannel, 0));

        assertTrue(exception.getMessage().contains("Unknown MagicNumber"));
        assertTrue(exception.getMessage().contains("-559038737"));
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeLargeOffset() throws IOException {
      final Path tempFile = Files.createTempFile("magic-number-large-offset", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        final int offset = 1024 * 1024 + 256; // large offset
        QctWriter.writeInt(fileChannel, offset, 0x1423D5FF);

        final MagicNumber magic = MagicNumber.Decoder.decode(fileChannel, offset);

        assertEquals(MagicNumber.QUICK_CHART_MAP, magic);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {

    @Test
    void encodeQuickChartInformation() throws IOException {
      final Path tempFile = Files.createTempFile("magic-number-encode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        MagicNumber.Encoder.encode(MagicNumber.QUICK_CHART_INFORMATION, fileChannel, 0);

        final MagicNumber decoded = MagicNumber.Decoder.decode(fileChannel, 0);
        assertEquals(MagicNumber.QUICK_CHART_INFORMATION, decoded);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void encodeQuickChartMap() throws IOException {
      final Path tempFile = Files.createTempFile("magic-number-encode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        MagicNumber.Encoder.encode(MagicNumber.QUICK_CHART_MAP, fileChannel, 0);

        final MagicNumber decoded = MagicNumber.Decoder.decode(fileChannel, 0);
        assertEquals(MagicNumber.QUICK_CHART_MAP, decoded);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void encodeAllVariants() throws IOException {
      final Path tempFile = Files.createTempFile("magic-number-encode-all", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        for (MagicNumber magic : MagicNumber.values()) {
          final int offset = magic.ordinal() * 16;
          MagicNumber.Encoder.encode(magic, fileChannel, offset);

          final MagicNumber decoded = MagicNumber.Decoder.decode(fileChannel, offset);
          assertEquals(magic, decoded);
          assertEquals(magic.value(), decoded.value());
        }
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Test
  void roundTrip() throws IOException {
    final int byteOffset = 1024;
    final Path tempFile = Files.createTempFile("magic-number-round-trip", ".bin");
    try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      for (MagicNumber original : MagicNumber.values()) {
        MagicNumber.Encoder.encode(original, fileChannel, byteOffset);
        final MagicNumber decoded = MagicNumber.Decoder.decode(fileChannel, byteOffset);

        assertEquals(original, decoded);
        assertEquals(original.value(), decoded.value());
      }
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
