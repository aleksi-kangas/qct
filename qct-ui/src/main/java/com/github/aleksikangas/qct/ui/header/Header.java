/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.header;

import com.github.aleksikangas.qct.ui.export.ExportManager;
import com.github.aleksikangas.qct.ui.file.QctFileManager;
import com.github.aleksikangas.qct.ui.header.action.BrowseQctFileAction;
import com.github.aleksikangas.qct.ui.header.action.ExportQctFileAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Objects;

public final class Header extends JPanel {
  private final JTextField fileTextField = new JTextField();

  private final transient QctFileManager qctFileManager;
  private final JButton exportButton;

  public Header(final ExportManager exportManager, final QctFileManager qctFileManager) {
    super(new MigLayout("insets 0", "[][grow][][]", "[fill]"));
    this.qctFileManager = Objects.requireNonNull(qctFileManager);
    exportButton = new JButton(new ExportQctFileAction(exportManager, qctFileManager, this));
    fileTextField.setEditable(false);

    add(new JLabel("File:"));
    add(fileTextField, "growx");
    add(new JButton(new BrowseQctFileAction(this::onBrowseButtonClick, this)));
    add(exportButton);
  }

  private void onBrowseButtonClick(final Path qctFilePath) {
    qctFileManager.setSelectedQctFile(qctFilePath);
    fileTextField.setText(qctFilePath.toString());
    exportButton.setEnabled(true);
  }
}
