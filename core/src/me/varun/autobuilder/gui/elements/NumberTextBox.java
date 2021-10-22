package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.NumberTextboxChangeListener;
import org.jetbrains.annotations.NotNull;

public class NumberTextBox extends TextBox {
    @NotNull
    private final NumberTextboxChangeListener numberTextboxChangeListener;
    private final int row;
    private final int column;
    private final UndoHandler undoHandler = UndoHandler.getInstance();

    public NumberTextBox(@NotNull String text, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                         @NotNull InputEventThrower eventThrower, @NotNull NumberTextboxChangeListener numberTextboxChangeListener, int row, int column) {
        super(text, fontShader, font, eventThrower, false, null);
        this.numberTextboxChangeListener = numberTextboxChangeListener;
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
        numberTextboxChangeListener.onTextChange(text, row, column, this);
        undoHandler.somethingChanged();
    }

    @Override
    protected void fireTextBoxClickEvent() {
        numberTextboxChangeListener.onTextBoxClick(text, row, column, this);
        undoHandler.somethingChanged();
    }
}
