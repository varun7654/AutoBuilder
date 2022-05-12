package com.dacubeking.autobuilder.gui.gui.shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.dacubeking.autobuilder.gui.gui.elements.AbstractGuiButton;

public class ShooterGuiOpenIcon extends AbstractGuiButton {
    public ShooterGuiOpenIcon(int x, int y, int width, int height) {
        super(x, y, width, height, new Texture(Gdx.files.internal("shooterconfig.png"), true));
    }
}
