/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image.action;


import com.github.aleksikangas.qct.ui.image.state.ImageState;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public final class ZoomOutAction extends AbstractZoomAction {
  public static final String KEY = "ZOOM_OUT";
  public static final int KEY_CODE_PRIMARY = KeyEvent.VK_MINUS;
  public static final int KEY_CODE_SECONDARY = KeyEvent.VK_SUBTRACT;

  public ZoomOutAction(final ImageState imageState) {
    super(imageState);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    zoomImage(1.0 - ZOOM_STEP);
  }
}
