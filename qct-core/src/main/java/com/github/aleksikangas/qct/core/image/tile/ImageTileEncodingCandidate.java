/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile;

import com.github.aleksikangas.qct.core.image.tile.ImageTile.Encoding;
import com.github.aleksikangas.qct.core.utils.QctWriter;

public interface ImageTileEncodingCandidate {
  Encoding encoding();

  int sizeBytes();

  void encode(QctWriter qctWriter, int byteOffset);
}
