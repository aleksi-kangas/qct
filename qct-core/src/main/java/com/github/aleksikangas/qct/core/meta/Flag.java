/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import java.nio.channels.FileChannel;
import java.util.EnumSet;
import java.util.Set;

/**
 * <pre>
 * +--------+-------------------+---------------------------------+
 * | Offset | Data Type         | Content                         |
 * +--------+-------------------+---------------------------------+
 * | 0x40   | Integer Bit-Field | Flags                           |
 * |        |                   | Bit 0 - Must have original file |
 * |        |                   | Bit 1 - Allow calibration       |
 * +--------+-------------------+---------------------------------+
 * </pre>
 */
public enum Flag {
  MUST_HAVE_ORIGINAL_FILE(1),
  ALLOW_CALIBRATION(1 << 1);

  private final int mask;

  Flag(final int value) {
    this.mask = value;
  }

  public int mask() {
    return mask;
  }

  public static final class Decoder {
    public static Set<Flag> decode(final FileChannel fileChannel, final long byteOffset) {
      final int value = QctReader.readInt(fileChannel, byteOffset);
      final Set<Flag> flags = EnumSet.noneOf(Flag.class);
      if ((value & MUST_HAVE_ORIGINAL_FILE.mask) != 0) {
        flags.add(MUST_HAVE_ORIGINAL_FILE);
      }
      if ((value & ALLOW_CALIBRATION.mask) != 0) {
        flags.add(ALLOW_CALIBRATION);
      }
      return flags;
    }

    private Decoder() {
    }
  }

  public static final class Encoder {
    public static void encode(final Set<Flag> flags, final FileChannel fileChannel, final long byteOffset) {
      int bitField = 0;
      for (Flag flag : flags) {
        bitField |= flag.mask();
      }
      QctWriter.writeInt(fileChannel, byteOffset, bitField);
    }

    private Encoder() {
    }
  }
}
