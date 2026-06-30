/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.meta.builder;

import com.github.aleksikangas.qct.core.meta.*;
import com.google.common.base.Preconditions;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * @see Metadata
 */
public final class MetadataBuilder {
  private MagicNumber magicNumber = MagicNumber.QUICK_CHART_MAP;
  private FileFormatVersion fileFormatVersion = FileFormatVersion.QUICK_CHART;
  private int widthTiles = 0;
  private int heightTiles = 0;
  private String longTitle = "";
  private String name = "";
  private String identifier = "";
  private String edition = "";
  private String revision = "";
  private String keywords = "";
  private String copyright = "";
  private String scale = "";
  private String datum = "";
  private String depths = "";
  private String heights = "";
  private String projection = "";
  private Set<Flag> flags = EnumSet.noneOf(Flag.class);
  private String originalFileName = "";
  private int originalFileSize = 0;
  private Instant originalFileCreationTime = Instant.now();
  private ExtendedData extendedData = null;
  private MapOutline mapOutline = null;

  public MetadataBuilder withMagicNumber(final MagicNumber magicNumber) {
    this.magicNumber = Objects.requireNonNull(magicNumber);
    return this;
  }

  public MetadataBuilder withFileFormatVersion(final FileFormatVersion fileFormatVersion) {
    this.fileFormatVersion = Objects.requireNonNull(fileFormatVersion);
    return this;
  }

  public MetadataBuilder withWidthTiles(final int widthTiles) {
    Preconditions.checkArgument(widthTiles >= 0, "widthTiles must be non-negative");
    this.widthTiles = widthTiles;
    return this;
  }

  public MetadataBuilder withHeightTiles(final int heightTiles) {
    Preconditions.checkArgument(heightTiles >= 0, "heightTiles must be non-negative");
    this.heightTiles = heightTiles;
    return this;
  }

  public MetadataBuilder withLongTitle(final String longTitle) {
    this.longTitle = Objects.requireNonNull(longTitle);
    return this;
  }

  public MetadataBuilder withName(final String name) {
    this.name = Objects.requireNonNull(name);
    return this;
  }

  public MetadataBuilder withIdentifier(final String identifier) {
    this.identifier = Objects.requireNonNull(identifier);
    return this;
  }

  public MetadataBuilder withEdition(final String edition) {
    this.edition = Objects.requireNonNull(edition);
    return this;
  }

  public MetadataBuilder withRevision(final String revision) {
    this.revision = Objects.requireNonNull(revision);
    return this;
  }

  public MetadataBuilder withKeywords(final String keywords) {
    this.keywords = Objects.requireNonNull(keywords);
    return this;
  }

  public MetadataBuilder withCopyright(final String copyright) {
    this.copyright = Objects.requireNonNull(copyright);
    return this;
  }

  public MetadataBuilder withScale(final String scale) {
    this.scale = Objects.requireNonNull(scale);
    return this;
  }

  public MetadataBuilder withDatum(final String datum) {
    this.datum = Objects.requireNonNull(datum);
    return this;
  }

  public MetadataBuilder withDepths(final String depths) {
    this.depths = Objects.requireNonNull(depths);
    return this;
  }

  public MetadataBuilder withHeights(final String heights) {
    this.heights = Objects.requireNonNull(heights);
    return this;
  }

  public MetadataBuilder withProjection(final String projection) {
    this.projection = Objects.requireNonNull(projection);
    return this;
  }

  public MetadataBuilder withFlags(final Set<Flag> flags) {
    this.flags = Objects.requireNonNull(flags);
    return this;
  }

  public MetadataBuilder withOriginalFileName(final String originalFileName) {
    this.originalFileName = Objects.requireNonNull(originalFileName);
    return this;
  }

  public MetadataBuilder withOriginalFileSize(final int originalFileSize) {
    Preconditions.checkArgument(originalFileSize >= 0, "originalFileSize must be non-negative");
    this.originalFileSize = originalFileSize;
    return this;
  }

  public MetadataBuilder withOriginalFileCreationTime(final Instant originalFileCreationTime) {
    this.originalFileCreationTime = Objects.requireNonNull(originalFileCreationTime);
    return this;
  }

  public MetadataBuilder withExtendedData(final ExtendedData extendedData) {
    this.extendedData = Objects.requireNonNull(extendedData);
    return this;
  }

  public MetadataBuilder withMapOutline(final MapOutline mapOutline) {
    this.mapOutline = Objects.requireNonNull(mapOutline);
    return this;
  }


  public Metadata build() {
    Preconditions.checkState(extendedData != null, "extendedData may not be null");
    Preconditions.checkState(mapOutline != null, "mapOutline may not be null");
    return new Metadata(magicNumber,
                        fileFormatVersion,
                        widthTiles,
                        heightTiles,
                        longTitle,
                        name,
                        identifier,
                        edition,
                        revision,
                        keywords,
                        copyright,
                        scale,
                        datum,
                        depths,
                        heights,
                        projection,
                        flags,
                        originalFileName,
                        originalFileSize,
                        originalFileCreationTime,
                        extendedData,
                        mapOutline);
  }
}
