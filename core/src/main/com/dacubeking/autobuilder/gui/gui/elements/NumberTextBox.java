package com.dacubeking.autobuilder.gui.gui.elements;

import com.dacubeking.autobuilder.gui.events.input.TextChangeListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class NumberTextBox extends TextBox {
    public NumberTextBox(@NotNull String text, TextChangeListener textChangeListener, int fontSize) {
        super(text, true, textChangeListener, fontSize);
    }

    public NumberTextBox(@NotNull String text, boolean wrapText, @NotNull Consumer<TextBox> textChangeCallback,
                         @NotNull Function<TextBox, String> onTextBoxClickCallback, int fontSize) {
        super(text, wrapText, textChangeCallback, onTextBoxClickCallback, fontSize);
    }

    @Override
    public void onKeyType(char character) {
        if (Character.isDigit(character) || character == '.' || character == '-') {
            super.onKeyType(character);
        }
    }
}
