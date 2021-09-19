package me.varun.autobuilder.gui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import me.varun.autobuilder.util.RoundedShapeRenderer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractGuiItem {

    public abstract int render(@NotNull RoundedShapeRenderer shapeRenderer, int drawStartX, int drawStartY, int drawWidth);
}
