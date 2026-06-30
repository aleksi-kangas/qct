/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.builder;

import com.github.aleksikangas.qct.core.meta.*;

import java.util.Objects;

public final class ExtendedDataBuilder {
  private String mapType = "";
  private DatumShift datumShift = new DatumShift(0, 0);
  private String diskName = "";
  private LicenseInformation licenseInformation = new LicenseInformation(0, new SerialNumber(new int[0]));
  private String associatedData = "";
  private DigitalMapShop digitalMapShop = new DigitalMapShop(8, "");

  public ExtendedDataBuilder withMapType(final String mapType) {
    this.mapType = Objects.requireNonNull(mapType);
    return this;
  }

  public ExtendedDataBuilder withDatumShift(final DatumShift datumShift) {
    this.datumShift = Objects.requireNonNull(datumShift);
    return this;
  }

  public ExtendedDataBuilder withDiskName(final String diskName) {
    this.diskName = Objects.requireNonNull(diskName);
    return this;
  }

  public ExtendedDataBuilder withLicenseInformation(final LicenseInformation licenseInformation) {
    this.licenseInformation = Objects.requireNonNull(licenseInformation);
    return this;
  }

  public ExtendedDataBuilder withAssociatedData(final String associatedData) {
    this.associatedData = Objects.requireNonNull(associatedData);
    return this;
  }

  public ExtendedDataBuilder withDigitalMapShop(final DigitalMapShop digitalMapShop) {
    this.digitalMapShop = Objects.requireNonNull(digitalMapShop);
    return this;
  }

  public ExtendedData build() {
    return new ExtendedData(mapType, datumShift, diskName, licenseInformation, associatedData, digitalMapShop);
  }
}
