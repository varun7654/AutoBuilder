package me.varun.autobuilder.gui.textrendering;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a text component that can be rendered. Each renderable text component must only be one line and may not
 * span multiple lines.
 */
public class RenderableTextComponent {
    public RenderableTextComponent(@NotNull String text, float x, float y, boolean isBold, boolean isItalic, boolean isUnderlined,
                                   boolean isStrikethrough, @NotNull Color color, int size, @NotNull Fonts font) {
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

    @NotNull final String text;
    final float x;
    final float y;
    final boolean isBold;
    final boolean isItalic;
    final boolean isUnderlined;
    final boolean isStrikethrough;
    @NotNull final Color color;
    final int size;
    @NotNull final Fonts font;

    public BitmapFont getBitmapFont() {
        return FontHandler.getFont(font, isBold, isItalic, size);
    }

    public FreeTypeFontGenerator.FreeTypeBitmapFontData getFontData() {
        return FontHandler.getFontData(font, isBold, isItalic, size);
    }
}
