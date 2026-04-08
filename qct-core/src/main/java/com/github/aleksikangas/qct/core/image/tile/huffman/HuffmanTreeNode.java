/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.huffman;

import java.util.Objects;

/**
 * Node of a Huffman Tree.
 *
 * @see HuffmanCodeBook
 */
sealed interface HuffmanTreeNode permits HuffmanTreeNode.ParentNode, HuffmanTreeNode.LeafNode {
  int frequency();

  /**
   * A parent node of Huffman Tree.
   *
   * @param left
   * @param right
   * @see HuffmanTreeNode
   * @see LeafNode
   * @see HuffmanCodeBook
   */
  record ParentNode(int frequency,
                    HuffmanTreeNode left,
                    HuffmanTreeNode right) implements HuffmanTreeNode {
    public ParentNode {
      Objects.requireNonNull(left);
      Objects.requireNonNull(right);
    }
  }

  /**
   *
   * A leaf node of Huffman Tree.
   *
   * @param frequency
   * @param paletteIndex
   * @see HuffmanTreeNode
   * @see ParentNode
   * @see HuffmanCodeBook
   */
  record LeafNode(int frequency,
                  int paletteIndex) implements HuffmanTreeNode {
  }
}
