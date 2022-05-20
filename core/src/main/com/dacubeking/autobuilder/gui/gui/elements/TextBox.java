package com.dacubeking.autobuilder.gui.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.UndoHandler;
import com.dacubeking.autobuilder.gui.events.input.InputEventListener;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.events.input.TextChangeListener;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.textrendering.FontRenderer;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.scripting.util.LintingPos;
import com.dacubeking.autobuilder.gui.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.dacubeking.autobuilder.gui.util.MouseUtil.*;


/*
TODO: Still kinda ugly
 */
public class TextBox extends InputEventListener {
    @NotNull
    private final InputEventThrower eventThrower;
    private final boolean wrapText;
    @Nullable
    private final TextChangeListener textChangeListener;
    @NotNull
    protected String text;
    private boolean selected = false;
    private long nextFlashChange = 0;
    private boolean flashing = false;
    private int selectedPos = 0;
    private final int fontSize;
    TextBlock textBlock;
    private final TextBlock cursorTextBlock;
    private float xPos = -1;

    private static final String STOP_WORD_CHARS = ".?!,;:-+=()[]{}<>";

    Map<Integer, Boolean> keyPressedMap = new HashMap<>();
    Map<Integer, Long> nextKeyPressTimeMap = new HashMap<>();

    ArrayList<TextComponent> textComponents = new ArrayList<>();

    private static final long KEY_PRESS_DELAY = 25;
    private static final long INITIAL_KEY_PRESS_DELAY = 400;

    public TextBox(@NotNull String text, @NotNull InputEventThrower eventThrower, boolean wrapText,
                   @Nullable TextChangeListener textChangeListener, int fontSize) {
        this.text = text;
        this.eventThrower = eventThrower;
        this.wrapText = wrapText;
        this.textChangeListener = textChangeListener;
        this.fontSize = fontSize;
        textBlock = new TextBlock(Fonts.JETBRAINS_MONO, fontSize, 350, new TextComponent(text).setColor(Color.BLACK));
        cursorTextBlock = new TextBlock(Fonts.JETBRAINS_MONO, fontSize, new TextComponent("|").setColor(Color.BLACK));
        eventThrower.register(this);
    }


    //TODO: Fix Text Going outside the box

    /**
     * @return if the mouse is hovering over the textbox
     */
    public boolean draw(@NotNull ShapeDrawer shapeRenderer, @NotNull Batch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, @Nullable ArrayList<LintingPos> linting) {
        textBlock.update();

        boolean hovering = getMouseX() > drawStartX && getMouseX() < drawStartX + drawWidth
                && getMouseY() > drawStartY - getHeight() + 8
                && getMouseY() < drawStartY + 4;

        Vector2 mousePos = new Vector2(getMouseX() - (drawStartX + 4),
                (getMouseY()) - (drawStartY - textBlock.getDefaultSize() + 4));
        int mouseIndexPos = textBlock.getIndexOfPosition(mousePos);
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (hovering) {
                selected = true;
                text = fireTextBoxClickEvent();

                selectedPos = mouseIndexPos;
                highlighting = false;
                highlightPosBegin = mouseIndexPos;
                highlightPosEnd = mouseIndexPos;
                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 500;
                AutoBuilder.scheduleRendering(500);
                xPos = -1;
            } else {
                selected = false;
                highlighting = false;
            }
        } else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (selected) {
                highlightPosEnd = mouseIndexPos;
                selectedPos = mouseIndexPos;
                if (highlightPosBegin - highlightPosEnd != 0) {
                    highlighting = true;
                }
                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 500;
                AutoBuilder.scheduleRendering(500);
            }
        }

        if (selected) {
            if (getKeyPressed(Keys.RIGHT)) {
                if (!highlighting) highlightPosBegin = selectedPos;

                selectedPos++;
                if (selectedPos > text.length()) {
                    selectedPos = text.length();
                } else if (isControlPressed()) {
                    boolean foundNonWhitespace = !Character.isWhitespace(text.charAt(selectedPos - 1))
                            || STOP_WORD_CHARS.contains(String.valueOf(text.charAt(selectedPos - 1)));
                    while (selectedPos < text.length() &&
                            (!(Character.isWhitespace(text.charAt(selectedPos))
                                    || STOP_WORD_CHARS.contains(String.valueOf(text.charAt(selectedPos - 1))))
                                    || !foundNonWhitespace)) {
                        if (!Character.isWhitespace(text.charAt(selectedPos))) foundNonWhitespace = true;
                        selectedPos++;
                    }
                }

                highlightPosEnd = selectedPos;
                highlighting = isShiftPressed();

                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 500;
                AutoBuilder.scheduleRendering(500);
                xPos = -1;
            }

            if (getKeyPressed(Keys.LEFT)) {
                if (!highlighting) highlightPosBegin = selectedPos;

                selectedPos--;
                if (selectedPos < 0) {
                    selectedPos = 0;
                } else if (isControlPressed()) {
                    boolean foundNonWhitespace = !Character.isWhitespace(text.charAt(selectedPos))
                            || STOP_WORD_CHARS.contains(String.valueOf(text.charAt(selectedPos)));
                    while (selectedPos > 0 &&
                            (!(Character.isWhitespace(text.charAt(selectedPos - 1))
                                    || STOP_WORD_CHARS.contains(String.valueOf(text.charAt(selectedPos - 1))))
                                    || !foundNonWhitespace)) {
                        if (!Character.isWhitespace(text.charAt(selectedPos - 1))) foundNonWhitespace = true;
                        selectedPos--;
                    }
                }

                highlightPosEnd = selectedPos;
                highlighting = isShiftPressed();

                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 500;
                AutoBuilder.scheduleRendering(500);
                xPos = -1;
            }

            //Act like we click up on the previous line
            if (getKeyPressed(Keys.UP)) {
                if (!highlighting) highlightPosBegin = selectedPos;

                Vector2 pos = textBlock.getPositionOfIndex(selectedPos);
                if (xPos == -1) xPos = pos.x;
                selectedPos = textBlock.getIndexOfPosition(new Vector2(xPos, pos.y + textBlock.getDefaultLineSpacingSize() - 1));

                highlightPosEnd = selectedPos;
                highlighting = isShiftPressed();

                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 500;
                AutoBuilder.scheduleRendering(500);
            }

            //Act like we click down on the next line
            if (getKeyPressed(Keys.DOWN)) {
                if (!highlighting) highlightPosBegin = selectedPos;

                Vector2 pos = textBlock.getPositionOfIndex(selectedPos);
                if (xPos == -1) xPos = pos.x;
                selectedPos = textBlock.getIndexOfPosition(new Vector2(xPos, pos.y - textBlock.getDefaultLineSpacingSize() / 2));

                highlightPosEnd = selectedPos;
                highlighting = isShiftPressed();

                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 500;
                AutoBuilder.scheduleRendering(500);
            }

            if (getKeyPressed(Keys.BACKSPACE)) {
                if (highlighting) {
                    deleteHighlightedSection();
                    fireTextChangeEvent();
                    UndoHandler.getInstance().somethingChanged();
                } else if (selectedPos > 0) {
                    text = text.substring(0, selectedPos - 1) + text.substring(selectedPos);
                    selectedPos--;
                    fireTextChangeEvent();
                    UndoHandler.getInstance().somethingChanged();
                }

                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 1500;
                AutoBuilder.scheduleRendering(500);
                xPos = -1;
            }

            if (getKeyPressed(Keys.FORWARD_DEL)) {
                if (highlighting) {
                    deleteHighlightedSection();
                    fireTextChangeEvent();
                    UndoHandler.getInstance().somethingChanged();
                } else if (selectedPos < text.length()) {
                    text = text.substring(0, selectedPos) + text.substring(selectedPos + 1);
                    fireTextChangeEvent();
                    UndoHandler.getInstance().somethingChanged();
                }

                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 1500;
                AutoBuilder.scheduleRendering(500);
                xPos = -1;
            }

            if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) &&
                    Gdx.input.isKeyJustPressed(Input.Keys.V)) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                try {
                    String clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
                    for (int i = 0; i < clipboardText.length(); i++) {
                        onKeyType(clipboardText.charAt(i));
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    System.out.println("bad Clipboard data");
                }
            }

            if (isControlPressed() && (Gdx.input.isKeyJustPressed(Input.Keys.C) || Gdx.input.isKeyJustPressed(Input.Keys.X))) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                int endPos = selectedPos;
                int startPos = selectedPos;
                if (highlighting) {
                    endPos = Math.max(highlightPosEnd, highlightPosBegin);
                    startPos = Math.min(highlightPosEnd, highlightPosBegin);
                } else {
                    while (endPos < text.length() && text.charAt(endPos) != '\n') {
                        endPos++;
                    }
                    if (endPos < text.length() && text.charAt(endPos) == '\n') endPos += 1; // Add the newline to the copied text

                    while (startPos > 0 && text.charAt(startPos - 1) != '\n') {
                        startPos--;
                    }
                }

                clipboard.setContents(new StringSelection(text.substring(startPos, endPos)), null);

                if (Gdx.input.isKeyPressed(Input.Keys.X)) {
                    text = text.substring(0, startPos) + text.substring(endPos);
                    selectedPos = startPos;
                    highlighting = false;
                    fireTextChangeEvent();
                    UndoHandler.getInstance().somethingChanged();
                }
            }

            if (nextFlashChange < System.currentTimeMillis()) {
                flashing = !flashing;
                nextFlashChange = System.currentTimeMillis() + 500;
                AutoBuilder.scheduleRendering(500);
            }

            long nextFlashTime = nextFlashChange - System.currentTimeMillis();
            if (nextFlashTime > 0) {
                AutoBuilder.scheduleRendering(nextFlashTime);
            }
        }

        LintingPos lintingPos = null;
        if (linting != null) {
            textComponents.clear();
            if (linting.size() > 0) {
                textComponents.addAll(
                        addHighlight(new TextComponent(text.substring(0, linting.get(0).index)).setUnderlined(false),
                                0, linting.get(0).index));
                for (int i = 0; i < linting.size(); i++) {
                    if (linting.size() > i + 1 && linting.get(i + 1).index < text.length()) {
                        // Lint in between this error and the next error
                        if (mouseIndexPos >= linting.get(i).index && mouseIndexPos < linting.get(i + 1).index) {
                            lintingPos = linting.get(i);
                        }
                        textComponents.addAll(
                                addHighlight(new TextComponent(
                                                text.substring(linting.get(i).index, linting.get(i + 1).index))
                                                .setUnderlined(true).setUnderlineColor(linting.get(i).underlineColor)
                                                .setColor(linting.get(i).color),
                                        linting.get(i).index, linting.get(i + 1).index));
                    } else {
                        //Lint the rest of the text
                        if (linting.get(i).index < text.length()) {
                            textComponents.addAll(
                                    addHighlight(new TextComponent(text.substring(linting.get(i).index))
                                                    .setUnderlined(true).setUnderlineColor(linting.get(i).underlineColor)
                                                    .setColor(linting.get(i).color),
                                            linting.get(i).index, text.length()));
                        }

                        if (mouseIndexPos >= linting.get(i).index) {
                            lintingPos = linting.get(i);
                        }
                    }
                }

                // TODO: Change the fixed values to be based of the font
                if (lintingPos != null && lintingPos.message != null &&
                        Math.abs(textBlock.getPositionOfIndex(mouseIndexPos).x - mousePos.x) < 7 &&
                        Math.abs(textBlock.getPositionOfIndex(mouseIndexPos).y - mousePos.y) < 20) {
                    if (!highlighting) {
                        HoverManager.setHoverText(lintingPos.message); // TODO: Render this at a fixed position above the text
                        // instead of being relative to the mouse
                    }
                }
            } else {
                textComponents.addAll(0, addHighlight(new TextComponent(text), 0, text.length()));
            }

            textBlock.setTextComponents(textComponents.toArray(new TextComponent[0]));
        } else {
            textBlock.setTextInComponent(0, text);
        }

        RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX, drawStartY - textBlock.getHeight(), drawWidth,
                textBlock.getHeight() + 8, 2, Color.WHITE);

        FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + 4, drawStartY - textBlock.getDefaultSize() + 4,
                textBlock);

        if (selected && flashing) {
            if (selectedPos >= 0 && textBlock.getRenderableTextComponents().size() > 0) {
                Vector2 cursorPos = textBlock.getPositionOfIndex(selectedPos);
                FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX + cursorPos.x,
                        drawStartY + textBlock.getDefaultSize() - textBlock.getDefaultLineSpacingSize() + 5 + cursorPos.y,
                        cursorTextBlock);
            } else {
                FontRenderer.renderText(spriteBatch, shapeRenderer, drawStartX,
                        drawStartY + textBlock.getDefaultSize() - textBlock.getDefaultLineSpacingSize() + 5,
                        cursorTextBlock);
            }
        }
        return hovering;
    }

    int highlightPosBegin = 3;
    int highlightPosEnd = 10;
    boolean highlighting = false;

    private final static Color HIGHLIGHT_COLOR = Color.valueOf("a6d2ffff");

    private Collection<TextComponent> addHighlight(TextComponent textComponent, int startIndex, int endIndex) {

        int highlightPosMin = Math.min(highlightPosBegin, highlightPosEnd);
        int highlightPosMax = Math.max(highlightPosBegin, highlightPosEnd);
        Collection<TextComponent> textComponents = new ArrayList<>();
        if (!highlighting) {
            textComponents.add(textComponent);
            return textComponents;
        }

        if (startIndex < highlightPosMax && endIndex > highlightPosMin) {
            if (highlightPosMin > startIndex) {
                TextComponent sub1 = textComponent.clone();
                sub1.setText(sub1.text.substring(0, highlightPosMin - startIndex));
                sub1.setHighlighted(false);
                textComponents.add(sub1);

                TextComponent sub2 = textComponent.clone();
                sub2.setText(sub2.text.substring(highlightPosMin - startIndex));
                sub2.setHighlighted(true).setHighlightColor(HIGHLIGHT_COLOR);
                startIndex = highlightPosMin;
                textComponent = sub2;
            } else {
                textComponent.setHighlighted(true).setHighlightColor(HIGHLIGHT_COLOR);
            }
        }

        if (startIndex <= highlightPosMax && endIndex > highlightPosMax) {
            TextComponent sub1 = textComponent.clone();
            sub1.setText(sub1.text.substring(0, highlightPosMax - startIndex));
            sub1.setHighlighted(true).setHighlightColor(HIGHLIGHT_COLOR);
            textComponents.add(sub1);

            textComponent.setText(textComponent.text.substring(highlightPosMax - startIndex));
            textComponent.setHighlighted(false);
            textComponents.add(textComponent);
        } else {
            textComponents.add(textComponent);
        }
        return textComponents;
    }

    public void dispose() {
        eventThrower.unRegister(this);
    }

    @Override
    public void onKeyType(char character) {
        if (selected) {
            if (Character.getType(character) != Character.CONTROL) {
                deleteHighlightedSection();

                if (text.length() == selectedPos) {
                    text = text + character;
                } else {
                    text = text.substring(0, selectedPos) + character + text.substring(selectedPos);
                }
                selectedPos++;
                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 1500;
                AutoBuilder.scheduleRendering(500);
                fireTextChangeEvent();
                UndoHandler.getInstance().somethingChanged();
                xPos = -1;
            } else if (Character.getName(character).equals("LINE FEED (LF)")) { //TODO: Make this not cringe
                deleteHighlightedSection();
                if (text.length() == selectedPos) {
                    text = text + '\n';
                } else {
                    text = text.substring(0, selectedPos) + '\n' + text.substring(selectedPos);
                }
                selectedPos++;
                flashing = true;
                nextFlashChange = System.currentTimeMillis() + 1500;
                AutoBuilder.scheduleRendering(1500);
                fireTextChangeEvent();
                UndoHandler.getInstance().somethingChanged();
                xPos = -1;
            }
        }
    }

    private void deleteHighlightedSection() {
        if (highlighting) {
            int highlightPosMin = Math.min(highlightPosBegin, highlightPosEnd);
            int highlightPosMax = Math.max(highlightPosBegin, highlightPosEnd);
            text = text.substring(0, highlightPosMin) + text.substring(highlightPosMax);
            selectedPos = highlightPosMin;
            highlighting = false;
        }
    }

    protected void fireTextChangeEvent() {
        assert textChangeListener != null;
        textChangeListener.onTextChange(text, this);
    }

    protected String fireTextBoxClickEvent() {
        assert textChangeListener != null;
        return textChangeListener.onTextBoxClick(text, this);
    }

    public float getHeight() {
        return textBlock.getHeight() + 8;
    }

    public @NotNull String getText() {
        return text;
    }

    /**
     * NOTE: If the textbox is selected the value that is set in this function will be ignored
     *
     * @param text text to set
     */
    public void setText(@NotNull String text) {
        if (!this.selected) {
            this.text = text;
        }
    }

    @Override
    public String toString() {
        return "TextBox{" +
                "text='" + text + '\'' +
                ", selectedPos=" + selectedPos +
                '}';
    }

    public boolean getKeyPressed(int keyCode) {
        if (Gdx.input.isKeyPressed(keyCode)) {
            if (keyPressedMap.get(keyCode) == null || !keyPressedMap.get(keyCode)) { // Key just pressed
                keyPressedMap.put(keyCode, true);
                nextKeyPressTimeMap.put(keyCode, System.currentTimeMillis() + INITIAL_KEY_PRESS_DELAY);
                return true;
            }

            if (System.currentTimeMillis() > nextKeyPressTimeMap.get(keyCode)) { // Key held down
                nextKeyPressTimeMap.put(keyCode, System.currentTimeMillis() + KEY_PRESS_DELAY);
                for (Long value : nextKeyPressTimeMap.values()) {
                    AutoBuilder.scheduleRendering(value - System.currentTimeMillis());
                }
                return true;
            }
        } else { // Key released
            keyPressedMap.put(keyCode, false);
        }
        return false;
    }
}
