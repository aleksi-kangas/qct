/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.DirectQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetadataTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("metadata-test", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, 2048);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  private Metadata createSampleMetadata() {
    MapOutline.Point[] points = { new MapOutline.Point(1.0, 2.0), new MapOutline.Point(3.0, 4.0) };
    MapOutline mapOutline = new MapOutline(points);

    ExtendedData extendedData = new ExtendedData("MapType",
                                                 new DatumShift(0.0, 0.0),
                                                 "DiskName",
                                                 new LicenseInformation(1, new SerialNumber(new int[32])),
                                                 "AssociatedData",
                                                 new DigitalMapShop(8, "shop.qc3"));

    return new Metadata(MagicNumber.QUICK_CHART_MAP,
                        FileFormatVersion.QUICK_CHART,
                        10,
                        20,
                        "LongTitle",
                        "Name",
                        "Identifier",
                        "Edition",
                        "Revision",
                        "Keywords",
                        "Copyright",
                        "Scale",
                        "Datum",
                        "Depths",
                        "Heights",
                        "Projection",
                        Set.of(),
                        "OriginalFile.qc3",
                        12345,
                        Instant.ofEpochSecond(1_650_000_000L),
                        extendedData,
                        mapOutline);
  }

  @Test
  void encodeDecodeRoundTrip() {
    final Metadata original = createSampleMetadata();

    Metadata.Encoder.encode(qctWriter, original);

    final Metadata decoded = Metadata.Decoder.decode(qctReader);

    assertEquals(original.magicNumber(), decoded.magicNumber());
    assertEquals(original.fileFormatVersion(), decoded.fileFormatVersion());
    assertEquals(original.widthTiles(), decoded.widthTiles());
    assertEquals(original.heightTiles(), decoded.heightTiles());
    assertEquals(original.longTitle(), decoded.longTitle());
    assertEquals(original.name(), decoded.name());
    assertEquals(original.identifier(), decoded.identifier());
    assertEquals(original.edition(), decoded.edition());
    assertEquals(original.revision(), decoded.revision());
    assertEquals(original.keywords(), decoded.keywords());
    assertEquals(original.copyright(), decoded.copyright());
    assertEquals(original.scale(), decoded.scale());
    assertEquals(original.datum(), decoded.datum());
    assertEquals(original.depths(), decoded.depths());
    assertEquals(original.heights(), decoded.heights());
    assertEquals(original.projection(), decoded.projection());
    assertEquals(original.flags(), decoded.flags());
    assertEquals(original.originalFileName(), decoded.originalFileName());
    assertEquals(original.originalFileSize(), decoded.originalFileSize());
    assertEquals(original.originalFileCreationTime(), decoded.originalFileCreationTime());

    assertEquals(original.extendedData().mapType(), decoded.extendedData().mapType());
    assertEquals(original.extendedData().diskName(), decoded.extendedData().diskName());
    assertEquals(original.extendedData().associatedData(), decoded.extendedData().associatedData());
    assertEquals(original.extendedData().digitalMapShop(), decoded.extendedData().digitalMapShop());

    assertEquals(original.mapOutline(), decoded.mapOutline());
  }

  @Test
  void constructorAndGetters() {
    Metadata metadata = createSampleMetadata();
    assertEquals(10, metadata.widthTiles());
    assertEquals(20, metadata.heightTiles());
    assertEquals("LongTitle", metadata.longTitle());
    assertEquals("OriginalFile.qc3", metadata.originalFileName());
    assertNotNull(metadata.originalFileCreationTime());
  }
}
