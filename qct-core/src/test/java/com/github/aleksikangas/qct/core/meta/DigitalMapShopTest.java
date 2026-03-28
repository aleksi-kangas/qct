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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class DigitalMapShopTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("digital-map-shop", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new QctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, DigitalMapShop.SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @Nested
  @DisplayName("DigitalMapShop")
  class RecordTests {
    @Test
    void constructor() {
      final var shop = new DigitalMapShop(8, "maps/example.qc3");
      assertEquals(8, shop.size());
      assertEquals("maps/example.qc3", shop.qc3Url());
    }

    @Test
    void immutable() {
      final var shop = new DigitalMapShop(8, "immutable.qc3");
      assertEquals(8, shop.size());
      assertEquals("immutable.qc3", shop.qc3Url());
    }

    @Test
    void equalsAndHashCode() {
      final var a = new DigitalMapShop(8, "a.qc3");
      final var b = new DigitalMapShop(8, "a.qc3");
      final var c = new DigitalMapShop(8, "b.qc3");

      assertEquals(a, b);
      assertNotEquals(a, c);
      assertEquals(a.hashCode(), b.hashCode());
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decode() {
      final int size = 8;
      final String url = "test/path.qc3";

      qctWriter.writeInt(0, size);
      qctWriter.allocateWriteString(4, url);

      final DigitalMapShop decoded = DigitalMapShop.Decoder.decode(qctReader, 0);

      assertEquals(size, decoded.size());
      assertEquals(url, decoded.qc3Url());
    }

    @Test
    void decodeLargeOffset() {
      final int offset = 1024 * 1024; // 1MB
      final int size = 8;
      final String url = "large/offset.qc3";

      qctWriter.writeInt(offset, size);
      qctWriter.allocateWriteString(offset + 4, url);

      final DigitalMapShop decoded = DigitalMapShop.Decoder.decode(qctReader, offset);

      assertEquals(size, decoded.size());
      assertEquals(url, decoded.qc3Url());
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encode() {
      final var shop = new DigitalMapShop(8, "encode/test.qc3");

      DigitalMapShop.Encoder.encode(qctWriter, shop, 0);

      final DigitalMapShop decoded = DigitalMapShop.Decoder.decode(qctReader, 0);

      assertEquals(shop.size(), decoded.size());
      assertEquals(shop.qc3Url(), decoded.qc3Url());
    }
  }

  @Test
  void roundTrip() {
    final int offset = 128;
    final var shop = new DigitalMapShop(8, "roundtrip/test.qc3");

    DigitalMapShop.Encoder.encode(qctWriter, shop, offset);
    final DigitalMapShop decoded = DigitalMapShop.Decoder.decode(qctReader, offset);

    assertEquals(shop, decoded);
  }
}
