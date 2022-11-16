package com.dacubeking.autobuilder.gui.gui.shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.dacubeking.autobuilder.gui.gui.elements.AbstractGuiButton;

public class ShooterGuiOpenIcon extends AbstractGuiButton {
    public ShooterGuiOpenIcon() {
        super(new Texture(Gdx.files.internal("shooterconfig.png"), true));
    }
}
