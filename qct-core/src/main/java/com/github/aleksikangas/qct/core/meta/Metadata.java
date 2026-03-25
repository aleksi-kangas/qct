/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

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
  public static final long BYTE_OFFSET = 0x0000L;

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
}
