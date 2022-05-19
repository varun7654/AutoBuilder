package com.dacubeking.autobuilder.gui.gui.textrendering;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a text component that can be rendered. Each renderable text component must only be one line and may not
 * span multiple lines.
 */
public class RenderableTextComponent {
    public RenderableTextComponent(@NotNull String text, float x, float y, float endX, boolean isBold, boolean isItalic,
                                   boolean isUnderlined, boolean isStrikethrough, boolean isHighlighted, @NotNull Color color,
                                   @NotNull Color underlineColor, @NotNull Color strikethroughColor,
                                   @NotNull Color highlightColor, int size, @NotNull Fonts font, int row) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderlined = isUnderlined;
        this.isStrikethrough = isStrikethrough;
        this.isHighlighted = isHighlighted;
        this.color = color;
        this.underlineColor = underlineColor;
        this.strikethroughColor = strikethroughColor;
        this.highlightColor = highlightColor;
        this.size = size;
        this.font = font;
        this.row = row;
    }

    /**
     * The text that should be rendered.
     */
    @NotNull final String text;

    /**
     * Relative x position of the text.
     */
    final float x;

    /**
     * Relative y position of the text.
     */
    final float y;

    /**
     * Relative end x position of the text.
     */
    final float endX;

    /**
     * Whether the text is bold.
     */
    final boolean isBold;

    /**
     * Whether the text is italic.
     */
    final boolean isItalic;

    /**
     * Whether the text is underlined.
     */
    final boolean isUnderlined;

    /**
     * Whether the text is strikethrough.
     */
    final boolean isStrikethrough;

    /**
     * Whether the text is highlighted.
     */
    final boolean isHighlighted;

    /**
     * Color of the text.
     */
    @NotNull final Color color;

    /**
     * Color of the underline.
     */
    @NotNull final Color underlineColor;

    /**
     * Color of the strikethrough.
     */
    @NotNull final Color strikethroughColor;

    /**
     * The color of the highlight.
     */
    @NotNull final Color highlightColor;

    /**
     * Font size of the text.
     */
    final int size;

    /**
     * Font of the text.
     */
    @NotNull final Fonts font;

    /**
     * Row of the text.
     */
    final int row;

    /**
     * @return The font that the text should be rendered with.
     */
    public BitmapFont getBitmapFont() {
        return FontHandler.getFont(font, isBold, isItalic, size);
    }

    public FreeTypeFontGenerator.FreeTypeBitmapFontData getFontData() {
        return FontHandler.getFontData(font, isBold, isItalic, size);
    }

    @Override
    public String toString() {
        return text;
    }
}
