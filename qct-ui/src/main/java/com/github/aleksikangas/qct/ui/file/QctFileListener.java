/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.ui.file;

import com.github.aleksikangas.qct.core.QctFile;

import javax.annotation.Nullable;

public interface QctFileListener {
  /**
   * @implNote Not necessarily invoked on EDT.
   */
  void onQctFile(@Nullable QctFile qctFile);
}
