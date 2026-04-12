/*
 * Copyright (c) 2026 Aleksi Kangas
 */

package com.github.aleksikangas.qct.core.color;

import com.github.aleksikangas.qct.core.utils.DirectQctReader;
import com.github.aleksikangas.qct.core.utils.QctReader;
import com.github.aleksikangas.qct.core.utils.QctWriter;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class PaletteTest {
  private Path tempFile;
  private FileChannel fileChannel;
  private QctReader qctReader;
  private QctWriter qctWriter;

  @BeforeEach
  void beforeEach() throws IOException {
    tempFile = Files.createTempFile("palette", ".bin");
    fileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
    qctReader = new DirectQctReader(fileChannel);
    qctWriter = new QctWriter(fileChannel, Palette.SIZE * 4);
  }

  @AfterEach
  void afterEach() throws IOException {
    fileChannel.close();
    Files.deleteIfExists(tempFile);
  }

  @Nested
  @DisplayName("Palette")
  class RecordTests {
    @Test
    void constructor() {
      final Color[] colors = createTestColors();
      final var palette = new Palette(colors);
      assertEquals(Palette.SIZE, palette.colors().length);
      assertEquals(colors[0], palette.color(0));
      assertEquals(colors[127], palette.color(127));
      assertThrows(NullPointerException.class, () -> new Palette(null));
      assertThrows(IllegalStateException.class, () -> new Palette(new Color[127]));
      assertThrows(IllegalStateException.class, () -> new Palette(new Color[129]));
    }

    @Test
    void immutable() {
      final Color[] original = createTestColors();
      final var palette = new Palette(original);
      assertEquals(original[5], palette.color(5));
    }

    @Test
    void colorMethod() {
      final Color[] colors = createTestColors();
      final var palette = new Palette(colors);
      for (int i = 0; i < Palette.SIZE; i++) {
        assertEquals(colors[i], palette.color(i));
      }
      assertThrows(IndexOutOfBoundsException.class, () -> palette.color(-1));
      assertThrows(IndexOutOfBoundsException.class, () -> palette.color(Palette.SIZE));
    }

    @Test
    void equalsAndHashCode() {
      final Color[] colors1 = createTestColors();
      final Color[] colors2 = createTestColors();
      final Color[] colors3 = createTestColors();
      colors3[50] = Color.MAGENTA;

      final var a = new Palette(colors1);
      final var b = new Palette(colors2);
      final var c = new Palette(colors3);

      assertEquals(a, b);
      assertNotEquals(a, c);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringContainsColors() {
      final var palette = new Palette(createTestColors());
      final String str = palette.toString();
      assertNotNull(str);
    }
  }

  @Nested
  @DisplayName("byteValues")
  class ByteValuesTests {
    @Test
    void byteValuesCorrectOrderAndPadding() {
      final Color[] colors = createTestColors();
      final var palette = new Palette(colors);
      final int[] bytes = palette.byteValues();

      assertEquals(Palette.SIZE * 4, bytes.length);

      for (int i = 0; i < Palette.SIZE; i++) {
        final Color color = colors[i];
        final int base = i * 4;

        assertEquals(color.getBlue(), bytes[base]);
        assertEquals(color.getGreen(), bytes[base + 1]);
        assertEquals(color.getRed(), bytes[base + 2]);
        assertEquals(0, bytes[base + 3]);
      }
    }
  }

  @Nested
  @DisplayName("Decoder")
  class DecoderTests {
    @Test
    void decode() {
      final Color[] expectedColors = createTestColors();
      final Palette expectedPalette = new Palette(expectedColors);
      qctWriter.writeBytes(Palette.BYTE_OFFSET, expectedPalette.byteValues());

      final Palette decodedPalette = Palette.Decoder.decode(qctReader);

      assertEquals(expectedPalette, decodedPalette);
    }

    @Test
    void decodeProducesCorrectColorObjects() {
      final Color testColor = new Color(100, 150, 200); // R=100, G=150, B=200
      final int[] singleColorBytes = { 200, // Blue
                                       150, // Green
                                       100, // Red
                                       0    // Padding
      };
      qctWriter.writeBytes(Palette.BYTE_OFFSET, singleColorBytes);
      qctWriter.writeBytes(Palette.BYTE_OFFSET + 4, new int[(Palette.SIZE - 1) * 4]);

      final Palette palette = Palette.Decoder.decode(qctReader);

      assertEquals(testColor, palette.color(0));
    }
  }

  @Nested
  @DisplayName("Encoder")
  class EncoderTests {
    @Test
    void encode() {
      final Color[] colors = createTestColors();
      final var palette = new Palette(colors);

      Palette.Encoder.encode(qctWriter, palette);
      final Palette decodedPalette = Palette.Decoder.decode(qctReader);

      assertEquals(palette, decodedPalette);
    }
  }

  @Test
  void roundTrip() {
    final Color[] originalColors = createTestColors();
    final var originalPalette = new Palette(originalColors);

    Palette.Encoder.encode(qctWriter, originalPalette);
    final Palette decodedPalette = Palette.Decoder.decode(qctReader);

    assertEquals(originalPalette, decodedPalette);
  }

  private Color[] createTestColors() {
    final Color[] colors = new Color[Palette.SIZE];
    for (int i = 0; i < Palette.SIZE; i++) {
      final int r = (i * 2) % 256;
      final int g = (i * 3) % 256;
      final int b = (i * 5) % 256;
      colors[i] = new Color(r, g, b);
    }
    return colors;
  }
}
