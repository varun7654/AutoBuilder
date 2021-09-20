package me.varun.autobuilder.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractGuiItem {

    public abstract int render(@NotNull RoundedShapeRenderer shapeRenderer, @NotNull SpriteBatch spriteBatch, int drawStartX, int drawStartY, int drawWidth);
}
