/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image.mouse;

import com.github.aleksikangas.qct.ui.image.state.ImageState;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Objects;

public final class PanMouseMotionListener implements MouseMotionListener {
  private final ImageState imageState;

  @Nullable
  private Point mousePosition = null;

  public PanMouseMotionListener(final ImageState imageState) {
    this.imageState = Objects.requireNonNull(imageState);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && !imageState.getMinimapState().isWithinMinimap(e.getPoint())) {
      Objects.requireNonNull(mousePosition);
      final Point panPosition = e.getPoint();
      imageState.translateOrigin(panPosition.y - mousePosition.y, panPosition.x - mousePosition.x);
      mousePosition = panPosition;
    }
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    mousePosition = e.getPoint();
  }
}
