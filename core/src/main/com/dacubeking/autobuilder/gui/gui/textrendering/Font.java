package com.dacubeking.autobuilder.gui.gui.textrendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Font implements Disposable {
    private @NotNull final String fontPath;
    private @NotNull final HashMap<Integer, FreeTypeBitmapFontData> fontData = new HashMap<>();
    private @NotNull final HashMap<Integer, BitmapFont> font = new HashMap<>();

    public Font(final @NotNull String fontPath) {
        this.fontPath = fontPath;
    }

    public void generateFont(int fontSize) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fontPath));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = fontSize;
        font.put(fontSize, generator.generateFont(parameter));
        fontData.put(fontSize, generator.generateData(parameter));
        generator.dispose(); // don't forget to dispose to avoid memory leaks!
    }

    @Override
    public void dispose() {
        font.values().forEach(BitmapFont::dispose);
        fontData.values().forEach(FreeTypeBitmapFontData::dispose);
        font.clear();
        fontData.clear();
    }

    public @NotNull String getFontPath() {
        return fontPath;
    }

    public FreeTypeBitmapFontData getFontData(int fontSize) {
        if (!fontData.containsKey(fontSize)) {
            generateFont(fontSize);
            //throw new IllegalArgumentException("Font size " + fontSize + " not found");
        }
        return fontData.get(fontSize);
    }

    public BitmapFont getFont(int fontSize) {
        if (!font.containsKey(fontSize)) {
            generateFont(fontSize);
            //throw new IllegalArgumentException("Font size " + fontSize + " not found");
        }
        return font.get(fontSize);
    }

}
