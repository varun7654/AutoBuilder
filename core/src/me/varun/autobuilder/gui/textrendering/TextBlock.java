package me.varun.autobuilder.gui.textrendering;


import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A text block is a collection of TextComponent objects.
 */
public class TextBlock {
    @NotNull private Fonts defaultFont;
    private boolean dirty = true;
    float wrapWidth;
    private int defaultSize;
    @NotNull private TextComponent[] textComponents;
    float lineSpacing = 2f;
    private float largestFontSize = 0;
    private int totalChars = 0;

    @NotNull private final ArrayList<RenderableTextComponent> renderableTextComponents = new ArrayList<>();

    public TextBlock(@NotNull Fonts defaultFont, int defaultSize, TextComponent... textComponent) {
        this.defaultFont = defaultFont;
        this.defaultSize = defaultSize;
        this.textComponents = textComponent;
        this.wrapWidth = Float.MAX_VALUE;
    }

    public TextBlock(@NotNull Fonts defaultFont, int defaultSize, float wrapWidth, TextComponent... textComponent) {
        this.defaultFont = defaultFont;
        this.defaultSize = defaultSize;
        this.textComponents = textComponent;
        this.wrapWidth = wrapWidth;
    }

    /**
     * Gets the text {@link RenderableTextComponent}s that are used to create this text block.
     *
     * @return a list of {@link RenderableTextComponent}s
     */
    public @NotNull ArrayList<RenderableTextComponent> getRenderableTextComponents() {
        updateIfDirty();
        return renderableTextComponents;
    }

    /**
     * Updates the text block if it is dirty.
     */
    public void updateIfDirty() {
        if (dirty) {
            update();
        }
    }

    /**
     * Updates the text block. Converts the {@link  TextComponent}s into the {@link RenderableTextComponent}s that are used to
     * render
     */
    public void update() {
        renderableTextComponents.clear();
        float x = 0, y = 0;
        float componentStartX = 0, componentStartY = 0;
        boolean textWrapped = false;
        boolean foundValidWhitespace = false;
        largestFontSize = 0;
        totalChars = 0;

        for (TextComponent component : textComponents) { //Iterate through each text component
            StringBuilder sb = new StringBuilder(); //Tempbuffer to hold the text that will be placed into the next component
            int lastWhiteSpaceIndex = 0; //Index of the last whitespace character. Used to tell where to line wrap

            FreeTypeBitmapFontData fontData = component.getFontData(defaultFont,
                    defaultSize); //Get the font data for the current component
            char[] chars = component.getText().toCharArray();
            totalChars += chars.length; //Add the number of characters to the total number of characters
            for (int j = 0; j < chars.length; j++) { //Iterate through each character in the text component
                char c = chars[j]; //Get the current character
                @Nullable BitmapFont.Glyph glyph = fontData.getGlyph(c); //Get the character data for the current character
                if (glyph != null) { // Certain characters (like \n) don't have glyphs
                    x += glyph.xadvance; //Move the positions of the next RenderableTextComponent to the right by the width
                }

                if (c == '\n') { // Explicit line break
                    sb.append(component.getText(), lastWhiteSpaceIndex, j); // Add all the remaining characters to the buffer
                    if (fontData.xHeight > largestFontSize) { // One final check to see if the current font size is larger than the largest recorded font size on this line
                        largestFontSize = fontData.xHeight;
                    }

                    renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                            component.isBold, component.isItalic, component.isUnderlined, component.isStrikethrough,
                            component.color, component.size.orElse(defaultSize), component.font.orElse(defaultFont)));

                    x = 0; // Go back to the start of the line
                    y -= largestFontSize * lineSpacing; //Move down a line
                    largestFontSize = 0; // Reset the largest font size because we are starting a new line

                    sb = new StringBuilder(); // Reset the buffer
                    componentStartX = x; // Set the x position of the next component to the current x position
                    componentStartY = y; // Set the y position of the next component to the current y position
                    lastWhiteSpaceIndex = j; // Set the last whitespace index to the current index

                    foundValidWhitespace = true; // Set valid whitespace to true because we don't want to mess with spacing
                    // after an explicit line break
                    textWrapped = false; // Explicit line breaks don't count as text wrapping

                    continue;
                } else if (Character.isWhitespace(c)) {
                    sb.append(component.getText(), lastWhiteSpaceIndex, j);
                    if (fontData.xHeight > largestFontSize) {
                        largestFontSize = fontData.xHeight;
                    }
                    foundValidWhitespace = true;

                    lastWhiteSpaceIndex = j;
                } else {
                    if (textWrapped) { // If this is the first character after the text has been implicitly wrapped
                        lastWhiteSpaceIndex = j; // Set the last whitespace index to the current index to avoid an extra space
                        // at the beginning of the line
                        textWrapped = false;
                    }
                }

                if (x > wrapWidth && !Character.isWhitespace(c)) {
                    if (foundValidWhitespace) { // If there is a space on this line we can wrap the text at the space
                        j = lastWhiteSpaceIndex; // Reset the index to the last whitespace index

                        // Add the buffer (which contains the text up to the last whitespace) to the list of renderable
                        renderableTextComponents.add(new RenderableTextComponent(sb + " ", componentStartX, componentStartY,
                                component.isBold, component.isItalic, component.isUnderlined, component.isStrikethrough,
                                component.color, component.size.orElse(defaultSize), component.font.orElse(defaultFont)));

                        x = 0; // Reset the x position
                        y -= largestFontSize * lineSpacing; // Move down a line
                        largestFontSize = 0; // Reset the largest font size because we are starting a new line

                        componentStartX = x; // Set the x position of the next component to the current x position
                        componentStartY = y; // Set the y position of the next component to the current y position
                        textWrapped = true; // Set to true because we implicitly wrapped the text at the last whitespace
                        foundValidWhitespace = false; // Reset the found valid whitespace because we are starting a new line
                        sb = new StringBuilder(); // Reset the buffer
                    } else { // If there is no space on this line we can't wrap the text at a space. We have to wrap the text at the end of the line in the middle of a word
                        sb.append(component.getText(), lastWhiteSpaceIndex, j); // Add the text from the last whitespace to the
                        // current index to the buffer

                        if (fontData.xHeight > largestFontSize) { // One last check to see if the current font size is the
                            // largest font size in the line. If it is, we use it as the largest font size for the line
                            largestFontSize = fontData.xHeight;
                        }
                        // Create a new renderable text component with the text from the buffer
                        renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                                component.isBold, component.isItalic, component.isUnderlined, component.isStrikethrough,
                                component.color, component.size.orElse(defaultSize), component.font.orElse(defaultFont)));
                        x = 0; // Reset the x position
                        y -= largestFontSize * lineSpacing; // Move down a line
                        largestFontSize = 0; // Reset the largest font size because we are starting a new line

                        sb = new StringBuilder(); // Reset the buffer
                        componentStartX = x; // Set the x position of the next component to the current x position
                        componentStartY = y; // Set the y position of the next component to the current y position
                        lastWhiteSpaceIndex = j; // Set the last whitespace index to the current index to avoid duplicating stuff
                        textWrapped = false; // Set to false because we didn't wrap the text at the last whitespace
                    }
                }
            }
            // We reached the end of the textComponent. Add the remaining text to a renderable text component
            if (chars.length > lastWhiteSpaceIndex) { // If there is text left that still needs to be added to the buffer
                sb.append(component.getText(), lastWhiteSpaceIndex,
                        chars.length); // Add the text from the last whitespace to the current index to the buffer

                if (fontData.xHeight > largestFontSize) { // One last check to see if the current font size is the
                    // largest font size in the line. If it is, we use it as the largest font size for the line
                    largestFontSize = fontData.xHeight;
                }
            }

            // Create a new renderable text component with the text from the buffer
            renderableTextComponents.add(new RenderableTextComponent(sb.toString(), componentStartX, componentStartY,
                    component.isBold, component.isItalic, component.isUnderlined, component.isStrikethrough, component.color,
                    component.size.orElse(defaultSize), component.font.orElse(defaultFont)));

            componentStartX = x; // Set the x position of the next component to the current x position
            componentStartY = y; // Set the y position of the next component to the current y position


            foundValidWhitespace = true; // Set to true because it's easier to just allow the text to wrap at the end of a
            // component than to try and wrap at a point inside the previous component.

            textWrapped = false; // Set to false because the text has not wrapped yet (text wrapping can only happen inside the loop)
        }

        dirty = false;
    }

    /**
     * @return the default font for this textBlock
     */
    public @NotNull Fonts getDefaultFont() {
        return defaultFont;
    }

    public void setDefaultFont(Fonts defaultFont) {
        setDirtyIfTrue(defaultFont != this.defaultFont);
        this.defaultFont = defaultFont;
    }

    /**
     * @return is the dirty flag set to true
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * @return the width that text will be wrapped at
     */
    public float getWrapWidth() {
        return wrapWidth;
    }

    /**
     * @param wrapWidth set the width that text will be wrapped at
     */
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

                getPositionOfIndexCache.put(index, new Vector2(renderableTextComponent.x + positionOffset,
                        renderableTextComponent.y));
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
