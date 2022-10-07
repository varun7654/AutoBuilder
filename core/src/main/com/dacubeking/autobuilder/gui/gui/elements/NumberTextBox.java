package com.dacubeking.autobuilder.gui.gui.elements;

import com.dacubeking.autobuilder.gui.events.input.TextChangeListener;
import org.jetbrains.annotations.NotNull;

public class NumberTextBox extends TextBox {
    public NumberTextBox(@NotNull String text, TextChangeListener textChangeListener, int fontSize) {
        super(text, true, textChangeListener, fontSize);
    }

    @Override
    public void onKeyType(char character) {
        if (Character.isDigit(character) || character == '.' || character == '-') {
            super.onKeyType(character);
        }
    }
}
