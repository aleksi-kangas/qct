/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.ui.file.QctFileManager;
import com.github.aleksikangas.qct.ui.image.action.ZoomInAction;
import com.github.aleksikangas.qct.ui.image.action.ZoomOutAction;
import com.github.aleksikangas.qct.ui.image.mouse.MinimapMouseAdapter;
import com.github.aleksikangas.qct.ui.image.mouse.PanMouseMotionListener;
import com.github.aleksikangas.qct.ui.image.state.ImageState;
import com.github.aleksikangas.qct.ui.image.state.MinimapState;
import com.github.aleksikangas.qct.ui.image.worker.QctFileToBufferedImageWorker;
import com.github.aleksikangas.qct.ui.utils.ThreadUtils;
import net.miginfocom.swing.MigLayout;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public final class ImagePanel extends JPanel {
  private final transient ImageState imageState;

  public ImagePanel(final QctFileManager qctFileManager) {
    super(new MigLayout("insets 4", "[fill, grow]", "[fill, grow]"));
    imageState = new ImageState(this::getHeight, this::getWidth);

    registerComponentListeners();
    registerKeybinds();
    registerMouseListeners();
    imageState.addListener(this::onImageStateChange);
    qctFileManager.addListener(this::onQctFile);
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    paintImage(g);
    paintMinimap(g);
  }

  private void paintImage(final Graphics g) {
    imageState.getImage()
              .ifPresent(image -> g.drawImage(image,
                                              imageState.getOriginX(),
                                              imageState.getOriginY(),
                                              imageState.getWidth(),
                                              imageState.getHeight(),
                                              null));
  }

  private void paintMinimap(final Graphics g) {
    final MinimapState minimapState = imageState.getMinimapState();
    minimapState.getMinimapImage().ifPresent(minimapImage -> {
      final int minimapWidth = minimapState.getWidth();
      final int minimapHeight = minimapState.getHeight();

      g.drawImage(minimapImage, 0, 0, minimapWidth, minimapHeight, null);
      g.setColor(Color.BLACK);
      g.drawRect(0, 0, minimapWidth, minimapHeight);
      if (!imageState.isWholeImageDisplayed(getHeight(), getWidth())) {
        g.setColor(Color.RED);
        g.drawRect(-imageState.getOriginX() * minimapWidth / imageState.getWidth(),
                   -imageState.getOriginY() * minimapHeight / imageState.getHeight(),
                   getWidth() * minimapWidth / imageState.getWidth(),
                   getHeight() * minimapHeight / imageState.getHeight());
      }
    });

  }

  private void onImageStateChange() {
    repaint();
  }

  private void onQctFile(@Nullable final QctFile qctFile) {
    ThreadUtils.runOnEDT(() -> {
      if (qctFile != null) {
        new QctFileToBufferedImageWorker(qctFile,
                                         image -> imageState.setImage(image, getWidth(), getHeight())).execute();
      } else {
        imageState.clear();
      }
    });
  }

  private void registerComponentListeners() {
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        imageState.resize(getWidth(), getHeight());
      }
    });
  }

  private void registerKeybinds() {
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(ZoomInAction.KEY_CODE_PRIMARY, 0),
                                                       ZoomInAction.KEY);
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(ZoomInAction.KEY_CODE_SECONDARY, 0),
                                                       ZoomInAction.KEY);
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(ZoomOutAction.KEY_CODE_PRIMARY, 0),
                                                       ZoomOutAction.KEY);
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(ZoomOutAction.KEY_CODE_SECONDARY, 0),
                                                       ZoomOutAction.KEY);
    getActionMap().put(ZoomInAction.KEY, new ZoomInAction(imageState));
    getActionMap().put(ZoomOutAction.KEY, new ZoomOutAction(imageState));
  }

  private void registerMouseListeners() {
    addMouseListener(new MinimapMouseAdapter(imageState));
    addMouseMotionListener(new PanMouseMotionListener(imageState));
  }
}
