package com.dacubeking.autobuilder.gui.gui.textrendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represents a part of a textblock that has the same styling throughout it. To change the styling within a textblock, you will
 * need to create a new TextComponent for each part that contains different styling.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TextComponent implements Cloneable {
    /**
     * The text that this component contains.
     */
    @NotNull public String text;

    /**
     * If the text in this component is bold.
     */
    public boolean isBold = false;

    /**
     * If the text in this component is italic.
     */
    public boolean isItalic = false;

    /**
     * If the text in this component is strikethrough.
     */
    public boolean isStrikethrough = false;

    /**
     * If the text in this component is underlined.
     */
    public boolean isUnderlined = false;

    /**
     * If the text in this component is highlighted.
     */
    public boolean isHighlighted = false;


    /**
     * The color of the text in this component.
     */
    @NotNull public Color color = Color.BLACK;

    /**
     * The optional font size of the text in this component.
     */
    @NotNull public OptionalInt size = OptionalInt.empty();

    /**
     * The optional font of the text in this component.
     */
    @NotNull public Optional<Fonts> font = Optional.empty();

    /**
     * The optional color of the underline in this component.
     */
    @NotNull public Optional<Color> underlineColor = Optional.empty();

    /**
     * The optional color of the strikethrough in this component.
     */
    @NotNull public Optional<Color> strikethroughColor = Optional.empty();

    /**
     * The optional color of the highlight in this component.
     */
    @NotNull public Optional<Color> highlightColor = Optional.empty();

    /**
     * @param text  the text of this component.
     * @param color the color of this component.
     */
    public TextComponent(@NotNull String text, @NotNull Color color) {
        this.text = text;
        this.color = color;
    }

    /**
     * @param text the text of this component.
     */
    public TextComponent(@NotNull String text) {
        this.text = text;
    }

    /**
     * @return the text of this component
     */
    public @NotNull String getText() {
        return text;
    }

    /**
     * @param text the text of this component
     * @return {@link TextComponent} this
     */
    public @NotNull TextComponent setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * @return true if the font is bold
     */
    public boolean isBold() {
        return isBold;
    }

    /**
     * @param bold set if the font is bold
     * @return {@link TextComponent} this
     */
    public @NotNull TextComponent setBold(boolean bold) {
        isBold = bold;
        return this;
    }

    /**
     * @return true if the font is italic
     */
    public boolean isItalic() {
        return isItalic;
    }

    /**
     * @param italic set if the font is italic
     * @return {@link TextComponent} this
     */
    public @NotNull TextComponent setItalic(boolean italic) {
        isItalic = italic;
        return this;
    }

    /**
     * @return true if strikethrough is enabled
     */
    public boolean isStrikethrough() {
        return isStrikethrough;
    }

    /**
     * @param strikethrough set if the text should be strikethrough
     * @return {@link TextComponent} this
     */
    public @NotNull TextComponent setStrikethrough(boolean strikethrough) {
        isStrikethrough = strikethrough;
        return this;
    }

    /**
     * @return true if underline is enabled
     */
    public boolean isUnderlined() {
        return isUnderlined;
    }

    /**
     * @param underlined set if the text should be underlined
     * @return {@link TextComponent} this
     */
    public @NotNull TextComponent setUnderlined(boolean underlined) {
        isUnderlined = underlined;
        return this;
    }


    /**
     * @param highlighted set if the text should be highlighted
     * @return {@link TextComponent} this
     */
    public @NotNull TextComponent setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        return this;
    }

    public @NotNull Color getColor() {
        return color;
    }

    /**
     * @param color color to set the text to
     * @return {@link TextComponent} this
     */
    public @NotNull TextComponent setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @param font default font if one is not specified
     * @param size default font size if one is not specified
     * @return the font that is requested
     */
    public @NotNull BitmapFont getBitmapFont(Fonts font, int size) {
        return FontHandler.getFont(this.font.orElse(font), isBold, isItalic, this.size.orElse(size));
    }

    /**
     * @param font default font if one is not specified
     * @param size default font size if one is not specified
     * @return the font data that is requested
     */
    public @NotNull FreeTypeBitmapFontData getFontData(Fonts font, int size) {
        return FontHandler.getFontData(this.font.orElse(font), isBold, isItalic, this.size.orElse(size));
    }

    public @NotNull TextComponent setSize(int size) {
        this.size = OptionalInt.of(size);
        return this;
    }

    public @NotNull OptionalInt getSize() {
        return size;
    }

    @Override
    public String toString() {
        return text;
    }

    public @NotNull Color getUnderlineColor() {
        return underlineColor.orElse(color);
    }

    public TextComponent setUnderlineColor(Color color) {
        this.underlineColor = Optional.of(color);
        return this;
    }

    public @NotNull Color getStrikethroughColor() {
        return strikethroughColor.orElse(color);
    }

    public TextComponent setStrikethroughColor(Color color) {
        this.strikethroughColor = Optional.of(color);
        return this;
    }

    public @NotNull Color getHighlightColor() {
        return highlightColor.orElse(color);
    }

    public TextComponent setHighlightColor(Color color) {
        this.highlightColor = Optional.of(color);
        return this;
    }

    public TextComponent setFont(Fonts font) {
        this.font = Optional.of(font);
        return this;
    }


    @Override
    public TextComponent clone() {
        try {
            TextComponent clone = (TextComponent) super.clone();
            clone.color = new Color(color);
            clone.underlineColor = Optional.ofNullable(
                    underlineColor.isPresent() ? new Color(underlineColor.orElseThrow()) : null);
            clone.strikethroughColor = Optional.ofNullable(
                    strikethroughColor.isPresent() ? new Color(strikethroughColor.orElseThrow()) : null);
            clone.highlightColor = Optional.ofNullable(
                    highlightColor.isPresent() ? new Color(highlightColor.orElseThrow()) : null);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
