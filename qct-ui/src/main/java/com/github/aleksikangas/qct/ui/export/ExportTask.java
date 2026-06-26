/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.export;

import com.github.aleksikangas.qct.convert.ConvertFormatOptions;

import javax.annotation.Nullable;
import java.nio.file.Path;

public record ExportTask(ExportFormat exportFormat,
                         Path exportPath,
                         @Nullable ConvertFormatOptions convertFormatOptions) {
}
