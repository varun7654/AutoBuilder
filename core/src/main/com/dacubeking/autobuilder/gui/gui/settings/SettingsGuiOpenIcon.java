package com.dacubeking.autobuilder.gui.gui.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.dacubeking.autobuilder.gui.gui.elements.AbstractGuiButton;

public class SettingsGuiOpenIcon extends AbstractGuiButton {
    public SettingsGuiOpenIcon() {
        super(new Texture(Gdx.files.internal("settings_icon.png"), true));
    }
}
