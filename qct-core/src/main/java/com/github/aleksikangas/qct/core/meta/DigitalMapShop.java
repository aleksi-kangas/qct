/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import java.util.Objects;

/**
 * <pre>
 * +--------+-------------------+-----------------------------+
 * | Offset | Data Type         | Content                     |
 * +--------+-------------------+-----------------------------+
 * | 0x00   | Integer           | Structure size, set to 8    |
 * | 0x04   | Pointer to String | Partial URL to QC3 map file |
 * +--------+-------------------+-----------------------------+
 * </pre>
 */
public record DigitalMapShop(int size,
                             String qc3Url) {
  public DigitalMapShop {
    Objects.requireNonNull(qc3Url);
  }
}
