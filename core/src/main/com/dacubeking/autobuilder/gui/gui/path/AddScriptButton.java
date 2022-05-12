package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.dacubeking.autobuilder.gui.UndoHandler;
import com.dacubeking.autobuilder.gui.events.input.InputEventThrower;
import com.dacubeking.autobuilder.gui.gui.elements.AbstractGuiButton;
import org.jetbrains.annotations.NotNull;

public class AddScriptButton extends AbstractGuiButton {
    final InputEventThrower inputEventThrower;

    public AddScriptButton(int x, int y, int width, int height, @NotNull InputEventThrower inputEventThrower) {
        super(x, y, width, height, new Texture(Gdx.files.internal("script_icon.png"), true));
        this.inputEventThrower = inputEventThrower;
    }

    public boolean checkClick(@NotNull PathGui pathGui) {
        if (super.checkClick()){
            pathGui.guiItems.add(new ScriptItem(inputEventThrower));
            UndoHandler.getInstance().somethingChanged();
            return true;
        }
        return false;
    }
}
