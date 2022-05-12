package com.dacubeking.autobuilder.gui.gui.elements;

import com.dacubeking.autobuilder.gui.UndoHandler;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.events.input.NumberTextboxChangeListener;
import org.jetbrains.annotations.NotNull;

public class NumberTextBox extends TextBox {
    @NotNull
    private final NumberTextboxChangeListener numberTextboxChangeListener;
    private final int row;
    private final int column;
    private final UndoHandler undoHandler = UndoHandler.getInstance();

    public NumberTextBox(@NotNull String text, @NotNull InputEventThrower eventThrower,
                         @NotNull NumberTextboxChangeListener numberTextboxChangeListener, int row, int column, int fontSize) {
        super(text, eventThrower, false, null, fontSize);
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
    }

    @Override
    protected String fireTextBoxClickEvent() {
        return numberTextboxChangeListener.onTextBoxClick(text, row, column, this);
    }
}
