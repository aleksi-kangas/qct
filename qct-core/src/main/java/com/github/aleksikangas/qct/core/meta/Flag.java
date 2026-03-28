/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;

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

  public static final int SIZE = 0x04;

  Flag(final int value) {
    this.mask = value;
  }

  public int mask() {
    return mask;
  }

  public static final class Decoder {
    public static Set<Flag> decode(final QctReader qctReader, final int byteOffset) {
      final int value = qctReader.readInt(byteOffset);
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
    public static void encode(final QctWriter qctWriter, final Set<Flag> flags, final int byteOffset) {
      int bitField = 0;
      for (Flag flag : flags) {
        bitField |= flag.mask();
      }
      qctWriter.writeInt(byteOffset, bitField);
    }

    private Encoder() {
    }
  }
}
