/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export;

import com.github.aleksikangas.qct.convert.ConvertFormatOptions;
import com.jgoodies.validation.ValidationResult;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public abstract class AbstractOptionsPanel extends JPanel {
  protected AbstractOptionsPanel() {
    setLayout(new MigLayout("insets 0, fillx, gapy 4", "[right][grow,fill]", ""));
  }

  public abstract ConvertFormatOptions getConvertOptions();

  public abstract ValidationResult validateOptions();
}
