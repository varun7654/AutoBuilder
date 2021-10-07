package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import me.varun.autobuilder.gui.Gui;
import me.varun.autobuilder.net.NetworkTablesHelper;
import org.jetbrains.annotations.NotNull;

public class PushAutoButton extends AbstractGuiButton {

    NetworkTablesHelper networkTables = NetworkTablesHelper.getInstance();

    public PushAutoButton(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("upload-icon.png"), true));
    }

    @Override
    public boolean checkClick(@NotNull Gui gui) {
        if (super.checkClick(gui)) {

            networkTables.pushData(gui.guiItems);
            return true;
        }
        return false;
    }
}
