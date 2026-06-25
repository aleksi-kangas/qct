/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.toolbar;

import com.github.aleksikangas.qct.ui.QctModel;
import com.github.aleksikangas.qct.ui.action.BrowseQctFileAction;
import com.github.aleksikangas.qct.ui.action.ConvertQctFileAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Objects;

public final class Toolbar extends JPanel {
  private final JTextField fileTextField = new JTextField();

  public Toolbar(final QctModel qctModel) {
    super(new MigLayout("insets 0", "[][grow][][]", "[fill]"));
    final JButton browseButton = new JButton(new BrowseQctFileAction(qctModel::onQctFilePath, this));
    final JButton convertButton = new JButton(new ConvertQctFileAction(qctModel, this));

    add(new JLabel("File:"));
    add(fileTextField, "growx");
    add(browseButton);
    add(convertButton);

    qctModel.addPropertyChangeListener(e -> {
      if (Objects.equals(e.getPropertyName(), QctModel.QCT_FILE_PATH)) {
        fileTextField.setText(e.getNewValue().toString());
      }
    });
  }
}
