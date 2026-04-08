/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.image.tile.huffman;

import com.github.aleksikangas.qct.core.color.Palette;
import com.github.aleksikangas.qct.core.image.tile.ImageTile;
import com.github.aleksikangas.qct.core.image.tile.ImageTileEncodingCandidate;
import com.github.aleksikangas.qct.core.utils.QctWriter;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Encoder of {@link ImageTile}s using {@link ImageTile.Encoding#HUFFMAN_CODING}.
 *
 * @see HuffmanDecoder
 */
public final class HuffmanEncoder {
  /**
   * A candidate for {@link ImageTile} encoding.
   *
   * @param pixelBytes pixel data bytes
   */
  public record Candidate(int[] pixelBytes) implements ImageTileEncodingCandidate {
    public Candidate {
      Objects.requireNonNull(pixelBytes);
    }

    @Override
    public ImageTile.Encoding encoding() {
      return ImageTile.Encoding.HUFFMAN_CODING;
    }

    @Override
    public int sizeBytes() {
      return 0x01 + pixelBytes.length;
    }

    @Override
    public void encode(final QctWriter qctWriter, final int byteOffset) {
      qctWriter.writeByte(byteOffset, 0x00);
      qctWriter.writeBytes(byteOffset + 1, pixelBytes);
    }

    @Override
    public boolean equals(final Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      final Candidate candidate = (Candidate) o;
      return Objects.deepEquals(pixelBytes, candidate.pixelBytes);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(pixelBytes);
    }

    @Nonnull
    @Override
    public String toString() {
      return "Candidate{" + "pixelBytes=" + Arrays.toString(pixelBytes) + '}';
    }

    public static Candidate of(final ImageTile imageTile) {
      final int[][] paletteIndices = imageTile.paletteIndices();
      final int[] frequencies = new int[Palette.SIZE];
      for (int y = 0; y < ImageTile.HEIGHT; ++y) {
        for (int x = 0; x < ImageTile.WIDTH; ++x) {
          ++frequencies[paletteIndices[y][x]];
        }
      }
      final HuffmanTreeNode rootNode = buildHuffmanTree(frequencies);
      final HuffmanCodeBook codeBook = new HuffmanCodeBook(rootNode);
      final List<Integer> codebookBytes = codeBook.toBytes();
      final Map<Integer, List<Boolean>> codes = buildCodes(rootNode);
      final List<Boolean> allBits = new ArrayList<>();
      for (int y = 0; y < ImageTile.HEIGHT; ++y) {
        for (int x = 0; x < ImageTile.WIDTH; ++x) {
          allBits.addAll(codes.get(paletteIndices[y][x]));
        }
      }
      final int numBytes = (allBits.size() + 7) / 8;
      final int[] bitstream = new int[numBytes];
      for (int i = 0; i < allBits.size(); i++) {
        if (Boolean.TRUE.equals(allBits.get(i))) {
          bitstream[i / 8] |= (1 << (i % 8));
        }
      }

      // 7. Combine codebook + bitstream
      final int[] pixelBytes = new int[codebookBytes.size() + bitstream.length];
      for (int i = 0; i < codebookBytes.size(); i++) {
        pixelBytes[i] = codebookBytes.get(i);
      }
      System.arraycopy(bitstream, 0, pixelBytes, codebookBytes.size(), bitstream.length);

      return new Candidate(pixelBytes);
    }
  }

  private static HuffmanTreeNode buildHuffmanTree(final int[] frequencies) {
    final PriorityQueue<HuffmanTreeNode> pq = new PriorityQueue<>(Comparator.comparingInt(HuffmanTreeNode::frequency));

    for (int i = 0; i < Palette.SIZE; i++) {
      if (frequencies[i] > 0) {
        pq.add(new HuffmanTreeNode.LeafNode(frequencies[i], i));
      }
    }

    if (pq.isEmpty()) throw new IllegalArgumentException("Tile contains no colours");
    if (pq.size() == 1) return pq.poll();

    while (pq.size() > 1) {
      HuffmanTreeNode left = pq.poll();
      HuffmanTreeNode right = pq.poll();
      pq.add(new HuffmanTreeNode.ParentNode(Objects.requireNonNull(left).frequency() +
                                            Objects.requireNonNull(right).frequency(), left, right));
    }
    return pq.poll();
  }

  private static Map<Integer, List<Boolean>> buildCodes(final HuffmanTreeNode root) {
    final Map<Integer, List<Boolean>> codes = new HashMap<>();
    buildCodesRecursive(root, new ArrayList<>(), codes);
    return codes;
  }

  private static void buildCodesRecursive(final HuffmanTreeNode node,
                                          final List<Boolean> current,
                                          final Map<Integer, List<Boolean>> codes) {
    if (node instanceof HuffmanTreeNode.ParentNode parent) {
      // 0 = left, 1 = right
      var leftCode = new ArrayList<>(current);
      leftCode.add(false);
      buildCodesRecursive(parent.left(), leftCode, codes);

      var rightCode = new ArrayList<>(current);
      rightCode.add(true);
      buildCodesRecursive(parent.right(), rightCode, codes);
    } else if (node instanceof HuffmanTreeNode.LeafNode leaf) {
      codes.put(leaf.paletteIndex(), new ArrayList<>(current));
    }
  }

  private HuffmanEncoder() {
  }
}
