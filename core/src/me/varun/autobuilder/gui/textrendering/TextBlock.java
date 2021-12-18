package me.varun.autobuilder.gui.textrendering;


import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TextBlock {
    private Fonts defaultFont;
    private boolean dirty = true;
    float wrapWidth;
    private int defaultSize;
    private TextComponent[] textComponents;
    float lineSpacing = 2f;
    private float largestFontSize = 0;
    private int totalChars = 0;

    private ArrayList<RenderableTextComponent> renderableTextComponents = new ArrayList<>();

    public TextBlock(Fonts defaultFont, int defaultSize, TextComponent... textComponent) {
        this.defaultFont = defaultFont;
        this.defaultSize = defaultSize;
        this.textComponents = textComponent;
        this.wrapWidth = Float.MAX_VALUE;
    }

    public TextBlock(Fonts defaultFont, int defaultSize, float wrapWidth, TextComponent... textComponent) {
        this.defaultFont = defaultFont;
        this.defaultSize = defaultSize;
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
        boolean foundValidWhitespace = false;
        largestFontSize = 0;
        totalChars = 0;

        for (TextComponent component : textComponents) {
            StringBuilder sb = new StringBuilder();
            int lastWhiteSpaceIndex = 0;

            FreeTypeBitmapFontData fontData = component.getFontData(defaultFont, defaultSize);
            char[] chars = component.getText().toCharArray();
            totalChars += chars.length;
            for (int j = 0; j < chars.length; j++) {
                char c = chars[j];
                @Nullable BitmapFont.Glyph glyph = fontData.getGlyph(c);
                if (glyph != null) {
                    x += glyph.xadvance;
                }
                if (c == '\n') {
                    sb.append(component.getText(), lastWhiteSpaceIndex, j);
                    if (fontData.xHeight > largestFontSize) {
                        largestFontSize = fontData.xHeight;
                    }

                    renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                            component.isBold, component.isItalic, component.isUnderlined, component.isStrikethrough,
                            component.color, component.size.orElse(defaultSize), component.font.orElse(defaultFont)));
                    x = 0; //Go back to the start of the line
                    y -= largestFontSize * lineSpacing; //Move down a line
                    largestFontSize = 0; //Reset the largest font size because we are starting a new line

                    sb = new StringBuilder();
                    componentStartX = x;
                    componentStartY = y;
                    lastWhiteSpaceIndex = j;

                    foundValidWhitespace = false;

                    continue;
                } else if (Character.isWhitespace(c)) {
                    if (textWrapped) {
                        if (glyph != null) {
                            x -= glyph.xadvance;
                        }
                    } else {
                        sb.append(component.getText(), lastWhiteSpaceIndex, j);
                        if (fontData.xHeight > largestFontSize) {
                            largestFontSize = fontData.xHeight;
                        }
                        foundValidWhitespace = true;
                    }
                    lastWhiteSpaceIndex = j;
                } else {
                    if (textWrapped) {
                        lastWhiteSpaceIndex = j;
                        textWrapped = false;
                    }
                }

                if (x > wrapWidth && !Character.isWhitespace(c)) {
                    if (foundValidWhitespace) {
                        j = lastWhiteSpaceIndex;

                        renderableTextComponents.add(new RenderableTextComponent(sb + " ", componentStartX,
                                componentStartY,
                                component.isBold, component.isItalic, component.isUnderlined, component.isStrikethrough,
                                component.color, component.size.orElse(defaultSize), component.font.orElse(defaultFont)));

                        x = 0;
                        y -= largestFontSize * lineSpacing;
                        largestFontSize = 0;

                        componentStartX = x;
                        componentStartY = y;
                        textWrapped = true;
                        foundValidWhitespace = false;
                        sb = new StringBuilder();
                    } else {
                        sb.append(component.getText(), lastWhiteSpaceIndex, j);
                        if (fontData.xHeight > largestFontSize) {
                            largestFontSize = fontData.xHeight;
                        }
                        renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                                component.isBold, component.isItalic, component.isUnderlined, component.isStrikethrough,
                                component.color, component.size.orElse(defaultSize), component.font.orElse(defaultFont)));
                        x = 0;
                        y -= largestFontSize * lineSpacing;
                        largestFontSize = 0;

                        sb = new StringBuilder();
                        componentStartX = x;
                        componentStartY = y;
                        lastWhiteSpaceIndex = j;
                    }
                }
            }
            if (chars.length > lastWhiteSpaceIndex) {
                sb.append(component.getText(), lastWhiteSpaceIndex, chars.length);
                if (fontData.xHeight > largestFontSize) {
                    largestFontSize = fontData.xHeight;
                }
            }

            renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                    component.isBold, component.isItalic, component.isUnderlined, component.isStrikethrough, component.color,
                    component.size.orElse(defaultSize), component.font.orElse(defaultFont)));
            componentStartX = x;
            componentStartY = y;


            foundValidWhitespace = true;
            textWrapped = false;
        }

        dirty = false;
    }

    public Fonts getDefaultFont() {
        return defaultFont;
    }

    public void setDefaultFont(Fonts defaultFont) {
        setDirtyIfTrue(defaultFont != this.defaultFont);
        this.defaultFont = defaultFont;
    }

    public boolean isDirty() {
        return dirty;
    }

    public float getWrapWidth() {
        return wrapWidth;
    }

    public void setWrapWidth(float wrapWidth) {
        setDirtyIfTrue(wrapWidth != this.wrapWidth);
        this.wrapWidth = wrapWidth;
    }

    /**
     * @return the default font size
     */
    public int getDefaultSize() {
        return defaultSize;
    }

    /**
     * @return defaultSize * lineSpacing
     */
    public float getDefaultLineSpacingSize() {
        return defaultSize * lineSpacing;
    }

    /**
     * @param defaultSize the default font size
     */
    public void setDefaultSize(int defaultSize) {
        setDirtyIfTrue(defaultSize != this.defaultSize);
        this.defaultSize = defaultSize;
    }

    /**
     * @return a mutable list of all the text components that are used to create this text block. If this list is modified, {@link
     * #setDirty()} must be called.
     */
    public TextComponent[] getTextComponents() {
        return textComponents;
    }

    /*
     * @param textComponents Set the text components to use for this text block.
     */
    public void setTextComponents(TextComponent... textComponents) {
        setDirtyIfTrue(textComponents != this.textComponents);
        this.textComponents = textComponents;
    }

    /**
     * @return the line spacing. This value is multiplied by the font size to get the actual line spacing.
     */
    public float getLineSpacing() {
        return lineSpacing;
    }

    /**
     * @param lineSpacing the line spacing. This value is multiplied by the font size to get the actual line spacing.
     */
    public void setLineSpacing(float lineSpacing) {
        setDirtyIfTrue(lineSpacing != this.lineSpacing);
        this.lineSpacing = lineSpacing;
    }

    float getHeightCache = -1;

    /**
     * @return The height of this textblock
     */
    public float getHeight() {
        if (getHeightCache != -1) return getHeightCache;
        List<RenderableTextComponent> renderableTextComponents = getRenderableTextComponents();
        if (totalChars == 0) return getHeightCache = defaultSize + 2;
        return getHeightCache =
                (largestFontSize * lineSpacing) - renderableTextComponents.get(renderableTextComponents.size() - 1).y;
    }

    /**
     * Marks the data in this object as needing to be recalculated.
     */
    public void setDirty() {
        this.dirty = true;
        getPositionOfIndexCache.clear();
        getHeightCache = -1;
    }

    public void setDirtyIfTrue(boolean dirty) {
        if (dirty) setDirty();
    }

    Map<Integer, Vector2> getPositionOfIndexCache = new HashMap<>();

    /**
     * @param index of character to get
     * @return the position of the character at the given index
     */
    public Vector2 getPositionOfIndex(int index) {
        if (getPositionOfIndexCache.containsKey(index)) return getPositionOfIndexCache.get(index);

        List<RenderableTextComponent> renderableTextComponents = getRenderableTextComponents();
        int currentIndex = 0;
        for (int i = 0; i < renderableTextComponents.size(); i++) {
            RenderableTextComponent renderableTextComponent = renderableTextComponents.get(i);
            String text = renderableTextComponent.text;
            if (text.length() + currentIndex == index && renderableTextComponents.size() > i + 1
                    && Character.isWhitespace(text.charAt(text.length() - 1)) && text.charAt(text.length() - 1) != '\n') {
                //Make sure that the cursor shows up in the right location when the text wraps
                getPositionOfIndexCache.put(index, new Vector2(renderableTextComponent.x, renderableTextComponent.y));
                return getPositionOfIndexCache.get(index);
            }

            if (text.length() + currentIndex >= index) {
                float positionOffset = 0;
                if (index - currentIndex >= 0) {
                    String subString = text.substring(0, index - currentIndex);
                    for (char c : subString.toCharArray()) {
                        @Nullable BitmapFont.Glyph glyph = renderableTextComponent.getFontData().getGlyph(c);
                        if (glyph != null) {
                            positionOffset += glyph.xadvance;
                        }
                    }
                }

                getPositionOfIndexCache.put(index,
                        new Vector2(renderableTextComponent.x + positionOffset, renderableTextComponent.y));
                return getPositionOfIndexCache.get(index);
            } else {
                currentIndex += text.length();
            }
        }

        throw new IllegalArgumentException("Index " + index + " is out of bounds for max index " + currentIndex + " text " +
                "length " + textComponents[0].getText().length());
    }

    public void setTextInComponent(int index, String text) {
        if (textComponents[index].getText().equals(text)) return;
        textComponents[index].setText(text);
        setDirty();

    }
}
