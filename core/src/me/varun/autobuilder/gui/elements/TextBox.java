package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextChangeListener;
import me.varun.autobuilder.gui.textrendering.FontRenderer;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextBlock;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Calendar;


/*
TODO: FIX
This class is barely working.
Code should be cleaned up so that it works
Known Bugs:
Making new lines without pressing the enter key breaks things (making text wrap)
Can't hold backspace
can't press up arrow to go up a line
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

    public TextBox(@NotNull String text, @NotNull InputEventThrower eventThrower, boolean wrapText,
                   @Nullable TextChangeListener textChangeListener, int fontSize) {
        this.text = text;
        this.eventThrower = eventThrower;
        this.wrapText = wrapText;
        this.textChangeListener = textChangeListener;
        this.fontSize = fontSize;
        textBlock = new TextBlock(Fonts.JETBRAINS_MONO, fontSize, 350,
                new TextComponent(text).setColor(Color.BLACK));
        eventThrower.register(this);
    }


    //TODO: Fix Text Going outside the box and the entire cringe that this class is
    public void draw(@NotNull ShapeDrawer shapeRenderer, @NotNull Batch spriteBatch, float drawStartX,
                     float drawStartY, float drawWidth) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (Gdx.input.getX() > drawStartX && Gdx.input.getX() < drawStartX + drawWidth
                    && Gdx.graphics.getHeight() - Gdx.input.getY() > drawStartY - getHeight()
                    && Gdx.graphics.getHeight() - Gdx.input.getY() < drawStartY) {
                selected = true;
                text = fireTextBoxClickEvent();

                //TODO: Find where the mouse clicked and set the selectedPos to that

                flashing = true;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
            } else {
                selected = false;
            }
        }

        if (selected) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedPos++;
                if (selectedPos > text.length()) {
                    selectedPos = text.length();
                }
                flashing = true;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedPos--;
                if (selectedPos < 0) {
                    selectedPos = 0;
                }
                flashing = true;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                if (selectedPos > 0) {
                    text = text.substring(0, selectedPos - 1) + text.substring(selectedPos);
                    selectedPos--;
                    fireTextChangeEvent();
                }
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

            if (nextFlashChange < Calendar.getInstance().getTimeInMillis()) {
                flashing = !flashing;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
            }
        }

        textBlock.setTextInComponent(0, text);

        textBlock.setLineSpacing(2);

        RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX, drawStartY - textBlock.getHeight(), drawWidth,
                textBlock.getHeight() + 8, 2, Color.WHITE);

        FontRenderer.renderText(spriteBatch, drawStartX + 4, drawStartY - textBlock.getDefaultSize() + 4, textBlock);

        if (selected && flashing) {
            textBlock.updateIfDirty();
            if (selectedPos >= 0 && textBlock.getRenderableTextComponents().size() > 0) {
                Vector2 cursorPos = textBlock.getPositionOfIndex(selectedPos);
                FontRenderer.renderText(spriteBatch, drawStartX + 1 + cursorPos.x,
                        drawStartY + textBlock.getDefaultSize() - textBlock.getDefaultLineSpacingSize() + 5 + cursorPos.y,
                        Fonts.JETBRAINS_MONO, fontSize, new TextComponent("|").setColor(Color.BLACK));
            } else {
                FontRenderer.renderText(spriteBatch, drawStartX + 1,
                        drawStartY + textBlock.getDefaultSize() - textBlock.getDefaultLineSpacingSize() + 5,
                        Fonts.JETBRAINS_MONO, fontSize, new TextComponent("|").setColor(Color.BLACK));
            }

        }
    }

    public void dispose() {
        eventThrower.unRegister(this);
    }

    @Override
    public void onKeyType(char character) {
        if (selected) {
            if (Character.getType(character) != Character.CONTROL) {
                if (text.length() == selectedPos) {
                    text = text + character;
                } else {
                    text = text.substring(0, selectedPos) + character + text.substring(selectedPos);
                }
                selectedPos++;
                flashing = true;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
                fireTextChangeEvent();
            } else if (Character.getName(character).equals("CARRIAGE RETURN (CR)")) { //TODO: Make this not cringe
                if (text.length() == selectedPos) {
                    text = text + '\n';
                } else {
                    text = text.substring(0, selectedPos) + '\n' + text.substring(selectedPos);
                }
                selectedPos++;
                flashing = true;
                nextFlashChange = Calendar.getInstance().getTimeInMillis() + 500;
                fireTextChangeEvent();
            }

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
    public  void setText(@NotNull String text) {
        if(!this.selected){
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
}
