/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image.action;

import com.github.aleksikangas.qct.ui.image.state.ImageState;

import javax.swing.*;
import java.util.Objects;

public abstract class AbstractZoomAction extends AbstractAction {
  public static final double ZOOM_MIN = 0.33;
  public static final double ZOOM_MAX = 50.0;

  protected static final double ZOOM_STEP = 0.2;

  protected final transient ImageState imageState;

  protected AbstractZoomAction(final ImageState imageState) {
    this.imageState = Objects.requireNonNull(imageState);
  }

  protected final void zoomImage(final double zoomFactor) {
    imageState.zoom(zoomFactor);
  }
}
