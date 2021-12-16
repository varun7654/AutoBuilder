package me.varun.autobuilder.gui.textrendering;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FontHandler {
    private static final HashMap<Fonts, FontFamily> FONT_FAMILY_MAP = new HashMap<>();

    static {
        FONT_FAMILY_MAP.put(Fonts.ROBOTO, new FontFamily(Fonts.ROBOTO, "roboto/Roboto-Regular.ttf",
                "roboto/Roboto-Bold.ttf",
                "roboto/Roboto-Italic.ttf",
                "roboto/Roboto-BoldItalic.ttf"));

        FONT_FAMILY_MAP.put(Fonts.JETBRAINS_MONO, new FontFamily(Fonts.JETBRAINS_MONO, "mono/JetBrainsMono-Regular.ttf",
                "mono/JetBrainsMono-Bold.ttf",
                "mono/JetBrainsMono-Italic.ttf",
                "mono/JetBrainsMono-BoldItalic.ttf"));
    }

    private static final List<Integer> fontSizes;

    static {
        fontSizes = new ArrayList<>();
        fontSizes.add(8);
        fontSizes.add(36);
        fontSizes.add(12);
    }


    public static void updateFonts() {
        for (FontFamily fontFamily : FONT_FAMILY_MAP.values()) {
            fontFamily.dispose();
        }

        for (Integer fontSize : fontSizes) {
            for (FontFamily font : FONT_FAMILY_MAP.values()) {
                font.generateFonts(fontSize);
            }
        }
    }

    public static FontFamily getFontFamily(Fonts font) {
        return FONT_FAMILY_MAP.get(font);
    }

    public static Font getFont(Fonts font, boolean bold, boolean italic) {
        return FONT_FAMILY_MAP.get(font).getFont(bold, italic);
    }

    public static BitmapFont getFont(Fonts font, boolean bold, boolean italic, int size) {
        return FONT_FAMILY_MAP.get(font).getFont(bold, italic).getFont(size);
    }

    public static FreeTypeBitmapFontData getFontData(Fonts font, boolean bold, boolean italic, int size) {
        return FONT_FAMILY_MAP.get(font).getFont(bold, italic).getFontData(size);
    }
}
