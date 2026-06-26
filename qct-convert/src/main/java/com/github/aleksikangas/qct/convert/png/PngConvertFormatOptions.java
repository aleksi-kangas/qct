/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.convert.png;

import com.github.aleksikangas.qct.convert.ConvertFormatOptions;
import com.github.aleksikangas.qct.core.interpolation.Interpolator;

import javax.annotation.Nullable;

public record PngConvertFormatOptions(GeoreferencingMode georeferencingMode,
                                      @Nullable Interpolator.DownscaleFactor downscaleFactor)
        implements ConvertFormatOptions {
}
