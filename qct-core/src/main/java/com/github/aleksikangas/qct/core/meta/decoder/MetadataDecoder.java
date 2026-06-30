/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decoder;

import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.meta.encoder.MetadataEncoder;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.time.Instant;

/**
 * @see Metadata
 * @see MetadataEncoder
 */
public final class MetadataDecoder {
  public static Metadata decode(final QctReader qctReader) {
    return new Metadata(MagicNumberDecoder.decode(qctReader, Metadata.BYTE_OFFSET),
                        FileFormatVersionDecoder.decode(qctReader, Math.toIntExact(Metadata.BYTE_OFFSET + 0x04L)),
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
                        FlagDecoder.decode(qctReader, Math.toIntExact(Metadata.BYTE_OFFSET + 0x40L)),
                        qctReader.readStringFromPointer(Math.toIntExact(Metadata.BYTE_OFFSET + 0x44L)),
                        qctReader.readInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x48L)),
                        Instant.ofEpochSecond(qctReader.readInt(Math.toIntExact(Metadata.BYTE_OFFSET + 0x4CL))),
                        ExtendedDataDecoder.decodeFromPointer(qctReader, Math.toIntExact(Metadata.BYTE_OFFSET + 0x54L)),
                        MapOutlineDecoder.decode(qctReader, Math.toIntExact(Metadata.BYTE_OFFSET + 0x58L)));
  }

  private MetadataDecoder() {
  }
}
