/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

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
}
