/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image.state;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public final class MinimapState {
  public static final double MINIMAP_SIZE_FACTOR = 0.2;

  @Nullable
  private BufferedImage minimapImage = null;
  private int minimapHeight = 0;
  private int minimapWidth = 0;

  public Optional<BufferedImage> getMinimapImage() {
    return Optional.ofNullable(minimapImage);
  }

  public int getWidth() {
    return minimapWidth;
  }

  public int getHeight() {
    return minimapHeight;
  }

  public boolean isWithinMinimap(final Point point) {
    return 0 <= point.y && point.y < minimapHeight && 0 <= point.x && point.x < minimapWidth;
  }

  public void update(final BufferedImage image, final int availableHeight) {
    minimapHeight = (int) (availableHeight * MINIMAP_SIZE_FACTOR);
    minimapWidth = minimapHeight * image.getWidth() / image.getHeight();
    minimapImage = new BufferedImage(minimapWidth, minimapHeight, image.getType());
    minimapImage.getGraphics().drawImage(image, 0, 0, minimapWidth, minimapHeight, null);
  }

  public void clear() {
    minimapImage = null;
    minimapHeight = 0;
    minimapWidth = 0;
  }
}
