package me.varun.autobuilder.gui.path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import me.varun.autobuilder.gui.elements.AbstractGuiButton;
import me.varun.autobuilder.net.NetworkTablesHelper;
import org.jetbrains.annotations.NotNull;

public class PushAutoButton extends AbstractGuiButton {

    NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();

    public PushAutoButton(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("upload-icon.png"), true));
    }

    public boolean checkClick(@NotNull PathGui pathGui) {
        if (super.checkClick()) {
            networkTables.pushAutoData(pathGui.guiItems);
            return true;
        }
        return false;
    }
}
