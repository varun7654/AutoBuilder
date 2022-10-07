package com.dacubeking.autobuilder.gui.gui.elements;

import com.dacubeking.autobuilder.gui.events.input.NumberTextboxChangeListener;
import org.jetbrains.annotations.NotNull;

public class PositionedNumberTextBox extends NumberTextBox {
    @NotNull
    private final NumberTextboxChangeListener numberTextboxChangeListener;
    private final int row;
    private final int column;

    public PositionedNumberTextBox(@NotNull String text,
                                   @NotNull NumberTextboxChangeListener numberTextboxChangeListener,
                                   int row,
                                   int column,
                                   int fontSize) {
        super(text, null, fontSize);
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
