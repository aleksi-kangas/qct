/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.meta;

import com.github.aleksikangas.qct.core.QctFile;
import com.github.aleksikangas.qct.core.meta.*;
import com.github.aleksikangas.qct.ui.model.QctModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Objects;

public final class MetadataPanel extends JPanel {
  private final JTextField magicNumberField = new JTextField();
  private final JTextField fileFormatVersionField = new JTextField();
  private final JTextField widthField = new JTextField();
  private final JTextField heightField = new JTextField();
  private final JTextField longTitleField = new JTextField();
  private final JTextField nameField = new JTextField();
  private final JTextField identifierField = new JTextField();
  private final JTextField editionField = new JTextField();
  private final JTextField revisionField = new JTextField();
  private final JTextField keywordsField = new JTextField();
  private final JTextField copyrightField = new JTextField();
  private final JTextField scaleField = new JTextField();
  private final JTextField datumField = new JTextField();
  private final JTextField depthsField = new JTextField();
  private final JTextField heightsField = new JTextField();
  private final JTextField projectionField = new JTextField();
  private final JTextField flagsField = new JTextField();
  private final JTextField originalFileNameField = new JTextField();
  private final JTextField originalFileSizeField = new JTextField();
  private final JTextField originalFileCreationTimeField = new JTextField();

  // --- Extended Data ---
  private final JTextField mapTypeField = new JTextField();
  private final DatumShiftPanel datumShiftPanel = new DatumShiftPanel();
  private final JTextField diskNameField = new JTextField();
  private final LicenseInformationPanel licenseInformationPanel = new LicenseInformationPanel();
  private final JTextField associatedDataField = new JTextField();
  private final DigitalMapShopPanel digitalMapShopPanel = new DigitalMapShopPanel();
  private final MapOutlinePanel mapOutlinePanel = new MapOutlinePanel();
  // --- Extended Data ---

  public MetadataPanel(final QctModel qctModel) {
    super(new MigLayout("fill, insets 4 10 4 10, gap 10", "[][fill, grow]", ""));
    setBorder(BorderFactory.createTitledBorder("Metadata"));

    magicNumberField.setEnabled(false);
    fileFormatVersionField.setEnabled(false);
    widthField.setEnabled(false);
    heightField.setEnabled(false);
    longTitleField.setEnabled(false);
    nameField.setEnabled(false);
    identifierField.setEnabled(false);
    editionField.setEnabled(false);
    revisionField.setEnabled(false);
    keywordsField.setEnabled(false);
    copyrightField.setEnabled(false);
    scaleField.setEnabled(false);
    datumField.setEnabled(false);
    depthsField.setEnabled(false);
    heightsField.setEnabled(false);
    projectionField.setEnabled(false);
    flagsField.setEnabled(false);
    originalFileNameField.setEnabled(false);
    originalFileSizeField.setEnabled(false);
    originalFileCreationTimeField.setEnabled(false);
    mapTypeField.setEnabled(false);
    diskNameField.setEnabled(false);
    associatedDataField.setEnabled(false);

    add(new JLabel("Magic Number:"));
    add(magicNumberField, "wrap");

    add(new JLabel("File Format Version:"));
    add(fileFormatVersionField, "wrap");

    add(new JLabel("Width (tiles / pixels):"));
    add(widthField, "wrap");

    add(new JLabel("Height (tiles / pixels):"));
    add(heightField, "wrap");

    add(new JLabel("Long Title:"));
    add(longTitleField, "wrap");

    add(new JLabel("Name:"));
    add(nameField, "wrap");

    add(new JLabel("Identifier:"));
    add(identifierField, "wrap");

    add(new JLabel("Edition:"));
    add(editionField, "wrap");

    add(new JLabel("Revision:"));
    add(revisionField, "wrap");

    add(new JLabel("Keywords:"));
    add(keywordsField, "wrap");

    add(new JLabel("Copyright:"));
    add(copyrightField, "wrap");

    add(new JLabel("Scale:"));
    add(scaleField, "wrap");

    add(new JLabel("Datum:"));
    add(datumField, "wrap");

    add(new JLabel("Depths:"));
    add(depthsField, "wrap");

    add(new JLabel("Heights:"));
    add(heightsField, "wrap");

    add(new JLabel("Projection:"));
    add(projectionField, "wrap");

    add(new JLabel("Flags:"));
    add(flagsField, "wrap");

    add(new JLabel("Original File Name:"));
    add(originalFileNameField, "wrap");

    add(new JLabel("Original File Size:"));
    add(originalFileSizeField, "wrap");

    add(new JLabel("Original File Creation Time:"));
    add(originalFileCreationTimeField, "wrap");

    add(new JLabel("Map Type:"));
    add(mapTypeField, "wrap");

    add(new JLabel("Datum Shift:"));
    add(datumShiftPanel, "wrap");

    add(new JLabel("Disk Name:"));
    add(diskNameField, "wrap");

    add(new JLabel("License Information"));
    add(licenseInformationPanel, "wrap");

    add(new JLabel("Associated Data:"));
    add(associatedDataField, "wrap");

    add(new JLabel("Digital Map Shop:"));
    add(digitalMapShopPanel, "wrap");

    add(new JLabel("Map Outline:"));
    add(mapOutlinePanel, "wrap");

    qctModel.addPropertyChangeListener(e -> {
      if (Objects.equals(e.getPropertyName(), QctModel.QCT_FILE)) {
        final QctFile qctFile = (QctFile) e.getNewValue();
        if (qctFile != null) {
          onMetadata(qctFile.metadata());
        } else {
          clear();
        }
      }
    });
  }

  private void onMetadata(final Metadata metadata) {
    magicNumberField.setText(metadata.magicNumber().toString());
    fileFormatVersionField.setText(metadata.fileFormatVersion().toString());
    widthField.setText(String.format("%d / %d", metadata.widthTiles(), metadata.widthPixels()));
    heightField.setText(String.format("%d / %d", metadata.heightTiles(), metadata.heightPixels()));
    longTitleField.setText(metadata.longTitle());
    nameField.setText(metadata.name());
    identifierField.setText(metadata.identifier());
    editionField.setText(metadata.edition());
    revisionField.setText(metadata.revision());
    keywordsField.setText(metadata.keywords());
    copyrightField.setText(metadata.copyright());
    scaleField.setText(metadata.scale());
    datumField.setText(metadata.datum());
    depthsField.setText(metadata.depths());
    heightsField.setText(metadata.heights());
    projectionField.setText(metadata.projection());
    flagsField.setText(metadata.flags().toString());
    originalFileNameField.setText(metadata.originalFileName());
    originalFileSizeField.setText(String.valueOf(metadata.originalFileSize()));
    originalFileSizeField.setToolTipText(String.format("KB: %d%n0MB: %d%nGB: %.2f",
                                                       metadata.originalFileSize() / 1000,
                                                       metadata.originalFileSize() / 1000 / 1000,
                                                       metadata.originalFileSize() / 1000.0 / 1000.0 / 1000.0));
    originalFileCreationTimeField.setText(metadata.originalFileCreationTime().toString());
    mapTypeField.setText(metadata.extendedData().mapType());
    datumShiftPanel.onMetadata(metadata);
    diskNameField.setText(metadata.extendedData().diskName());
    licenseInformationPanel.onMetadata(metadata);
    associatedDataField.setText(metadata.extendedData().associatedData());
    digitalMapShopPanel.onMetadata(metadata);
    mapOutlinePanel.onMetadata(metadata);
  }

  private void clear() {
    magicNumberField.setText("");
    fileFormatVersionField.setText("");
    widthField.setText("");
    heightsField.setText("");
    longTitleField.setText("");
    nameField.setText("");
    identifierField.setText("");
    editionField.setText("");
    revisionField.setText("");
    keywordsField.setText("");
    copyrightField.setText("");
    scaleField.setText("");
    datumField.setText("");
    depthsField.setText("");
    heightsField.setText("");
    projectionField.setText("");
    flagsField.setText("");
    originalFileNameField.setText("");
    originalFileSizeField.setText("");
    originalFileCreationTimeField.setText("");
    mapTypeField.setText("");
    datumShiftPanel.clear();
    diskNameField.setText("");
    licenseInformationPanel.clear();
    associatedDataField.setText("");
    digitalMapShopPanel.clear();
    mapOutlinePanel.clear();
  }

  private static final class DatumShiftPanel extends JPanel {
    private final JTextField northField = new JTextField();
    private final JTextField eastField = new JTextField();

    private DatumShiftPanel() {
      super(new MigLayout("insets 0", "[][fill, grow]", "[][]"));

      northField.setEnabled(false);
      eastField.setEnabled(false);

      add(new JLabel("North:"));
      add(northField, "wrap");

      add(new JLabel("East:"));
      add(eastField, "wrap");
    }

    private void onMetadata(final Metadata metadata) {
      final DatumShift datumShift = metadata.extendedData().datumShift();
      northField.setText(String.valueOf(datumShift.north()));
      eastField.setText(String.valueOf(datumShift.east()));
    }

    private void clear() {
      northField.setText("");
      eastField.setText("");
    }
  }

  private static final class LicenseInformationPanel extends JPanel {
    private final JTextField identifierField = new JTextField();
    private final JTextField serialNumberField = new JTextField();

    public LicenseInformationPanel() {
      super(new MigLayout("insets 0", "[][fill, grow]", "[][][]"));

      identifierField.setEnabled(false);
      serialNumberField.setEnabled(false);

      add(new JLabel("Identifier:"));
      add(identifierField, "wrap");

      add(new JLabel("Serial Number:"));
      add(serialNumberField, "wrap");
    }

    private void onMetadata(final Metadata metadata) {
      final LicenseInformation licenseInformation = metadata.extendedData().licenseInformation();
      identifierField.setText(String.valueOf(licenseInformation.identifier()));
      serialNumberField.setText(licenseInformation.serialNumber().toString());
    }

    private void clear() {
      identifierField.setText("");
      serialNumberField.setText("");
    }
  }

  private static final class DigitalMapShopPanel extends JPanel {
    private final JTextField sizeField = new JTextField();
    private final JTextField qc3UrlField = new JTextField();


    public DigitalMapShopPanel() {
      super(new MigLayout("insets 0", "[][fill, grow]", "[][]"));

      sizeField.setEnabled(false);
      qc3UrlField.setEnabled(false);

      add(new JLabel("Size:"));
      add(sizeField, "wrap");

      add(new JLabel("QC3 URL:"));
      add(qc3UrlField, "wrap");
    }

    private void onMetadata(final Metadata metadata) {
      final DigitalMapShop digitalMapShop = metadata.extendedData().digitalMapShop();
      sizeField.setText(String.valueOf(digitalMapShop.size()));
      qc3UrlField.setText(digitalMapShop.qc3Url());
    }

    private void clear() {
      sizeField.setText("");
      qc3UrlField.setText("");
    }
  }

  private static final class MapOutlinePanel extends JPanel {
    private MapOutlinePanel() {
      super(new MigLayout("insets 0", "[][fill, grow]", ""));
    }

    private void onMetadata(final Metadata metadata) {
      for (final MapOutline.Point point : metadata.mapOutline().points()) {
        final var pointLabel = new JLabel("Lat / Lon (°):");

        final JTextField pointField = new JTextField(point.latitude() + " / " + point.longitude());
        pointField.setEnabled(false);

        add(pointLabel);
        add(pointField, "wrap");
      }
      revalidate();
    }

    private void clear() {
      removeAll();
      revalidate();
    }
  }
}
