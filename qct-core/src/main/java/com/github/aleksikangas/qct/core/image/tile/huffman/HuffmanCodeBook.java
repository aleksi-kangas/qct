/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.huffman;


import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.utils.QctByteStream;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A Huffman Codebook which resembles a Huffman tree.
 */
public final class HuffmanCodeBook {
  private final HuffmanTreeNode root;

  HuffmanCodeBook(final HuffmanTreeNode root) {
    this.root = Objects.requireNonNull(root);
  }

  HuffmanTreeNode getRoot() {
    return root;
  }

  boolean isSingleColor() {
    return root instanceof HuffmanTreeNode.LeafNode;
  }

  int getSinglePaletteIndex() {
    Preconditions.checkState(isSingleColor(), "Not a single-color codebook");
    return ((HuffmanTreeNode.LeafNode) root).paletteIndex();
  }

  List<Integer> toBytes() {
    return encodeTreeRecursive(root);
  }

  public static HuffmanCodeBook from(final QctByteStream qctByteStream) {
    final HuffmanTreeNode root = decodeTreeRecursive(qctByteStream);
    return new HuffmanCodeBook(root);
  }

  private static HuffmanTreeNode decodeTreeRecursive(final QctByteStream stream) {
    final int currentByte = stream.nextByte();
    if (currentByte < 128) {  // Leaf
      Objects.checkIndex(currentByte, Palette.SIZE);
      return new HuffmanTreeNode.LeafNode(0, currentByte);
    }
    final HuffmanTreeNode left;
    final HuffmanTreeNode right;
    if (currentByte == 128) {  // Far branch (skip the two offset bytes)
      stream.skip(2);
    }
    left = decodeTreeRecursive(stream);
    right = decodeTreeRecursive(stream);
    return new HuffmanTreeNode.ParentNode(0, left, right);
  }

  private static List<Integer> encodeTreeRecursive(final HuffmanTreeNode node) {
    return switch (node) {
      case HuffmanTreeNode.ParentNode parentNode -> {
        final List<Integer> bytes = new ArrayList<>();
        final List<Integer> leftBytes = encodeTreeRecursive(parentNode.left());
        final List<Integer> rightBytes = encodeTreeRecursive(parentNode.right());
        final int leftSize = leftBytes.size();
        final int jumpAfterHeader = 1 + leftSize;   // 1 for the branch byte itself
        if (jumpAfterHeader <= 127) {  // Near branch
          final int branchByte = 257 - jumpAfterHeader;
          bytes.add(branchByte);
        } else {  // Far branch
          bytes.add(128);
          final int offset = 65537 - jumpAfterHeader;
          bytes.add(offset);
          bytes.add((offset >> 8));
        }
        bytes.addAll(leftBytes);
        bytes.addAll(rightBytes);
        yield bytes;
      }
      case HuffmanTreeNode.LeafNode leafNode -> List.of(leafNode.paletteIndex());
    };
  }
}
