/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.DirectQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FlagTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("flag", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, Flag.SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @Nested
  @DisplayName("Flag Encoder & Decoder")
  class EncoderDecoderTests {
    @Test
    void encodeAndDecodeNoFlags() {
      final Set<Flag> flags = EnumSet.noneOf(Flag.class);
      Flag.Encoder.encode(qctWriter, flags, 0);
      final Set<Flag> decoded = Flag.Decoder.decode(qctReader, 0);
      assertTrue(decoded.isEmpty());
    }

    @Test
    void encodeAndDecodeSingleFlag() {
      final Set<Flag> flags = EnumSet.of(Flag.MUST_HAVE_ORIGINAL_FILE);
      Flag.Encoder.encode(qctWriter, flags, 0);
      final Set<Flag> decoded = Flag.Decoder.decode(qctReader, 0);

      assertEquals(1, decoded.size());
      assertTrue(decoded.contains(Flag.MUST_HAVE_ORIGINAL_FILE));
      assertFalse(decoded.contains(Flag.ALLOW_CALIBRATION));
    }

    @Test
    void encodeAndDecodeBothFlags() {
      final Set<Flag> flags = EnumSet.of(Flag.MUST_HAVE_ORIGINAL_FILE, Flag.ALLOW_CALIBRATION);
      Flag.Encoder.encode(qctWriter, flags, 0);
      final Set<Flag> decoded = Flag.Decoder.decode(qctReader, 0);
      assertEquals(2, decoded.size());
      assertTrue(decoded.contains(Flag.MUST_HAVE_ORIGINAL_FILE));
      assertTrue(decoded.contains(Flag.ALLOW_CALIBRATION));
    }

    @Test
    void encodeAndDecodeLargeOffset() {
      final Set<Flag> flags = EnumSet.of(Flag.ALLOW_CALIBRATION);
      final int offset = 1024 * 1024; // 1MB offset
      Flag.Encoder.encode(qctWriter, flags, offset);
      final Set<Flag> decoded = Flag.Decoder.decode(qctReader, offset);
      assertEquals(EnumSet.of(Flag.ALLOW_CALIBRATION), decoded);
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {

    @Test
    void decodeOnlyMustHaveOriginalFile() {
      qctWriter.writeInt(0, 1); // bit 0 set
      final Set<Flag> decoded = Flag.Decoder.decode(qctReader, 0);
      assertEquals(EnumSet.of(Flag.MUST_HAVE_ORIGINAL_FILE), decoded);
    }

    @Test
    void decodeOnlyAllowCalibration() {
      qctWriter.writeInt(0, 2); // bit 1 set
      final Set<Flag> decoded = Flag.Decoder.decode(qctReader, 0);
      assertEquals(EnumSet.of(Flag.ALLOW_CALIBRATION), decoded);
    }

    @Test
    void decodeUnknownBitsAreIgnored() {
      qctWriter.writeInt(0, 0b1111_1111); // all bits set
      final Set<Flag> decoded = Flag.Decoder.decode(qctReader, 0);
      assertTrue(decoded.contains(Flag.MUST_HAVE_ORIGINAL_FILE));
      assertTrue(decoded.contains(Flag.ALLOW_CALIBRATION));
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encodeEmptySetWritesZero() {
      final Set<Flag> flags = EnumSet.noneOf(Flag.class);
      Flag.Encoder.encode(qctWriter, flags, 0);
      final int rawValue = qctReader.readInt(0);
      assertEquals(0, rawValue);
    }
  }

  @Test
  void roundTripAllCombinations() {
    final Set<Flag> original = EnumSet.of(Flag.MUST_HAVE_ORIGINAL_FILE, Flag.ALLOW_CALIBRATION);
    final int byteOffset = 0x40;
    Flag.Encoder.encode(qctWriter, original, byteOffset);
    final Set<Flag> decoded = Flag.Decoder.decode(qctReader, byteOffset);
    assertEquals(original, decoded);
  }
}
