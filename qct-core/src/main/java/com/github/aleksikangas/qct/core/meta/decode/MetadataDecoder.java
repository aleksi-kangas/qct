/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.decode;

import com.github.aleksikangas.qct.core.meta.FileFormatVersion;
import com.github.aleksikangas.qct.core.meta.Flag;
import com.github.aleksikangas.qct.core.meta.MagicNumber;
import com.github.aleksikangas.qct.core.meta.Metadata;
import com.github.aleksikangas.qct.core.utils.QctReader;

import java.nio.channels.AsynchronousFileChannel;
import java.time.Instant;

/**
 * A decoder of {@link Metadata}.
 *
 * @see Metadata
 */
public final class MetadataDecoder {
  public static Metadata decode(final AsynchronousFileChannel asyncFileChannel) {
    return new Metadata(MagicNumber.of(QctReader.readInt(asyncFileChannel, Metadata.BYTE_OFFSET)),
                        FileFormatVersion.of(QctReader.readInt(asyncFileChannel, Metadata.BYTE_OFFSET + 0x04L)),
                        QctReader.readInt(asyncFileChannel, Metadata.BYTE_OFFSET + 0x08L),
                        QctReader.readInt(asyncFileChannel, Metadata.BYTE_OFFSET + 0x0CL),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x10L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x14L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x18L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x1CL),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x20L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x24L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x28L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x2CL),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x30L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x34L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x38L),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x3CL),
                        Flag.flagsOf(QctReader.readInt(asyncFileChannel, Metadata.BYTE_OFFSET + 0x40)),
                        QctReader.readStringFromPointer(asyncFileChannel, Metadata.BYTE_OFFSET + 0x44L),
                        QctReader.readInt(asyncFileChannel, Metadata.BYTE_OFFSET + 0x48L),
                        Instant.ofEpochSecond(QctReader.readInt(asyncFileChannel, Metadata.BYTE_OFFSET + 0x4CL)),
                        ExtendedDataDecoder.decode(asyncFileChannel,
                                                   QctReader.readPointer(asyncFileChannel,
                                                                         Metadata.BYTE_OFFSET + 0x54L)),
                        MapOutlineDecoder.decode(asyncFileChannel, Metadata.BYTE_OFFSET + 0x58L));
  }

  private MetadataDecoder() {
  }
}
