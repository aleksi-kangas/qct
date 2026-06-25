/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.ui.QctModel;
import com.github.aleksikangas.qct.ui.async.QctFileToBufferedImageWorker;
import com.github.aleksikangas.qct.ui.image.action.ZoomInAction;
import com.github.aleksikangas.qct.ui.image.action.ZoomOutAction;
import com.github.aleksikangas.qct.ui.image.mouse.MinimapMouseAdapter;
import com.github.aleksikangas.qct.ui.image.mouse.PanMouseMotionListener;
import com.github.aleksikangas.qct.ui.image.state.Coordinates;
import com.github.aleksikangas.qct.ui.image.state.ImageState;
import com.github.aleksikangas.qct.ui.image.state.MinimapState;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Objects;
import java.util.Optional;

public final class ImagePanel extends JPanel {
  private final transient ImageState imageState;

  public ImagePanel(final QctModel qctModel) {
    super(new MigLayout("insets 4", "[fill, grow]", "[fill, grow]"));
    imageState = new ImageState(this::getHeight, this::getWidth);

    registerComponentListeners();
    registerKeybinds();
    registerMouseListeners();
    imageState.addPropertyChangeListener(this::onImageTransform);
    qctModel.addPropertyChangeListener(this::onQctFileEvent);
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

  private Optional<Rectangle> displayedImageRectangle(final BufferedImage image) {
    final Coordinates topLeftImageCoordinates = imageState.panelToImage(Coordinates.panelCoordinates(0, 0));
    final Coordinates bottomRightImageCoordinates = imageState.panelToImage(Coordinates.panelCoordinates(getWidth() -
                                                                                                         1.0,
                                                                                                         getHeight() -
                                                                                                         1.0));
    if (image.getHeight() <= topLeftImageCoordinates.y() ||
        bottomRightImageCoordinates.y() < 0 ||
        image.getWidth() <= topLeftImageCoordinates.x() ||
        bottomRightImageCoordinates.x() < 0) {
      return Optional.empty();
    }
    final int y1 = Math.max(0, topLeftImageCoordinates.yAsInt());
    final int x1 = Math.max(0, topLeftImageCoordinates.xAsInt());
    final int y2 = Math.min(image.getHeight() - 1, bottomRightImageCoordinates.yAsInt());
    final int x2 = Math.min(image.getWidth() - 1, bottomRightImageCoordinates.xAsInt());
    final var rectangle = new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    if (rectangle.height < 1 || rectangle.width < 1) {
      return Optional.empty();
    }
    return Optional.of(rectangle);
  }

  private void onImageTransform(final PropertyChangeEvent e) {
    if (Objects.equals(e.getPropertyName(), ImageState.IMAGE_REPAINT)) {
      repaint();
    }
  }

  private void onQctFileEvent(final PropertyChangeEvent e) {
    if (Objects.equals(e.getPropertyName(), QctModel.QCT_FILE)) {
      final QctFile qctFile = (QctFile) e.getNewValue();
      if (qctFile != null) {
        new QctFileToBufferedImageWorker(qctFile,
                                         image -> imageState.setImage(image, getWidth(), getHeight())).execute();
      } else {
        imageState.clear();
      }
    }
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
