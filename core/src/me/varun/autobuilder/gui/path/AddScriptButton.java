package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.gui.elements.AbstractGuiButton;
import org.jetbrains.annotations.NotNull;

public class AddScriptButton extends AbstractGuiButton {
    private @NotNull
    final ShaderProgram fontShader;
    private @NotNull
    final InputEventThrower inputEventThrower;

    private @NotNull
    final BitmapFont font;

    public AddScriptButton(int x, int y, int width, int height, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                           @NotNull InputEventThrower inputEventThrower) {
        super(x, y, width, height, new Texture(Gdx.files.internal("script_icon.png"), true));
        this.font = font;
        this.fontShader = fontShader;
        this.inputEventThrower = inputEventThrower;
    }

    @Override
    public boolean checkClick(@NotNull PathGui pathGui) {
        if (super.checkClick(pathGui)) {
            pathGui.guiItems.add(new ScriptItem(fontShader, font, inputEventThrower));
            UndoHandler.getInstance().somethingChanged();
            return true;
        }
        return false;
    }
}
