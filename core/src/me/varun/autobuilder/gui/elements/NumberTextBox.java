package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.events.textchange.TextChangeListener;
import org.jetbrains.annotations.NotNull;

public class NumberTextBox extends TextBox{
    private int row;
    private int column;

    public NumberTextBox(@NotNull String text, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                         @NotNull InputEventThrower eventThrower, @NotNull TextChangeListener textChangeListener, int row, int column ) {
        super(text, fontShader, font, eventThrower, textChangeListener);
        this.row = row;
        this.column = column;
    }

    @Override
    public void onKeyType(char character) {
        if(Character.isDigit(character) || character == '.'){
           super.onKeyType(character);
        }
    }

    @Override
    protected void fireTextChangeEvent() {
        textChangeListener.onTextChange(text, row, column);
    }
}
