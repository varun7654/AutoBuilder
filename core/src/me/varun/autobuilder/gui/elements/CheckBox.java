package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class CheckBox extends AbstractGuiButton {

    public CheckBox(int x, int y, int width, int height ){
        super(x, y, width, height, CHECKMARK_TEXTURE);

    }

    private static final Texture CHECKMARK_TEXTURE = new Texture(Gdx.files.internal("check-mark.png"), true);
    static  {
        CHECKMARK_TEXTURE.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);
    }

    @Override
    public void dispose() {

    }
}
