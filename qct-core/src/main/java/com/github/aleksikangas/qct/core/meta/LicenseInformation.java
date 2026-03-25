/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

import java.util.Objects;

/**
 * <pre>
 * +--------+-------------------+--------------------------------------------------------------------------------------+
 * | Offset | Data Type         | Content                                                                              |
 * +--------+-------------------+--------------------------------------------------------------------------------------+
 * | 0x00   | Integer           | Identifier of the license.                                                           |
 * |        |                   | This correlates with name of the license file used that must be paired with the map. |
 * | 0x04   | Integer           | Unknown                                                                              |
 * | 0x08   | Integer           | Unknown                                                                              |
 * | 0x0C   | Pointer to String | License description                                                                  |
 * | 0x10   | Pointer to Struct | Serial Number                                                                        |
 * | 0x14   | Integer           | Unknown                                                                              |
 * | 0x18   | 16 Bytes          | Unknown, set to 0                                                                    |
 * | 0x28   | 64 Bytes          | Unknown                                                                              |
 * +--------+-------------------+--------------------------------------------------------------------------------------+
 * </pre>
 */
public record LicenseInformation(int identifier,
                                 String description,
                                 SerialNumber serialNumber) {
  public LicenseInformation {
    Objects.requireNonNull(description);
    Objects.requireNonNull(serialNumber);
  }
}
