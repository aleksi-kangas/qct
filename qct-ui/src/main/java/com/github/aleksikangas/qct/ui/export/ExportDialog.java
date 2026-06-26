/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export;

import com.github.aleksikangas.qct.convert.ConvertFormatOptions;
import com.github.aleksikangas.qct.convert.png.PngConvertFormatOptions;
import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.ui.export.action.BrowseExportPathAction;
import com.github.aleksikangas.qct.ui.export.png.PngExportOptionsPanel;
import com.github.aleksikangas.qct.ui.export.png.PngExportTask;
import com.github.aleksikangas.qct.ui.export.task.ExportTask;
import com.github.aleksikangas.qct.ui.file.QctFileManager;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.Validator;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import net.miginfocom.swing.MigLayout;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
  private final JPanel optionsPanelHolder = new JPanel(new BorderLayout());
  private final JButton exportButton = new JButton("Export");

  private final transient ExportManager exportManager;
  private final transient QctFileManager qctFileManager;
  private final transient QctFile qctFile;

  @Nullable
  private AbstractOptionsPanel optionsPanel = null;

  private ExportDialog(final ExportManager exportManager,
                       final QctFileManager qctFileManager,
                       @Nullable final Window owner) {
    super(owner, "Convert", ModalityType.APPLICATION_MODAL);
    this.exportManager = Objects.requireNonNull(exportManager);
    this.qctFileManager = Objects.requireNonNull(qctFileManager);
    this.qctFile = qctFileManager.getQctFile().orElseThrow();
    setLayout(new MigLayout("fillx, insets dialog", "[][fill, grow]", ""));

    add(new JLabel("Format:"));
    add(formatComboBox, "growx, wrap");
    formatComboBox.addActionListener(_ -> {
      displayFormatOptions();
      setDefaultExportPath();
    });
    formatComboBox.setSelectedItem(Arrays.stream(ExportFormat.values()).findFirst().orElseThrow());

    add(new JLabel("Output:"));
    add(createOutputPanel(), "growx, wrap");

    add(optionsPanelHolder, "span 2, growx, wrap");

    add(createButtonPanel(), "span 2, right");

    pack();
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setMinimumSize(getPreferredSize());
    setLocationRelativeTo(owner);
  }

  public static void showDialog(final ExportManager exportManager,
                                final QctFileManager qctFileManager,
                                @Nullable final Component parent) {
    final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
    final var dialog = new ExportDialog(exportManager, qctFileManager, owner);
    dialog.setVisible(true);
  }

  private JPanel createOutputPanel() {
    exportPathTextField.setEditable(false);
    exportPathTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      @Override
      public void insertUpdate(javax.swing.event.DocumentEvent e) {
        validateDialog();
      }

      @Override
      public void removeUpdate(javax.swing.event.DocumentEvent e) {
        validateDialog();
      }

      @Override
      public void changedUpdate(javax.swing.event.DocumentEvent e) {
        validateDialog();
      }
    });
    setDefaultExportPath();
    final var outputPanel = new JPanel(new MigLayout("insets 0", "[grow,fill][]", ""));
    outputPanel.add(exportPathTextField);
    outputPanel.add(new JButton(new BrowseExportPathAction(this::getExportPath,
                                                           this::getExportFormatFileNameExtensionFilter,
                                                           exportPath -> exportPathTextField.setText(exportPath.toString()),
                                                           this)));
    return outputPanel;
  }

  private JPanel createButtonPanel() {
    final var panel = new JPanel(new MigLayout("insets 0", "[]", ""));
    exportButton.addActionListener(_ -> onExport());
    panel.add(exportButton);
    return panel;
  }

  private void validateDialog() {
    final ValidationResult validationResult = convertDialogValidator().validate(this);
    validationResultModel.setResult(validationResult);
    final boolean isValid = !validationResult.hasErrors();
    exportButton.setEnabled(isValid);
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

  private String getExportFormatExtension() {
    return switch (getExportFormat()) {
      case PNG -> ".png";
    };
  }

  private FileNameExtensionFilter getExportFormatFileNameExtensionFilter() {
    return switch (getExportFormat()) {
      case PNG -> new javax.swing.filechooser.FileNameExtensionFilter("PNG image (*.png)", "png");
    };
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

  private void setDefaultExportPath() {
    final Path qctFilePath = qctFileManager.getQctFilePath();
    if (qctFilePath.getFileName() == null) return;
    final String exportFormatExtension = getExportFormatExtension();
    String fileName = qctFilePath.getFileName().toString();
    final int dotIndex = fileName.lastIndexOf('.');
    fileName = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    final Path parent = qctFilePath.getParent() != null ? qctFilePath.getParent() : Path.of(".");
    final Path outputPath = parent.resolve(fileName + exportFormatExtension);
    exportPathTextField.setText(outputPath.toString());
  }

  private void displayFormatOptions() {
    optionsPanelHolder.removeAll();
    optionsPanel = null;

    optionsPanel = switch (getExportFormat()) {
      case PNG -> new PngExportOptionsPanel();
    };

    optionsPanelHolder.add(optionsPanel, BorderLayout.CENTER);
    optionsPanelHolder.revalidate();
    optionsPanelHolder.repaint();
    pack();
  }

  private void onExport() {
    final ExportFormat exportFormat = getExportFormat();
    final Path exportPath = getExportPath().orElseThrow();
    final ConvertFormatOptions convertFormatOptions = optionsPanel != null ? optionsPanel.getConvertOptions() : null;
    final ExportTask exportTask = switch (exportFormat) {
      case PNG -> new PngExportTask(exportPath, qctFile, (PngConvertFormatOptions) convertFormatOptions);
    };
    exportManager.export(exportTask);
    dispose();
  }
}