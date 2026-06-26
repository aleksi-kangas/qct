/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export;

import com.github.aleksikangas.qct.convert.ConvertFormatOptions;
import com.github.aleksikangas.qct.ui.async.ExportQctFileWorker;
import com.github.aleksikangas.qct.ui.export.png.PngExportOptionsPanel;
import com.github.aleksikangas.qct.ui.model.QctModel;
import com.google.common.base.Preconditions;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.Validator;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import net.miginfocom.swing.MigLayout;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class ExportDialog extends JDialog {
  private final ValidationResultModel validationResultModel = new DefaultValidationResultModel();
  private final JComboBox<ExportFormat> formatComboBox = new JComboBox<>(ExportFormat.values());
  private final JTextField exportPathTextField = new JTextField();
  private final JPanel optionsContainerPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
  private final transient QctModel qctModel;
  @Nullable
  private AbstractOptionsPanel optionsPanel = null;

  private ExportDialog(final QctModel qctModel, @Nullable final Window owner) {
    super(owner, "Convert", ModalityType.APPLICATION_MODAL);
    this.qctModel = Objects.requireNonNull(qctModel);
    setLayout(new MigLayout("insets dialog", "[][grow]", ""));

    add(new JLabel("Format:"));
    add(formatComboBox, "wrap");
    formatComboBox.addActionListener(_ -> displayFormatOptions());
    formatComboBox.setSelectedItem(Arrays.stream(ExportFormat.values()).findFirst().orElseThrow());

    add(new JLabel("Output:"));
    add(createOutputPanel(), "wrap");

    add(optionsContainerPanel, "span 2, wrap");

    add(createButtonPanel(), "span 2, right");

    pack();
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setMinimumSize(getPreferredSize());
    setLocationRelativeTo(owner);
  }

  public static void showDialog(final QctModel qctModel, @Nullable final Component parent) {
    final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
    final var dialog = new ExportDialog(qctModel, owner);
    dialog.setVisible(true);
  }

  private JPanel createOutputPanel() {
    final var outputPanel = new JPanel(new MigLayout("insets 0", "[grow,fill][]", ""));
    outputPanel.add(exportPathTextField);
    outputPanel.add(createBrowseButton());
    return outputPanel;
  }

  private JButton createBrowseButton() {
    final JButton button = new JButton("Browse...");
    button.addActionListener(e -> {
      final var fileChooser = new JFileChooser();
      if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        exportPathTextField.setText(fileChooser.getSelectedFile().toPath().toString());
      }
    });
    return button;
  }

  private JPanel createButtonPanel() {
    final var panel = new JPanel(new MigLayout("ins 0", "[]", ""));
    final var exportButton = new JButton("Export");
    exportButton.addActionListener(e -> onExport());
    panel.add(exportButton);
    return panel;
  }

  private boolean validateDialog() {
    final ValidationResult validationResult = convertDialogValidator().validate(this);
    validationResultModel.setResult(validationResult);
    return !validationResult.hasErrors();
  }

  private Validator<ExportDialog> convertDialogValidator() {
    return _ -> {
      final var validationResult = new ValidationResult();
      if (getExportPath().isEmpty()) {
        validationResult.addError("Output path must be valid");
      }
      if (optionsPanel != null) {
        validationResult.addAllFrom(optionsPanel.validateOptions());
      }
      return validationResult;
    };
  }

  private ExportFormat getExportFormat() {
    return Objects.requireNonNull((ExportFormat) formatComboBox.getSelectedItem());
  }

  private Optional<Path> getExportPath() {
    final String exportPathString = exportPathTextField.getText().trim();
    if (exportPathString.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.of(Path.of(exportPathString));
    } catch (final InvalidPathException _) {
      return Optional.empty();
    }
  }

  private void displayFormatOptions() {
    optionsContainerPanel.removeAll();
    optionsPanel = null;

    optionsPanel = switch (getExportFormat()) {
      case PNG -> new PngExportOptionsPanel();
    };

    optionsContainerPanel.add(optionsPanel);
    optionsContainerPanel.revalidate();
    optionsContainerPanel.repaint();
    pack();
  }

  private void onExport() {
    Preconditions.checkState(validateDialog());
    final ExportFormat exportFormat = getExportFormat();
    final Path exportPath = getExportPath().orElseThrow();
    final ConvertFormatOptions convertFormatOptions = optionsPanel != null ? optionsPanel.getConvertOptions() : null;
    new ExportQctFileWorker(new ExportTask(exportFormat, exportPath, convertFormatOptions),
                            qctModel.getQctFile().orElseThrow()).execute();
    dispose();
  }
}