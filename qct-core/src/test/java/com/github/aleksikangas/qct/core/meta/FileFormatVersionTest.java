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

import static org.junit.jupiter.api.Assertions.*;

class FileFormatVersionTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("file-format-version", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, FileFormatVersion.SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

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
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decodeQuickChart() {
      qctWriter.writeInt(0, 0x00000002);
      final FileFormatVersion version = FileFormatVersion.Decoder.decode(qctReader, 0);
      assertEquals(FileFormatVersion.QUICK_CHART, version);
    }

    @Test
    void decodeQuickChartWithLicenseManagement() {
      qctWriter.writeInt(0, 0x00000004);
      final FileFormatVersion version = FileFormatVersion.Decoder.decode(qctReader, 0);
      assertEquals(FileFormatVersion.QUICK_CHART_SUPPORTING_LICENSE_MANAGEMENT, version);
    }

    @Test
    void decodeQC3() {
      qctWriter.writeInt(0, 0x20000001);
      final FileFormatVersion version = FileFormatVersion.Decoder.decode(qctReader, 0);
      assertEquals(FileFormatVersion.QC3, version);
    }

    @Test
    void decodeUnknownValueThrowsException() {
      qctWriter.writeInt(0, 0xFFFFFFFF); // unknown value
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                        () -> FileFormatVersion.Decoder.decode(qctReader, 0));
      assertTrue(exception.getMessage().contains("Unknown FileFormatVersion"));
      assertTrue(exception.getMessage().contains("-1"));
    }

    @Test
    void decodeLargeOffset() {
      final int offset = 1024 * 1024; // 1MB offset
      qctWriter.writeInt(offset, 0x20000001);
      final FileFormatVersion version = FileFormatVersion.Decoder.decode(qctReader, offset);
      assertEquals(FileFormatVersion.QC3, version);
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encodeQuickChart() {
      FileFormatVersion.Encoder.encode(qctWriter, FileFormatVersion.QUICK_CHART, 0);
      final FileFormatVersion decoded = FileFormatVersion.Decoder.decode(qctReader, 0);
      assertEquals(FileFormatVersion.QUICK_CHART, decoded);
    }

    @Test
    void encodeAllVariants() {
      for (FileFormatVersion version : FileFormatVersion.values()) {
        final int offset = version.ordinal() * 8;  // spaced out
        FileFormatVersion.Encoder.encode(qctWriter, version, offset);
        final FileFormatVersion decoded = FileFormatVersion.Decoder.decode(qctReader, offset);
        assertEquals(version, decoded);
        assertEquals(version.value(), decoded.value());
      }
    }
  }

  @Test
  void roundTrip() {
    final int byteOffset = 512;
    for (FileFormatVersion original : FileFormatVersion.values()) {
      FileFormatVersion.Encoder.encode(qctWriter, original, byteOffset);
      final FileFormatVersion decoded = FileFormatVersion.Decoder.decode(qctReader, byteOffset);
      assertEquals(original, decoded);
      assertEquals(original.value(), decoded.value());
    }
  }
}
