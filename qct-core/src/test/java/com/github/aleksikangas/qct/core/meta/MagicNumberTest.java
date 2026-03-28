/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class MagicNumberTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("magic-number", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new QctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, MagicNumber.SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

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
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decodeQuickChartInformation() {
      qctWriter.writeInt(0, 0x1423D5FE);
      final MagicNumber magic = MagicNumber.Decoder.decode(qctReader, 0);
      assertEquals(MagicNumber.QUICK_CHART_INFORMATION, magic);
    }

    @Test
    void decodeQuickChartMap() {
      qctWriter.writeInt(0, 0x1423D5FF);
      final MagicNumber magic = MagicNumber.Decoder.decode(qctReader, 0);
      assertEquals(MagicNumber.QUICK_CHART_MAP, magic);
    }

    @Test
    void decodeUnknownValueThrowsException() {
      qctWriter.writeInt(0, 0xDEADBEEF); // unknown magic
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                        () -> MagicNumber.Decoder.decode(qctReader, 0));
      assertTrue(exception.getMessage().contains("Unknown MagicNumber"));
      assertTrue(exception.getMessage().contains("-559038737"));
    }

    @Test
    void decodeLargeOffset() {
      final int offset = 1024 * 1024 + 256; // large offset
      qctWriter.writeInt(offset, 0x1423D5FF);
      final MagicNumber magic = MagicNumber.Decoder.decode(qctReader, offset);
      assertEquals(MagicNumber.QUICK_CHART_MAP, magic);
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encodeQuickChartInformation() {
      MagicNumber.Encoder.encode(qctWriter, MagicNumber.QUICK_CHART_INFORMATION, 0);
      final MagicNumber decoded = MagicNumber.Decoder.decode(qctReader, 0);
      assertEquals(MagicNumber.QUICK_CHART_INFORMATION, decoded);
    }

    @Test
    void encodeQuickChartMap() {
      MagicNumber.Encoder.encode(qctWriter, MagicNumber.QUICK_CHART_MAP, 0);
      final MagicNumber decoded = MagicNumber.Decoder.decode(qctReader, 0);
      assertEquals(MagicNumber.QUICK_CHART_MAP, decoded);
    }

    @Test
    void encodeAllVariants() {
      for (MagicNumber magic : MagicNumber.values()) {
        final int offset = magic.ordinal() * 16;
        MagicNumber.Encoder.encode(qctWriter, magic, offset);
        final MagicNumber decoded = MagicNumber.Decoder.decode(qctReader, offset);
        assertEquals(magic, decoded);
        assertEquals(magic.value(), decoded.value());
      }
    }
  }

  @Test
  void roundTrip() {
    final int byteOffset = 1024;
    for (MagicNumber original : MagicNumber.values()) {
      MagicNumber.Encoder.encode(qctWriter, original, byteOffset);
      final MagicNumber decoded = MagicNumber.Decoder.decode(qctReader, byteOffset);
      assertEquals(original, decoded);
      assertEquals(original.value(), decoded.value());
    }
  }
}
