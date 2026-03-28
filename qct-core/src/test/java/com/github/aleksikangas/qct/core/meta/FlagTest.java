/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FlagTest {
  @Nested
  @DisplayName("Flag Encoder & Decoder")
  class EncoderDecoderTests {
    @Test
    void encodeAndDecodeNoFlags() throws IOException {
      final Set<Flag> flags = EnumSet.noneOf(Flag.class);
      final Path tempFile = Files.createTempFile("flag-none", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {

        Flag.Encoder.encode(flags, fileChannel, 0);
        final Set<Flag> decoded = Flag.Decoder.decode(fileChannel, 0);

        assertTrue(decoded.isEmpty());
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void encodeAndDecodeSingleFlag() throws IOException {
      final Set<Flag> flags = EnumSet.of(Flag.MUST_HAVE_ORIGINAL_FILE);
      final Path tempFile = Files.createTempFile("flag-single", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        Flag.Encoder.encode(flags, fileChannel, 0);
        final Set<Flag> decoded = Flag.Decoder.decode(fileChannel, 0);

        assertEquals(1, decoded.size());
        assertTrue(decoded.contains(Flag.MUST_HAVE_ORIGINAL_FILE));
        assertFalse(decoded.contains(Flag.ALLOW_CALIBRATION));
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void encodeAndDecodeBothFlags() throws IOException {
      final Set<Flag> flags = EnumSet.of(Flag.MUST_HAVE_ORIGINAL_FILE, Flag.ALLOW_CALIBRATION);
      final Path tempFile = Files.createTempFile("flag-both", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        Flag.Encoder.encode(flags, fileChannel, 0);
        final Set<Flag> decoded = Flag.Decoder.decode(fileChannel, 0);

        assertEquals(2, decoded.size());
        assertTrue(decoded.contains(Flag.MUST_HAVE_ORIGINAL_FILE));
        assertTrue(decoded.contains(Flag.ALLOW_CALIBRATION));
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void encodeAndDecodeLargeOffset() throws IOException {
      final Set<Flag> flags = EnumSet.of(Flag.ALLOW_CALIBRATION);
      final Path tempFile = Files.createTempFile("flag-offset", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        final int offset = 1024 * 1024; // 1MB offset
        Flag.Encoder.encode(flags, fileChannel, offset);
        final Set<Flag> decoded = Flag.Decoder.decode(fileChannel, offset);

        assertEquals(EnumSet.of(Flag.ALLOW_CALIBRATION), decoded);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {

    @Test
    void decodeOnlyMustHaveOriginalFile() throws IOException {
      final Path tempFile = Files.createTempFile("flag-decode-must", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 1); // bit 0 set

        final Set<Flag> decoded = Flag.Decoder.decode(fileChannel, 0);

        assertEquals(EnumSet.of(Flag.MUST_HAVE_ORIGINAL_FILE), decoded);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeOnlyAllowCalibration() throws IOException {
      final Path tempFile = Files.createTempFile("flag-decode-allow", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 2); // bit 1 set

        final Set<Flag> decoded = Flag.Decoder.decode(fileChannel, 0);

        assertEquals(EnumSet.of(Flag.ALLOW_CALIBRATION), decoded);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }

    @Test
    void decodeUnknownBitsAreIgnored() throws IOException {
      final Path tempFile = Files.createTempFile("flag-unknown-bits", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        QctWriter.writeInt(fileChannel, 0, 0b1111_1111); // all bits set

        final Set<Flag> decoded = Flag.Decoder.decode(fileChannel, 0);

        assertTrue(decoded.contains(Flag.MUST_HAVE_ORIGINAL_FILE));
        assertTrue(decoded.contains(Flag.ALLOW_CALIBRATION));
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encodeEmptySetWritesZero() throws IOException {
      final Set<Flag> flags = EnumSet.noneOf(Flag.class);
      final Path tempFile = Files.createTempFile("flag-empty-encode", ".bin");
      try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        Flag.Encoder.encode(flags, fileChannel, 0);

        final int rawValue = QctReader.readInt(fileChannel, 0);
        assertEquals(0, rawValue);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Test
  void roundTripAllCombinations() throws IOException {
    final Set<Flag> original = EnumSet.of(Flag.MUST_HAVE_ORIGINAL_FILE, Flag.ALLOW_CALIBRATION);
    final int byteOffset = 0x40;
    final Path tempFile = Files.createTempFile("flag-round-trip", ".bin");
    try (final var fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      Flag.Encoder.encode(original, fileChannel, byteOffset);
      final Set<Flag> decoded = Flag.Decoder.decode(fileChannel, byteOffset);

      assertEquals(original, decoded);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
