package me.varun.autobuilder.gui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class CheckBox extends AbstractGuiButton {

    private static final Texture CHECKMARK_TEXTURE = new Texture(Gdx.files.internal("check-mark.png"), true);

    static {
        CHECKMARK_TEXTURE.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear);
    }

    public CheckBox(float x, float y, float width, float height) {
        super(x, y, width, height, CHECKMARK_TEXTURE);
    }

    @Override
    public void dispose() {

    }
}
