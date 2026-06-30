/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.encoder;

import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.meta.decoder.MetadataDecoder;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.util.Objects;

/**
 * @see Metadata
 * @see MetadataDecoder
 */
public final class MetadataEncoder {
  public static void encode(final QctWriter qctWriter, final Metadata metadata) {
    Objects.requireNonNull(metadata);

    MagicNumberEncoder.encode(qctWriter, metadata.magicNumber(), Metadata.BYTE_OFFSET);
    FileFormatVersionEncoder.encode(qctWriter,
                                    metadata.fileFormatVersion(),
                                    Math.toIntExact(Metadata.BYTE_OFFSET + 0x04L));
    qctWriter.writeInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x08L), metadata.widthTiles());
    qctWriter.writeInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x0CL), metadata.heightTiles());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x10L), metadata.longTitle());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x14L), metadata.name());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x18L), metadata.identifier());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x1CL), metadata.edition());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x20L), metadata.revision());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x24L), metadata.keywords());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x28L), metadata.copyright());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x2CL), metadata.scale());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x30L), metadata.datum());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x34L), metadata.depths());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x38L), metadata.heights());
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x3CL), metadata.projection());
    FlagEncoder.encode(qctWriter, metadata.flags(), Math.toIntExact(Metadata.BYTE_OFFSET + 0x40L));
    qctWriter.allocateWriteString(Math.toIntExact(Metadata.BYTE_OFFSET + 0x44L), metadata.originalFileName());
    qctWriter.writeInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x48L), metadata.originalFileSize());
    qctWriter.writeInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x4CL),
                       (int) metadata.originalFileCreationTime().getEpochSecond());
    ExtendedDataEncoder.encodeWithPointer(qctWriter,
                                          metadata.extendedData(),
                                          Math.toIntExact(Metadata.BYTE_OFFSET + 0x54L));
    MapOutlineEncoder.encode(qctWriter, metadata.mapOutline(), Math.toIntExact(Metadata.BYTE_OFFSET + 0x58L));
  }

  private MetadataEncoder() {
  }
}
