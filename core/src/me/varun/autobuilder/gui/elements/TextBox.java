package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.events.scroll.InputEventListener;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextChangeListener;
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
    private final ShaderProgram fontShader;
    @NotNull
    private final BitmapFont font;
    @NotNull
    private final InputEventThrower eventThrower;
    private final boolean wrapText;
    @Nullable
    private final TextChangeListener textChangeListener;
    @NotNull
    private final GlyphLayout glyphLayout = new GlyphLayout();
    @NotNull
    protected String text;
    private boolean selected = false;
    private long nextFlashChange = 0;
    private boolean flashing = false;
    private int selectedPos = 0;

    public TextBox(@NotNull String text, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                   @NotNull InputEventThrower eventThrower, boolean wrapText, @Nullable TextChangeListener textChangeListener) {
        this.text = text;
        this.fontShader = fontShader;
        this.font = font;
        this.eventThrower = eventThrower;
        this.wrapText = wrapText;
        this.textChangeListener = textChangeListener;
        eventThrower.register(this);
    }


    //TODO: Fix Text Going outside the box and the entire cringe that this class is
    public void draw(@NotNull ShapeDrawer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX,
                     int drawStartY, int drawWidth, int drawHeight) {
        font.getData().setScale((drawHeight - 2) / 64f);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {

            if (Gdx.input.getX() > drawStartX && Gdx.input.getX() < drawStartX + drawWidth
                    && Gdx.graphics.getHeight() - Gdx.input.getY() > drawStartY - getHeight(drawWidth, drawHeight)
                    && Gdx.graphics.getHeight() - Gdx.input.getY() < drawStartY) {
                selected = true;

                //Checking where we click and setting the mouse cursor to the right pos (Y pos)
                int relativeMouseY = (int) ((drawStartY - (font.getData().xHeight) - 4) - (Gdx.graphics.getHeight() - Gdx.input.getY()));
                int row = (int) Math.floor(relativeMouseY / font.getData().xHeight);


                //Checking where we click and setting the mouse cursor to the right pos (X pos)
                int relativeMousePos = Gdx.input.getX() - drawStartX - 4;
                selectedPos = -1;
                if (relativeMousePos < 0) {
                    selectedPos = 0;
                } else {
                    float lastTextPos = 0;
                    for (int i = 0; i < (text + " ").length(); i++) {
                        glyphLayout.setText(font, (text + " "), 0, i, Color.BLACK, drawWidth - 8, -1, wrapText, null);
                        if (relativeMousePos < glyphLayout.width && glyphLayout.height > (font.getData().xHeight * row) + 4) {
                            if (glyphLayout.width - relativeMousePos > relativeMousePos - lastTextPos) {
                                selectedPos = i - 1;
                            } else {
                                selectedPos = i;
                            }

                            break;
                        }
                        lastTextPos = glyphLayout.width;
                    }
                    if (selectedPos == -1) {
                        if (text.length() > 0) {
                            if (glyphLayout.width - relativeMousePos > relativeMousePos - lastTextPos) {
                                selectedPos = text.length() - 1;
                            } else {
                                selectedPos = text.length();
                            }
                        } else {
                            selectedPos = 0;
                        }

                    }
                }

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

        glyphLayout.setText(font, text, 0, text.length(), Color.BLACK, drawWidth - 8, -1, wrapText, null);
        shapeRenderer.setColor(Color.WHITE);
        RoundedShapeRenderer.roundedRect(shapeRenderer, drawStartX, drawStartY - glyphLayout.height - 8, drawWidth, glyphLayout.height + 8, 2);


        spriteBatch.setShader(fontShader);
        font.draw(spriteBatch, text, drawStartX + 4, drawStartY - 4, drawWidth - 8, -1, wrapText);

        if (selected && flashing) {
            glyphLayout.setText(font, text, 0, selectedPos, Color.BLACK, drawWidth - 8, -1, wrapText, null);
            float cursorRenderPosY = glyphLayout.height;
            float cursorRenderPosX = 0;
            for (int i = 0; i < text.length(); i++) {
                glyphLayout.setText(font, text, i, selectedPos, Color.BLACK, drawWidth - 8, -1, wrapText, null);
                if (glyphLayout.height <= font.getData().xHeight * 2) {
                    cursorRenderPosX = glyphLayout.width;
                    break;
                }

            }
            font.draw(spriteBatch, "|", drawStartX + 4 + cursorRenderPosX, drawStartY + 4 + font.getData().xHeight - cursorRenderPosY);
        }

        spriteBatch.setShader(null);
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
        UndoHandler.getInstance().somethingChanged();
    }

    public float getHeight(int drawWidth, int drawHeight) {
        font.getData().setScale((drawHeight - 2) / 64f);
        glyphLayout.setText(font, text, 0, text.length(), Color.BLACK, drawWidth - 8, -1, wrapText, null);
        return glyphLayout.height + 8;
    }

    public @NotNull String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "TextBox{" +
                "text='" + text + '\'' +
                ", selectedPos=" + selectedPos +
                '}';
    }
}
