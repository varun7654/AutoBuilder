package me.varun.autobuilder.gui.textrendering;

public final class FontType {
    public boolean isBold;
    public boolean isItalic;
    public Font fontName;
    public String fontPath;

    public FontType(final Font fontName, final boolean isBold, final boolean isItalic, final String fontPath) {
        this.fontName = fontName;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.fontPath = fontPath;
    }
}
