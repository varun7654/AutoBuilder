package com.dacubeking.autobuilder.gui.gui.elements;

import com.dacubeking.autobuilder.gui.events.input.TextChangeListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class IntegerNumberTextBox extends NumberTextBox {


    public IntegerNumberTextBox(@NotNull String text, TextChangeListener textChangeListener, int fontSize) {
        super(text, textChangeListener, fontSize);
    }

    public IntegerNumberTextBox(@NotNull String text, boolean wrapText, @NotNull Consumer<TextBox> textChangeCallback,
                                @NotNull Function<TextBox, String> onTextBoxClickCallback, int fontSize) {
        super(text, wrapText, textChangeCallback, onTextBoxClickCallback, fontSize);
    }

    @Override
    public void onKeyType(char character) {
        if (Character.isDigit(character) || character == '-') {
            super.onKeyType(character);
        }
    }
}
