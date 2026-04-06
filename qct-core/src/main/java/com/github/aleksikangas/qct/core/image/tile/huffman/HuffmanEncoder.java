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
   * @param encoding   {@link ImageTile.Encoding#HUFFMAN_CODING}
   * @param pixelBytes pixel data bytes
   */
  public record Candidate(ImageTile.Encoding encoding,
                          int[] pixelBytes) implements ImageTileEncodingCandidate {
    public Candidate(final int[] pixelBytes) {
      this(ImageTile.Encoding.HUFFMAN_CODING, pixelBytes);
    }

    public Candidate {
      Objects.requireNonNull(encoding);
      Objects.requireNonNull(pixelBytes);
    }

    @Override
    public int sizeBytes() {
      return pixelBytes.length;
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
      return Objects.deepEquals(pixelBytes, candidate.pixelBytes) && encoding == candidate.encoding;
    }

    @Override
    public int hashCode() {
      return Objects.hash(encoding, Arrays.hashCode(pixelBytes));
    }

    @Nonnull
    @Override
    public String toString() {
      return "RleImageTileEncodingCandidate{" +
             "encoding=" +
             encoding +
             ", pixelBytes=" +
             Arrays.toString(pixelBytes) +
             '}';
    }

    public static Candidate of(final ImageTile imageTile) {
      final int[][] paletteIndices = imageTile.paletteIndices();
      final int[] frequencies = new int[Palette.SIZE];
      for (int y = 0; y < ImageTile.HEIGHT; ++y) {
        for (int x = 0; x < ImageTile.WIDTH; ++x) {
          final int idx = paletteIndices[y][x];
          ++frequencies[idx];
        }
      }
      final HuffmanTreeNode root = buildHuffmanTree(frequencies);
      // Build the compact codebook (the exact linear format expected by HuffmanCodeBook)
      final List<Integer> codebookList = recursiveSerializeTree(root);

      // Build variable-length codes (0 = continue/inline = left child, 1 = jump = right child)
      final Map<Integer, List<Boolean>> codes = buildCodes(root);

      // Generate the raw bit stream in the exact order the decoder expects
      // (sequential row-major order matching HuffmanDecoder.decodeComplex)
      final List<Boolean> allBits = new ArrayList<>();
      for (int y = 0; y < ImageTile.HEIGHT; ++y) {
        for (int x = 0; x < ImageTile.WIDTH; ++x) {
          final int pidx = paletteIndices[y][x];
          final List<Boolean> codeBits = codes.get(pidx);
          if (codeBits == null) {
            throw new IllegalStateException("Missing Huffman code for palette index " + pidx);
          }
          allBits.addAll(codeBits);
        }
      }

      // Pack bits into bytes (LSB-first, exactly as consumed by the decoder)
      final int numBitstreamBytes = (allBits.size() + 7) / 8;
      final int[] bitstream = new int[numBitstreamBytes];
      int bitIndex = 0;
      for (int b = 0; b < numBitstreamBytes; ++b) {
        int byteVal = 0;
        for (int i = 0; i < 8 && bitIndex < allBits.size(); ++i, ++bitIndex) {
          if (Boolean.TRUE.equals(allBits.get(bitIndex))) {
            byteVal |= (1 << i);   // bit 0 of the stream goes into LSB of the byte
          }
        }
        bitstream[b] = byteVal;
      }

      // pixelBytes = codebook bytes immediately followed by bit-stream bytes
      // (for blank/single-colour tiles the bit-stream is empty, as required by the spec)
      final int[] pixelBytesArr = new int[codebookList.size() + bitstream.length];
      for (int i = 0; i < codebookList.size(); ++i) {
        pixelBytesArr[i] = codebookList.get(i);
      }
      System.arraycopy(bitstream, 0, pixelBytesArr, codebookList.size(), bitstream.length);

      return new Candidate(pixelBytesArr);
    }
  }

  private static HuffmanTreeNode buildHuffmanTree(final int[] frequencies) {
    final PriorityQueue<HuffmanTreeNode> pq = new PriorityQueue<>(Comparator.comparingInt(HuffmanTreeNode::frequency));
    for (int i = 0; i < Palette.SIZE; ++i) {
      if (frequencies[i] > 0) {
        pq.add(new HuffmanTreeNode.LeafNode(frequencies[i], i));
      }
    }
    if (pq.isEmpty()) {
      throw new IllegalArgumentException("Tile contains no colours");
    }
    if (pq.size() == 1) {
      return pq.poll(); // single color tile (blank tile)
    }
    while (pq.size() > 1) {
      final HuffmanTreeNode left = pq.poll();
      final HuffmanTreeNode right = pq.poll();
      // Left = continue (bit 0), right = jump (bit 1). The exact assignment of children does not
      // affect code lengths (Huffman guarantees optimality) but determines the concrete bit values.
      final HuffmanTreeNode parent = new HuffmanTreeNode.ParentNode(Objects.requireNonNull(left).frequency() +
                                                                    Objects.requireNonNull(right).frequency(),
                                                                    left,
                                                                    right);
      pq.add(parent);
    }
    return pq.poll();
  }

  private static List<Integer> recursiveSerializeTree(final HuffmanTreeNode node) {
    return switch (node) {
      case HuffmanTreeNode.ParentNode parentNode -> {
        // Recursively serialize both children first, in order to compute the jump
        final List<Integer> leftBytes = recursiveSerializeTree(parentNode.left());
        final List<Integer> rightBytes = recursiveSerializeTree(parentNode.right());

        // Is this a near or far branch?
        final int tentativeBranchSize = 1;
        final int tentativeRelativeJump = tentativeBranchSize + leftBytes.size();
        final boolean isNearBranch = tentativeRelativeJump <= 127;
        final int branchSize = isNearBranch ? 1 : 3;
        final int relativeJump = branchSize + leftBytes.size();

        final List<Integer> subTreeBytes = new ArrayList<>();
        if (isNearBranch) {
          final int bn = 257 - relativeJump;
          subTreeBytes.add(bn);
        } else {
          subTreeBytes.add(128);  // far branch marker
          final int temp = 65539 - relativeJump;
          final int b1 = temp & 0xFF;
          final int b2 = (temp >> 8) & 0xFF;
          subTreeBytes.add(b1);
          subTreeBytes.add(b2);
        }
        subTreeBytes.addAll(leftBytes);
        subTreeBytes.addAll(rightBytes);
        yield subTreeBytes;
      }
      case HuffmanTreeNode.LeafNode leafNode -> List.of(leafNode.paletteIndex());
    };
  }

  private static Map<Integer, List<Boolean>> buildCodes(final HuffmanTreeNode root) {
    final Map<Integer, List<Boolean>> codes = new HashMap<>();
    buildCodesRecursive(root, new ArrayList<>(), codes);
    return codes;
  }

  private static void buildCodesRecursive(final HuffmanTreeNode node,
                                          final List<Boolean> currentCode,
                                          final Map<Integer, List<Boolean>> codes) {
    switch (node) {
      case HuffmanTreeNode.ParentNode parentNode -> {
        final List<Boolean> leftCode = new ArrayList<>(currentCode);
        leftCode.add(false);
        buildCodesRecursive(parentNode.left(), leftCode, codes);

        final List<Boolean> rightCode = new ArrayList<>(currentCode);
        rightCode.add(true);
        buildCodesRecursive(parentNode.right(), rightCode, codes);
      }
      case HuffmanTreeNode.LeafNode leafNode -> codes.put(leafNode.paletteIndex(), new ArrayList<>(currentCode));
    }

  }

  private HuffmanEncoder() {
  }
}
