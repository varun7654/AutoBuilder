package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.dacubeking.autobuilder.gui.gui.elements.AbstractGuiButton;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import org.jetbrains.annotations.NotNull;

public class AddScriptButton extends AbstractGuiButton {
    public AddScriptButton(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("script_icon.png"), true));
    }

    public boolean checkClick(@NotNull PathGui pathGui) {
        if (super.checkClick()) {
            pathGui.guiItems.add(new ScriptItem());
            pathGui.scrollToBottom();
            UndoHandler.getInstance().somethingChanged();
            return true;
        }
        return false;
    }
}
