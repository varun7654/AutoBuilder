package me.varun.autobuilder.gui.textrendering;

import com.badlogic.gdx.utils.Disposable;
import org.jetbrains.annotations.NotNull;

public class FontFamily implements Disposable {

    @NotNull public final Font boldFont;
    @NotNull public final Font italicFont;
    @NotNull public final Font regularFont;
    @NotNull public final Font boldItalicFont;
    @NotNull public final Fonts font;

    public FontFamily(@NotNull Fonts font, @NotNull Font regularFont, @NotNull Font boldFont, @NotNull Font italicFont,
                      @NotNull Font boldItalicFont) {
        this.font = font;
        this.boldFont = boldFont;
        this.italicFont = italicFont;
        this.regularFont = regularFont;
        this.boldItalicFont = boldItalicFont;
    }

    public FontFamily(@NotNull Fonts font, @NotNull String regularFont, @NotNull String boldFont, @NotNull String italicFont,
                      @NotNull String boldItalicFont) {
        this.font = font;
        this.boldFont = new Font(boldFont);
        this.italicFont = new Font(italicFont);
        this.regularFont = new Font(regularFont);
        this.boldItalicFont = new Font(boldItalicFont);
    }

    public Font getFont(boolean bold, boolean italic) {
        if (bold && italic) {
            return boldItalicFont;
        } else if (bold) {
            return boldFont;
        } else if (italic) {
            return italicFont;
        } else {
            return regularFont;
        }
    }

    public void generateFonts(int size) {
        boldFont.generateFont(size);
        italicFont.generateFont(size);
        regularFont.generateFont(size);
        boldItalicFont.generateFont(size);
    }

    @Override
    public void dispose() {
        boldFont.dispose();
        italicFont.dispose();
        regularFont.dispose();
        boldItalicFont.dispose();
    }
}