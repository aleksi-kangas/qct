/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.github.aleksikangas.qct.ui.image.ImagePanel;
import com.github.aleksikangas.qct.ui.meta.MetadataPanel;
import com.github.aleksikangas.qct.ui.model.QctModel;
import com.github.aleksikangas.qct.ui.toolbar.Toolbar;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public final class QctApplicationFrame extends JFrame {
  private final transient QctModel qctModel = new QctModel();

  public QctApplicationFrame() {
    super("QCT");
    setContentPane(createContentPane());
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    pack();
    setLocationRelativeTo(null);
  }

  static void main() {
    FlatMacDarkLaf.setup();
    SwingUtilities.invokeLater(() -> {
      final var qctApplicationFrame = new QctApplicationFrame();
      qctApplicationFrame.setVisible(true);
    });
  }

  private JPanel createContentPane() {
    final var contentPane = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[pref!][grow]"));
    contentPane.add(new Toolbar(qctModel), "growx, wrap");
    final var metadataPanel = new MetadataPanel(qctModel);
    final var metadataScrollPane = new JScrollPane(metadataPanel);
    metadataScrollPane.setBorder(BorderFactory.createEmptyBorder());
    final var imageDisplayPanel = new ImagePanel(qctModel);
    final var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, metadataScrollPane, imageDisplayPanel);
    splitPane.setContinuousLayout(true);
    splitPane.setPreferredSize(new Dimension(1920, 1080));
    splitPane.setResizeWeight(0.25);
    contentPane.add(splitPane, "grow, push, wrap");
    return contentPane;
  }
}
