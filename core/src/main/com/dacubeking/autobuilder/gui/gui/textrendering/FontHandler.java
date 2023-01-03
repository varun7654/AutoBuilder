package com.dacubeking.autobuilder.gui.gui.textrendering;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FontHandler {
    @NotNull private static final HashMap<Fonts, FontFamily> FONT_FAMILY_MAP = new HashMap<>();

    static {
        FONT_FAMILY_MAP.put(Fonts.ROBOTO, new FontFamily(Fonts.ROBOTO, "Roboto/Roboto-Regular.ttf",
                "Roboto/Roboto-Bold.ttf",
                "Roboto/Roboto-Italic.ttf",
                "Roboto/Roboto-BoldItalic.ttf"));

        FONT_FAMILY_MAP.put(Fonts.JETBRAINS_MONO, new FontFamily(Fonts.JETBRAINS_MONO, "JetBrainsMono/JetBrainsMono-Regular.ttf",
                "JetBrainsMono/JetBrainsMono-Bold.ttf",
                "JetBrainsMono/JetBrainsMono-Italic.ttf",
                "JetBrainsMono/JetBrainsMono-BoldItalic.ttf"));

        FONT_FAMILY_MAP.put(Fonts.ROBOTO_MONO, new FontFamily(Fonts.ROBOTO_MONO, "RobotoMono/RobotoMono-Regular.ttf",
                "RobotoMono/RobotoMono-Bold.ttf",
                "RobotoMono/RobotoMono-Italic.ttf",
                "RobotoMono/RobotoMono-BoldItalic.ttf"));
    }

    @NotNull private static final List<Integer> fontSizes;

    static {
        fontSizes = new ArrayList<>();
    }

    public static void dispose() {
        for (FontFamily fontFamily : FONT_FAMILY_MAP.values()) {
            fontFamily.dispose();
        }
    }

    public static void updateFonts() {
        for (FontFamily fontFamily : FONT_FAMILY_MAP.values()) {
            fontFamily.dispose();
        }
    }

    public static @NotNull FontFamily getFontFamily(@NotNull Fonts font) {
        return FONT_FAMILY_MAP.get(font);
    }

    public static @NotNull Font getFont(@NotNull Fonts font, boolean bold, boolean italic) {
        return FONT_FAMILY_MAP.get(font).getFont(bold, italic);
    }

    public static @NotNull BitmapFont getFont(@NotNull Fonts font, boolean bold, boolean italic, int size) {
        return FONT_FAMILY_MAP.get(font).getFont(bold, italic).getFont(size);
    }

    public static @NotNull FreeTypeBitmapFontData getFontData(@NotNull Fonts font, boolean bold, boolean italic, int size) {
        return FONT_FAMILY_MAP.get(font).getFont(bold, italic).getFontData(size);
    }
}
