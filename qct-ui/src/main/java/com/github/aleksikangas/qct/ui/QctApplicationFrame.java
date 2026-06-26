/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.github.aleksikangas.qct.ui.export.ExportManager;
import com.github.aleksikangas.qct.ui.file.QctFileManager;
import com.github.aleksikangas.qct.ui.footer.Footer;
import com.github.aleksikangas.qct.ui.header.Header;
import com.github.aleksikangas.qct.ui.image.ImagePanel;
import com.github.aleksikangas.qct.ui.meta.MetadataPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public final class QctApplicationFrame extends JFrame {
  private final transient ExportManager exportManager = new ExportManager();
  private final transient QctFileManager qctFileManager = new QctFileManager();

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
    final var contentPane = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[pref!][grow][pref!]"));
    contentPane.add(new Header(exportManager, qctFileManager), "growx, wrap");
    final var metadataPanel = new MetadataPanel(qctFileManager);
    final var metadataScrollPane = new JScrollPane(metadataPanel);
    metadataScrollPane.setBorder(BorderFactory.createEmptyBorder());
    final var imageDisplayPanel = new ImagePanel(qctFileManager);
    final var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, metadataScrollPane, imageDisplayPanel);
    splitPane.setContinuousLayout(true);
    splitPane.setPreferredSize(new Dimension(1920, 1080));
    splitPane.setResizeWeight(0.25);
    contentPane.add(splitPane, "grow, push, wrap");
    contentPane.add(new Footer(exportManager), "growx");
    return contentPane;
  }
}
