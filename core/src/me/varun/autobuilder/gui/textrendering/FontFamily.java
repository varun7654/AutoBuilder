package me.varun.autobuilder.gui.textrendering;

import com.badlogic.gdx.utils.Disposable;

public class FontFamily implements Disposable {

    public final Font boldFont;
    public final Font italicFont;
    public final Font regularFont;
    public final Font boldItalicFont;
    public final Fonts font;

    public FontFamily(Fonts font, Font regularFont, Font boldFont, Font italicFont, Font boldItalicFont) {
        this.font = font;
        this.boldFont = boldFont;
        this.italicFont = italicFont;
        this.regularFont = regularFont;
        this.boldItalicFont = boldItalicFont;
    }

    public FontFamily(Fonts font, String regularFont, String boldFont, String italicFont, String boldItalicFont) {
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