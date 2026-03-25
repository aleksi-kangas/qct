/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

/**
 * <pre>
 * +--------+-----------+-------------------+
 * | Offset | Data Type | Content           |
 * +--------+-----------+-------------------+
 * | 0x00   | Double    | Datum Shift North |
 * | 0x08   | Double    | Datum Shift East  |
 * +--------+-----------+-------------------+
 * </pre>
 */
public record DatumShift(double north,
                         double east) {
}
