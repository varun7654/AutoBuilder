package me.varun.autobuilder.gui.textrendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;

import java.util.Optional;
import java.util.OptionalInt;

public class TextComponent {
    public String text;
    public boolean isBold = false;
    public boolean isItalic = false;
    public boolean isStrikethrough = false;
    public boolean isUnderlined = false;
    public boolean isObfuscated = false;
    public Color color = Color.WHITE;
    public OptionalInt size = OptionalInt.empty();
    public Optional<Fonts> font = Optional.empty();

    public TextComponent(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public TextComponent(String text) {
        this.text = text;
    }

    public TextComponent(String text, boolean isBold, boolean isItalic, boolean isStrikethrough, boolean isUnderlined,
                         boolean isObfuscated, Color color) {
        this.text = text;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isStrikethrough = isStrikethrough;
        this.isUnderlined = isUnderlined;
        this.isObfuscated = isObfuscated;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public TextComponent setText(String text) {
        this.text = text;
        return this;
    }

    public boolean isBold() {
        return isBold;
    }

    public TextComponent setBold(boolean bold) {
        isBold = bold;
        return this;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public TextComponent setItalic(boolean italic) {
        isItalic = italic;
        return this;
    }

    public boolean isStrikethrough() {
        return isStrikethrough;
    }

    public TextComponent setStrikethrough(boolean strikethrough) {
        isStrikethrough = strikethrough;
        return this;
    }

    public boolean isUnderlined() {
        return isUnderlined;
    }

    public TextComponent setUnderlined(boolean underlined) {
        isUnderlined = underlined;
        return this;
    }

    /**
     * not implemented yet
     */
    public boolean isObfuscated() {
        return isObfuscated;
    }

    /**
     * not implemented yet
     */
    public TextComponent setObfuscated(boolean obfuscated) {
        isObfuscated = obfuscated;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public TextComponent setColor(Color color) {
        this.color = color;
        return this;
    }

    public BitmapFont getBitmapFont(Fonts font, int size) {
        return FontHandler.getFont(font, isBold, isItalic, size);
    }

    public FreeTypeBitmapFontData getFontData(Fonts font, int size) {
        return FontHandler.getFontData(this.font.orElse(font), isBold, isItalic, this.size.orElse(size));
    }

    public TextComponent setSize(int size) {
        this.size = OptionalInt.of(size);
        return this;
    }

    public OptionalInt getSize() {
        return size;
    }
}
