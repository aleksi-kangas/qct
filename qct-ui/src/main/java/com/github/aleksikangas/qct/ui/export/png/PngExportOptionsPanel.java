/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export.png;

import com.github.aleksikangas.qct.convert.png.GeoreferencingMode;
import com.github.aleksikangas.qct.convert.png.PngConvertFormatOptions;
import com.github.aleksikangas.qct.core.interpolation.Interpolator;
import com.github.aleksikangas.qct.ui.export.AbstractOptionsPanel;
import com.jgoodies.validation.ValidationResult;

import javax.swing.*;
import java.util.Optional;

public final class PngExportOptionsPanel extends AbstractOptionsPanel {
  private final JComboBox<GeoreferencingMode> georeferencingModeComboBox = new JComboBox<>(GeoreferencingMode.values());
  private final JCheckBox downscaleCheckBox = new JCheckBox("Downscale");

  private final JComboBox<Interpolator.DownscaleFactor> downscaleFactorComboBox = new JComboBox<>(Interpolator.DownscaleFactor.values());

  public PngExportOptionsPanel() {
    add(new JLabel("Georeferencing:"));
    add(georeferencingModeComboBox, "growx, wrap");

    add(downscaleCheckBox);
    downscaleCheckBox.addActionListener(_ -> onDownscaleCheckBoxClick());
    add(downscaleFactorComboBox, "growx, wrap");

    onDownscaleCheckBoxClick();
  }

  @Override
  public PngConvertFormatOptions getConvertOptions() {
    return new PngConvertFormatOptions((GeoreferencingMode) georeferencingModeComboBox.getSelectedItem(),
                                       getDownscaleFactor().orElse(null));
  }

  @Override
  public ValidationResult validateOptions() {
    final ValidationResult result = new ValidationResult();

    if (georeferencingModeComboBox.getSelectedItem() == null) {
      result.addError("Georeferencing mode must be selected");
    }

    if (downscaleCheckBox.isSelected() && downscaleFactorComboBox.getSelectedItem() == null) {
      result.addError("Downscale factor must be selected");
    }

    return result;
  }

  private void onDownscaleCheckBoxClick() {
    downscaleFactorComboBox.setVisible(downscaleCheckBox.isSelected());
    revalidate();
    repaint();
  }

  private Optional<Interpolator.DownscaleFactor> getDownscaleFactor() {
    return downscaleCheckBox.isSelected()
           ? Optional.ofNullable((Interpolator.DownscaleFactor) downscaleFactorComboBox.getSelectedItem())
           : Optional.empty();
  }
}
