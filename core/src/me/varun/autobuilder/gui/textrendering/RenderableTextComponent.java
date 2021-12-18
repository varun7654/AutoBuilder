package me.varun.autobuilder.gui.textrendering;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class RenderableTextComponent {
    public RenderableTextComponent(String text, float x, float y, boolean isBold, boolean isItalic, boolean isUnderlined,
                                   boolean isStrikethrough, Color color, int size, Fonts font) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderlined = isUnderlined;
        this.isStrikethrough = isStrikethrough;
        this.color = color;
        this.size = size;
        this.font = font;
    }

    final String text;
    final float x;
    final float y;
    final boolean isBold;
    final boolean isItalic;
    final boolean isUnderlined;
    final boolean isStrikethrough;
    final Color color;
    final int size;
    final Fonts font;

    public BitmapFont getBitmapFont() {
        return FontHandler.getFont(font, isBold, isItalic, size);
    }

    public FreeTypeFontGenerator.FreeTypeBitmapFontData getFontData() {
        return FontHandler.getFontData(font, isBold, isItalic, size);
    }
}
