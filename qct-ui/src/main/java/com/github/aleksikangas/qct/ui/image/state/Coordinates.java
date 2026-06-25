/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image.state;

public record Coordinates(double y,
                          double x) {
  public int yAsInt() {
    return (int) Math.round(y);
  }

  public int xAsInt() {
    return (int) Math.round(x);
  }

  public static Coordinates panelCoordinates(final double y, final double x) {
    return new Coordinates(y, x);
  }

  public static Coordinates imageCoordinates(final double y, final double x) {
    return new Coordinates(y, x);
  }

  public static Coordinates minimapCoordinates(final double y, final double x) {
    return new Coordinates(y, x);
  }
}
