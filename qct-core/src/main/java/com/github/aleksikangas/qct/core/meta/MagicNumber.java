/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta;

/**
 * <pre>
 * +--------+-----------+--------------------------------------+
 * | Offset | Data Type | Content                              |
 * +--------+-----------+--------------------------------------+
 * | 0x00   | Integer   | Magic Number                         |
 * |        |           | 0x1423D5FE - Quick Chart Information |
 * |        |           | 0x1423D5FF - Quick Chart Map         |
 * +--------+-----------+--------------------------------------+
 * </pre>
 */
public enum MagicNumber {
  QUICK_CHART_INFORMATION(0x1423D5FE),
  QUICK_CHART_MAP(0x1423D5FF);

  private final int value;

  public static final int SIZE = 0x04;

  MagicNumber(final int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }

  @Override
  public String toString() {
    return switch (this) {
      case QUICK_CHART_INFORMATION -> "Quick Chart Information";
      case QUICK_CHART_MAP -> "Quick Chart Map";
    };
  }
}
