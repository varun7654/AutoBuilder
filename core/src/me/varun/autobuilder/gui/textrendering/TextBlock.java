package me.varun.autobuilder.gui.textrendering;


import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class TextBlock {
    private Fonts font;
    private boolean dirty = true;
    float wrapWidth;
    private int size;
    private TextComponent[] textComponents;
    float lineSpacing = 1.5f;

    private ArrayList<RenderableTextComponent> renderableTextComponents = new ArrayList<>();

    public TextBlock(Fonts font, int size, TextComponent... textComponent) {
        this.font = font;
        this.size = size;
        this.textComponents = textComponent;
        this.wrapWidth = Float.MAX_VALUE;
    }

    public TextBlock(Fonts font, int size, float wrapWidth, TextComponent... textComponent) {
        this.font = font;
        this.size = size;
        this.textComponents = textComponent;
        this.wrapWidth = wrapWidth;
    }

    public ArrayList<RenderableTextComponent> getRenderableTextComponents() {
        updateIfDirty();
        return renderableTextComponents;
    }

    public void updateIfDirty() {
        if (dirty) {
            update();
        }
    }

    public void update() {
        renderableTextComponents.clear();
        float x = 0, y = 0;
        float componentStartX = 0, componentStartY = 0;
        boolean textWrapped = false;

        for (int i = 0; i < textComponents.length; i++) {
            StringBuilder sb = new StringBuilder();
            int lastWhiteSpaceIndex = 0;

            TextComponent textComponent = textComponents[i];
            FreeTypeBitmapFontData fontData = textComponent.getFontData(font, size);
            char[] chars = textComponent.getText().toCharArray();
            for (int j = 0; j < chars.length; j++) {
                assert sb != null;
                char c = chars[j];
                @Nullable BitmapFont.Glyph glyph = fontData.getGlyph(c);
                if (glyph != null) {
                    x += glyph.xadvance;
                }
                if (c == '\n') {
                    sb.append(textComponent.getText(), lastWhiteSpaceIndex, j);
                    renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                            textComponent.isBold, textComponent.isItalic, textComponent.isUnderlined,
                            textComponent.isStrikethrough, textComponent.color));
                    x = 0;
                    y -= fontData.xHeight * lineSpacing;

                    sb = new StringBuilder();
                    componentStartX = x;
                    componentStartY = y;
                    lastWhiteSpaceIndex = j;
                } else if (Character.isWhitespace(c)) {
                    if (textWrapped) {
                        if (glyph != null) {
                            x -= glyph.xadvance;
                        }
                    } else {
                        sb.append(textComponent.getText(), lastWhiteSpaceIndex, j);
                    }
                    lastWhiteSpaceIndex = j;
                } else {
                    if (textWrapped) {
                        lastWhiteSpaceIndex = j;
                        textWrapped = false;
                    }
                }

                if (x > wrapWidth) {
                    j = lastWhiteSpaceIndex;

                    renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                            textComponent.isBold, textComponent.isItalic, textComponent.isUnderlined,
                            textComponent.isStrikethrough, textComponent.color));

                    x = 0;
                    y += fontData.lineHeight * lineSpacing;
                    componentStartX = x;
                    componentStartY = y;
                    textWrapped = true;
                    sb = null;
                }
            }
            if (sb != null) {
                if (chars.length > lastWhiteSpaceIndex) {
                    sb.append(textComponent.getText(), lastWhiteSpaceIndex, chars.length);
                }
                renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                        textComponent.isBold, textComponent.isItalic, textComponent.isUnderlined,
                        textComponent.isStrikethrough, textComponent.color));
                componentStartX = x;
                componentStartY = y;
            }
        }

        dirty = false;
    }

    public Fonts getFont() {
        return font;
    }

    public void setFont(Fonts font) {
        this.dirty = true;
        this.font = font;
    }

    public boolean isDirty() {
        return dirty;
    }

    public float getWrapWidth() {
        return wrapWidth;
    }

    public void setWrapWidth(float wrapWidth) {
        this.dirty = true;
        this.wrapWidth = wrapWidth;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.dirty = true;
        this.size = size;
    }

    public TextComponent[] getTextComponents() {
        return textComponents;
    }

    public void setTextComponents(TextComponent... textComponents) {
        this.dirty = true;
        this.textComponents = textComponents;
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.dirty = true;
        this.lineSpacing = lineSpacing;
    }
}
