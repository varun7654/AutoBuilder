package me.varun.autobuilder.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class AddPathButton extends AbstractGuiButton{
    public AddPathButton(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("path_icon.png"), true));
    }

    @Override
    public boolean checkClick(Gui gui) {
        if(super.checkClick(gui)){
            //Do things
            return true;
        }

        return false;

    }
}
