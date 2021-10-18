package me.varun.autobuilder.gui.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import me.varun.autobuilder.gui.elements.AbstractGuiButton;

public class SettingsIcon extends AbstractGuiButton {
    public SettingsIcon(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("settings_icon.png"), true));
    }
}
