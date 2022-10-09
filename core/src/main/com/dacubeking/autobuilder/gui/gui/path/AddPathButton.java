package com.dacubeking.autobuilder.gui.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.dacubeking.autobuilder.gui.CameraHandler;
import com.dacubeking.autobuilder.gui.gui.elements.AbstractGuiButton;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import org.jetbrains.annotations.NotNull;

public class AddPathButton extends AbstractGuiButton {
    private final CameraHandler cameraHandler;

    public AddPathButton(int x, int y, int width, int height,
                         CameraHandler cameraHandler) {
        super(x, y, width, height, new Texture(Gdx.files.internal("path_icon.png"), true));
        this.cameraHandler = cameraHandler;
    }

    public boolean checkClick(@NotNull PathGui pathGui) {
        if (super.checkClick()) {
            pathGui.guiItems.add(new TrajectoryItem(pathGui, cameraHandler));
            pathGui.scrollToBottom();
            UndoHandler.getInstance().somethingChanged();
            return true;
        }

        return false;
    }
}
