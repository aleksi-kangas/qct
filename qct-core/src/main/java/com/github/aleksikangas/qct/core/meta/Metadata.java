/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * <pre>
 * +--------+--------------+----------------------------------+
 * | Offset | Size (Bytes) | Content                          |
 * +--------+--------------+----------------------------------+
 * | 0x0000 | 24 x 4       | Meta Data - 24 Integers/Pointers |
 * +--------+--------------+----------------------------------+
 *
 * +--------+-------------------+--------------------------------------------------------+
 * | Offset | Data Type         | Content                                                |
 * +--------+-------------------+--------------------------------------------------------+
 * | 0x00   | Integer           | Magic Number                                           |
 * |        |                   | 0x1423D5FE - Quick Chart Information                   |
 * |        |                   | 0x1423D5FF - Quick Chart Map                           |
 * | 0x04   | Integer           | File Format Version                                    |
 * |        |                   | 0x00000002 – Quick Chart                               |
 * |        |                   | 0x00000004 – Quick Chart supporting License Management |
 * |        |                   | 0x20000001 – QC3 Format                                |
 * | 0x08   | Integer           | Width (Tiles)                                          |
 * | 0x0C   | Integer           | Height (Tiles)                                         |
 * | 0x10   | Pointer to String | Long Title                                             |
 * | 0x14   | Pointer to String | Name                                                   |
 * | 0x18   | Pointer to String | Identifier                                             |
 * | 0x1C   | Pointer to String | Edition                                                |
 * | 0x20   | Pointer to String | Revision                                               |
 * | 0x24   | Pointer to String | Keywords                                               |
 * | 0x28   | Pointer to String | Copyright                                              |
 * | 0x2C   | Pointer to String | Scale                                                  |
 * | 0x30   | Pointer to String | Datum                                                  |
 * | 0x34   | Pointer to String | Depths                                                 |
 * | 0x38   | Pointer to String | Heights                                                |
 * | 0x3C   | Pointer to String | Projection                                             |
 * | 0x40   | Integer           | Bit-field Flags                                        |
 * |        |                   | Bit 0 - Must have original file                        |
 * |        |                   | Bit 1 - Allow Calibration                              |
 * | 0x44   | Pointer to String | Original File Name                                     |
 * | 0x48   | Integer           | Original File Size                                     |
 * | 0x4C   | Integer           | Original File Creation Time (seconds since epoch)      |
 * | 0x50   | Integer           | Reserved, set to 0                                     |
 * | 0x54   | Pointer to Struct | Extended Data Structure (see section 6.2.1)            |
 * | 0x58   | Integer           | Number of Map Outline Points                           |
 * | 0x5C   | Pointer to Array  | Map Outline (see section 6.2.2)                        |
 * +--------+-------------------+--------------------------------------------------------+
 * </pre>
 */
public record Metadata(MagicNumber magicNumber,
                       FileFormatVersion fileFormatVersion,
                       int widthTiles,
                       int heightTiles,
                       String longTitle,
                       String name,
                       String identifier,
                       String edition,
                       String revision,
                       String keywords,
                       String copyright,
                       String scale,
                       String datum,
                       String depths,
                       String heights,
                       String projection,
                       Set<Flag> flags,
                       String originalFileName,
                       int originalFileSize,
                       Instant originalFileCreationTime,
                       ExtendedData extendedData,
                       MapOutline mapOutline) {
  public static final int BYTE_OFFSET = 0x0000;
  public static final int HEADER_SIZE = 24 * 0x04;

  public Metadata {
    Objects.requireNonNull(magicNumber);
    Objects.requireNonNull(fileFormatVersion);
    Objects.requireNonNull(longTitle);
    Objects.requireNonNull(name);
    Objects.requireNonNull(identifier);
    Objects.requireNonNull(edition);
    Objects.requireNonNull(revision);
    Objects.requireNonNull(keywords);
    Objects.requireNonNull(copyright);
    Objects.requireNonNull(scale);
    Objects.requireNonNull(datum);
    Objects.requireNonNull(depths);
    Objects.requireNonNull(heights);
    Objects.requireNonNull(projection);
    Objects.requireNonNull(flags);
    Objects.requireNonNull(originalFileName);
    Objects.requireNonNull(originalFileCreationTime);
    Objects.requireNonNull(extendedData);
    Objects.requireNonNull(mapOutline);
  }

  @Nonnull
  @Override
  public String toString() {
    return "{" +
           "magicNumber=" +
           magicNumber +
           ", fileFormatVersion=" +
           fileFormatVersion +
           ", widthTiles=" +
           widthTiles +
           ", heightTiles=" +
           heightTiles +
           ", longTitle='" +
           longTitle +
           '\'' +
           ", name='" +
           name +
           '\'' +
           ", identifier='" +
           identifier +
           '\'' +
           ", edition='" +
           edition +
           '\'' +
           ", revision='" +
           revision +
           '\'' +
           ", keywords='" +
           keywords +
           '\'' +
           ", copyright='" +
           copyright +
           '\'' +
           ", scale='" +
           scale +
           '\'' +
           ", datum='" +
           datum +
           '\'' +
           ", depths='" +
           depths +
           '\'' +
           ", heights='" +
           heights +
           '\'' +
           ", projection='" +
           projection +
           '\'' +
           ", flags=" +
           flags +
           ", originalFileName='" +
           originalFileName +
           '\'' +
           ", originalFileSize=" +
           originalFileSize +
           ", originalFileCreationTime=" +
           originalFileCreationTime +
           ", extendedData=" +
           extendedData +
           ", mapOutline=" +
           mapOutline +
           '}';
  }

  public static final class Decoder {
    public static Metadata decode(final QctReader qctReader) {
      return new Metadata(MagicNumber.Decoder.decode(qctReader, Metadata.BYTE_OFFSET),
                          FileFormatVersion.Decoder.decode(qctReader, Math.toIntExact(Metadata.BYTE_OFFSET + 0x04L)),
                          qctReader.readInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x08L)),
                          qctReader.readInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x0CL)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x10L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x14L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x18L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x1CL)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x20L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x24L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x28L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x2CL)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x30L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x34L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x38L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x3CL)),
                          Flag.Decoder.decode(qctReader, Math.toIntExact(Metadata.BYTE_OFFSET + 0x40L)),
                          qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x44L)),
                          qctReader.readInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x48L)),
                          Instant.ofEpochSecond(qctReader.readInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x4CL))),
                          ExtendedData.Decoder.decodeFromPointer(qctReader,
                                                                 Math.toIntExact(Metadata.BYTE_OFFSET + 0x54L)),
                          MapOutline.Decoder.decode(qctReader, Math.toIntExact(Metadata.BYTE_OFFSET + 0x58L)));
    }

    private Decoder() {

    }
  }

  public static final class Encoder {
    public static void encode(final QctWriter qctWriter, final Metadata metadata) {
      Objects.requireNonNull(metadata);

      MagicNumber.Encoder.encode(qctWriter, metadata.magicNumber, Metadata.BYTE_OFFSET);
      FileFormatVersion.Encoder.encode(qctWriter,
                                       metadata.fileFormatVersion,
                                       Math.toIntExact(Metadata.BYTE_OFFSET + 0x04L));
      qctWriter.writeInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x08L), metadata.widthTiles);
      qctWriter.writeInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x0CL), metadata.heightTiles);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x10L), metadata.longTitle);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x14L), metadata.name);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x18L), metadata.identifier);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x1CL), metadata.edition);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x20L), metadata.revision);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x24L), metadata.keywords);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x28L), metadata.copyright);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x2CL), metadata.scale);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x30L), metadata.datum);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x34L), metadata.depths);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x38L), metadata.heights);
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x3CL), metadata.projection);
      Flag.Encoder.encode(qctWriter, metadata.flags, Math.toIntExact(Metadata.BYTE_OFFSET + 0x40L));
      qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x44L), metadata.originalFileName);
      qctWriter.writeInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x48L), metadata.originalFileSize);
      qctWriter.writeInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x4CL),
                         (int) metadata.originalFileCreationTime.getEpochSecond());
      ExtendedData.Encoder.encodeWithPointer(qctWriter,
                                             metadata.extendedData,
                                             Math.toIntExact(Metadata.BYTE_OFFSET + 0x54L));
      MapOutline.Encoder.encode(qctWriter, metadata.mapOutline, Math.toIntExact(Metadata.BYTE_OFFSET + 0x58L));
    }

    private Encoder() {
    }
  }
}
