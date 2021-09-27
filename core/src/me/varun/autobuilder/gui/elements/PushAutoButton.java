package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.gui.Gui;
import me.varun.autobuilder.gui.ScriptItem;
import me.varun.autobuilder.net.NetworkTables;
import org.jetbrains.annotations.NotNull;

public class PushAutoButton extends AbstractGuiButton {

    NetworkTables networkTables = NetworkTables.getInstance();

    public PushAutoButton(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("upload-icon.png"), true));
    }

    @Override
    public boolean checkClick(@NotNull Gui gui) {
        if(super.checkClick(gui)){
            networkTables.pushData(gui.guiItems);
            return true;
        }
        return false;
    }
}
