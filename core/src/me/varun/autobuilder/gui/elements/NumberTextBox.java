package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextPositionChangeListener;
import org.jetbrains.annotations.NotNull;

public class NumberTextBox extends TextBox {
    @NotNull
    private final TextPositionChangeListener textPositionChangeListener;
    private final int row;
    private final int column;
    private final UndoHandler undoHandler = UndoHandler.getInstance();

    public NumberTextBox(@NotNull String text, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                         @NotNull InputEventThrower eventThrower, @NotNull TextPositionChangeListener textPositionChangeListener, int row, int column) {
        super(text, fontShader, font, eventThrower, false, null);
        this.textPositionChangeListener = textPositionChangeListener;
        this.row = row;
        this.column = column;
    }

    @Override
    public void onKeyType(char character) {
        if (Character.isDigit(character) || character == '.' || character == '-') {
            super.onKeyType(character);
        }
    }

    @Override
    protected void fireTextChangeEvent() {
        textPositionChangeListener.onTextChange(text, row, column, this);
        undoHandler.somethingChanged();
    }
}
