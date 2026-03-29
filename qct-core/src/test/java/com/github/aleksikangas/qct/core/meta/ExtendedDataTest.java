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

class ExtendedDataTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("extended-data", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new QctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, ExtendedData.HEADER_SIZE);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  private int[] serialBytes() {
    final int[] bytes = new int[SerialNumber.SIZE];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = i;
    }
    return bytes;
  }

  private ExtendedData createSample() {
    return new ExtendedData("map-type",
                            new DatumShift(1.23, 4.56),
                            "disk-name",
                            new LicenseInformation(123, new SerialNumber(serialBytes())),
                            "assoc-data",
                            new DigitalMapShop(8, "shop.qc3"));
  }

  @Nested
  @DisplayName("ExtendedData")
  class RecordTests {
    @Test
    void constructor() {
      final var data = createSample();

      assertEquals("map-type", data.mapType());
      assertEquals(1.23, data.datumShift().north());
      assertEquals("disk-name", data.diskName());
      assertEquals("assoc-data", data.associatedData());
      assertEquals("shop.qc3", data.digitalMapShop().qc3Url());
    }

    @Test
    void immutable() {
      final var data = createSample();

      assertEquals("map-type", data.mapType());
      assertEquals("disk-name", data.diskName());
    }

    @Test
    void equalsAndHashCode() {
      final var a = createSample();
      final var b = createSample();

      final var c = new ExtendedData("different",
                                     new DatumShift(1.23, 4.56),
                                     "disk-name",
                                     new LicenseInformation(123, new SerialNumber(serialBytes())),
                                     "assoc-data",
                                     new DigitalMapShop(8, "shop.qc3"));

      assertEquals(a, b);
      assertNotEquals(a, c);
      assertEquals(a.hashCode(), b.hashCode());
    }
  }

  @Nested
  @DisplayName("Encoder/Decoder")
  class CodecTests {

    @Test
    void encodeDecode() {
      final var original = createSample();

      ExtendedData.Encoder.encode(qctWriter, original, 0);
      final var decoded = ExtendedData.Decoder.decode(qctReader, 0);

      assertEquals(original.mapType(), decoded.mapType());
      assertEquals(original.datumShift(), decoded.datumShift());
      assertEquals(original.diskName(), decoded.diskName());
      assertEquals(original.licenseInformation(), decoded.licenseInformation());
      assertEquals(original.associatedData(), decoded.associatedData());
      assertEquals(original.digitalMapShop(), decoded.digitalMapShop());
    }

    @Test
    void encodeDecodeLargeOffset() {
      final int offset = 1024 * 1024;
      final var original = createSample();

      ExtendedData.Encoder.encode(qctWriter, original, offset);
      final var decoded = ExtendedData.Decoder.decode(qctReader, offset);

      assertEquals(original.mapType(), decoded.mapType());
      assertEquals(original.datumShift(), decoded.datumShift());
    }
  }

  @Test
  void roundTrip() {
    final var original = createSample();

    ExtendedData.Encoder.encode(qctWriter, original, 0);
    final var decoded = ExtendedData.Decoder.decode(qctReader, 0);

    assertEquals(original.mapType(), decoded.mapType());
    assertEquals(original.datumShift(), decoded.datumShift());
    assertEquals(original.diskName(), decoded.diskName());
    assertEquals(original.licenseInformation(), decoded.licenseInformation());
    assertEquals(original.associatedData(), decoded.associatedData());
    assertEquals(original.digitalMapShop(), decoded.digitalMapShop());
  }
}