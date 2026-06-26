/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.image.state;

import com.github.aleksikangas.qct.ui.image.action.AbstractZoomAction;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

public final class ImageState {
  private final Set<ImageStateListener> imageStateListeners = new CopyOnWriteArraySet<>();
  private final MinimapState minimapState = new MinimapState();

  private final Supplier<Integer> panelHeightSupplier;
  private final Supplier<Integer> panelWidthSupplier;

  @Nullable
  private BufferedImage image = null;

  private double imageFitScale = 0.0;
  private double imageScale = 0.0;

  /**
   * Image top-left corner in panel coordinates.
   */
  private final Point origin = new Point(0, 0);

  public ImageState(final Supplier<Integer> panelHeightSupplier, final Supplier<Integer> panelWidthSupplier) {
    this.panelHeightSupplier = Objects.requireNonNull(panelHeightSupplier);
    this.panelWidthSupplier = Objects.requireNonNull(panelWidthSupplier);
  }

  public void addListener(final ImageStateListener imageStateListener) {
    imageStateListeners.add(Objects.requireNonNull(imageStateListener));
  }

  public void removeListener(final ImageStateListener imageStateListener) {
    imageStateListeners.remove(Objects.requireNonNull(imageStateListener));
  }

  public MinimapState getMinimapState() {
    return minimapState;
  }

  public Optional<BufferedImage> getImage() {
    return Optional.ofNullable(image);
  }

  public int getHeight() {
    return (int) (imageScale * (image != null ? image.getHeight() : 0));
  }

  public int getWidth() {
    return (int) (imageScale * (image != null ? image.getWidth() : 0));
  }

  public double getImageScale() {
    return imageScale;
  }

  public int getOriginY() {
    return Math.toIntExact(Math.round(origin.getY()));
  }

  public int getOriginX() {
    return Math.toIntExact(Math.round(origin.getX()));
  }

  public boolean isWholeImageDisplayed(final int availableHeight, final int availableWidth) {
    return 0 <= getOriginY() &&
           (getOriginY() + getHeight()) <= availableHeight &&
           0 <= getOriginX() &&
           (getOriginX() + getWidth()) <= availableWidth;
  }

  public double getCurrentZoom() {
    return imageScale / imageFitScale;
  }

  public Coordinates panelToImage(final Coordinates panelCoordinates) {
    return new Coordinates((panelCoordinates.y() - getOriginY()) / imageScale,
                           (panelCoordinates.x() - getOriginX()) / imageScale);
  }

  public Coordinates imageToPanel(final Coordinates imageCoordinates) {
    return new Coordinates(imageCoordinates.y() * imageScale + getOriginY(),
                           imageCoordinates.x() * imageScale + getOriginX());
  }

  public Coordinates minimapToImage(final Coordinates minimapCoordinates) {
    return new Coordinates(minimapCoordinates.y() * getHeight() / minimapState.getHeight(),
                           minimapCoordinates.x() * getWidth() / minimapState.getWidth());
  }

  public void setImage(final BufferedImage image, final int width, final int height) {
    this.image = Objects.requireNonNull(image);
    onResize(width, height);
    notifyListeners();
  }

  public void clear() {
    image = null;
    imageFitScale = 0.0;
    imageScale = 0.0;
    origin.setLocation(0, 0);
    minimapState.clear();
    notifyListeners();
  }

  public void centerAt(final Coordinates imageCoordinates) {
    if (image == null) return;
    origin.setLocation(panelWidthSupplier.get() / 2 - imageCoordinates.xAsInt(),
                       panelHeightSupplier.get() / 2 - imageCoordinates.yAsInt());
    notifyListeners();
  }

  public void resize(final int width, final int height) {
    if (image == null) return;
    onResize(width, height);
    notifyListeners();
  }

  public void translateOrigin(final int dy, final int dx) {
    if (image == null) return;
    origin.translate(dx, dy);
    notifyListeners();
  }

  public void zoom(final double zoomFactor) {
    if (image == null) return;
    final int panelWidth = panelWidthSupplier.get();
    final int panelHeight = panelHeightSupplier.get();
    final var centerPanelCoordinates = Coordinates.panelCoordinates(panelHeight / 2.0, panelWidth / 2.0);
    final var centerImageCoordinates = panelToImage(centerPanelCoordinates);
    imageScale = Math.clamp(imageScale * zoomFactor,
                            AbstractZoomAction.ZOOM_MIN * imageFitScale,
                            AbstractZoomAction.ZOOM_MAX * imageFitScale);
    origin.setLocation(centerPanelCoordinates.x() - centerImageCoordinates.x() * imageScale,
                       centerPanelCoordinates.y() - centerImageCoordinates.y() * imageScale);
    notifyListeners();
  }

  private void onResize(final int width, final int height) {
    if (image == null) return;
    imageFitScale = Math.min((double) height / image.getHeight(), (double) width / image.getWidth());
    imageScale = imageFitScale;
    origin.setLocation((width - getWidth()) / 2, (height - getHeight()) / 2);
    minimapState.update(image, height);
  }

  private void notifyListeners() {
    imageStateListeners.forEach(ImageStateListener::onImageStateChange);
  }
}
