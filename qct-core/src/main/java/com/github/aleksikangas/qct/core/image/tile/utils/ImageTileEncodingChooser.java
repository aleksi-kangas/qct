/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.utils;

import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.ImageTileEncodingCandidate;
import com.github.aleksikangas.qct.core.image.tile.huffman.HuffmanEncoder;
import com.github.aleksikangas.qct.core.image.tile.rle.RleEncoder;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class ImageTileEncodingChooser {
  /**
   * Choose the best encoding algorithm for an {@link ImageTile}, where best = smallest compressed size in bytes.
   *
   * @param imageTile to encode
   * @return the best {@link ImageTileEncodingCandidate}
   */
  public static ImageTileEncodingCandidate chooseEncoding(final ImageTile imageTile) {
    final Set<ImageTileEncodingCandidate> candidates = new HashSet<>();
    final List<CompletableFuture<Void>> candidatesFutures = new ArrayList<>();
    candidatesFutures.add(CompletableFuture.runAsync(() -> candidates.add(HuffmanEncoder.Candidate.of(imageTile))));
    candidatesFutures.add(CompletableFuture.runAsync(() -> candidates.add(RleEncoder.Candidate.of(imageTile))));
    candidatesFutures.forEach(CompletableFuture::join);
    return candidates.stream().min(Comparator.comparingInt(ImageTileEncodingCandidate::sizeBytes)).orElseThrow();
  }

  private ImageTileEncodingChooser() {
  }
}
