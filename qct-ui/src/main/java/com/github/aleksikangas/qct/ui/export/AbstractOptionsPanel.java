/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export;

import com.github.aleksikangas.qct.convert.ConvertFormatOptions;
import com.jgoodies.validation.ValidationResult;

import javax.swing.*;

public abstract class AbstractOptionsPanel extends JPanel {
  public abstract ConvertFormatOptions getConvertOptions();

  public abstract ValidationResult validateOptions();
}
