/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image.mouse;

import com.github.aleksikangas.qct.ui.image.state.Coordinates;
import com.github.aleksikangas.qct.ui.image.state.ImageState;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public final class MinimapMouseAdapter extends MouseAdapter {
  private final ImageState imageState;

  public MinimapMouseAdapter(final ImageState state) {
    this.imageState = Objects.requireNonNull(state);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && imageState.getMinimapState().isWithinMinimap(e.getPoint())) {
      final Coordinates imageCoordinates = imageState.minimapToImage(Coordinates.minimapCoordinates(e.getY(),
                                                                                                    e.getX()));
      imageState.centerAt(imageCoordinates);
    }
  }
}
