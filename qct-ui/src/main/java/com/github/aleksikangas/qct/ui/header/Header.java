/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.header;

import com.github.aleksikangas.qct.ui.action.BrowseQctFileAction;
import com.github.aleksikangas.qct.ui.action.ExportQctFileAction;
import com.github.aleksikangas.qct.ui.model.QctModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Objects;

public final class Header extends JPanel {
  private final JTextField fileTextField = new JTextField();

  public Header(final QctModel qctModel) {
    super(new MigLayout("insets 0", "[][grow][][]", "[fill]"));
    final JButton browseButton = new JButton(new BrowseQctFileAction(qctModel::setQctFilePath, this));
    final JButton exportButton = new JButton(new ExportQctFileAction(qctModel, this));

    add(new JLabel("File:"));
    add(fileTextField, "growx");
    add(browseButton);
    add(exportButton);

    qctModel.addPropertyChangeListener(e -> {
      if (Objects.equals(e.getPropertyName(), QctModel.QCT_FILE_PATH)) {
        fileTextField.setText(e.getNewValue().toString());
      }
    });
  }
}
