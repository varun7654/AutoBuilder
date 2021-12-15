package me.varun.autobuilder.gui.textrendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FontsHandler {
    private static final List<FontType> fontFilesNames;

    static {
        fontFilesNames = new ArrayList<>();
        //Add the jetbains mono fonts
        fontFilesNames.add(new FontType(Font.JETBRAINS_MONO, true, false, "mono/JetBrainsMono-Bold.ttf"));
        fontFilesNames.add(new FontType(Font.JETBRAINS_MONO, true, true, "mono/JetBrainsMono-BoldItalic.ttf"));
        fontFilesNames.add(new FontType(Font.JETBRAINS_MONO, false, true, "mono/JetBrainsMono-Italic.ttf"));
        fontFilesNames.add(new FontType(Font.JETBRAINS_MONO, false, false, "mono/JetBrainsMono-Regular.ttf"));
    }

    private static List<Integer> fontSizes;

    static {
        fontSizes = new ArrayList<>();
        fontSizes.add(8);
        fontSizes.add(36);
        fontSizes.add(12);
    }

    public enum Fonts {
        JETBRAINS_MONO
    }

    static HashMap<Integer, HashMap<String, BitmapFont>> fonts = new HashMap<>();

    public static void updateFonts() {
        for (HashMap<String, BitmapFont> value : fonts.values()) {
            for (BitmapFont font : value.values()) {
                font.dispose();
            }
        }
        fonts.clear();

        for (Integer fontSize : fontSizes) {
            HashMap<String, BitmapFont> bitmapFontHashMap = new HashMap<>();
            for (FontType fontType : fontFilesNames) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fontType.fontPath));
                FreeTypeFontParameter parameter = new FreeTypeFontParameter();
                parameter.size = fontSize;
                BitmapFont font = generator.generateFont(parameter);
                generator.dispose(); // don't forget to dispose to avoid memory leaks!
                bitmapFontHashMap.put(fontType.fontName.toString() + (fontType.isBold ? "Bold" : "") +
                        (fontType.isItalic ? "Italic" : ""), font);
            }
            fonts.put(fontSize, bitmapFontHashMap);
        }
    }

    public static BitmapFont getFont(@NotNull Fonts font, boolean bold, boolean italic, int fontSize) {
        return fonts.get(fontSize).get(font.toString() + (bold ? "Bold" : "") + (italic ? "Italic" : ""));
    }
}
