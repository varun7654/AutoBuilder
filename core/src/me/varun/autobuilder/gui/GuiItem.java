package me.varun.autobuilder.gui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import me.varun.autobuilder.util.RoundedShapeRenderer;

public abstract class GuiItem{

    public abstract int render(RoundedShapeRenderer shapeRenderer, int drawStartX, int drawStartY, int drawWidth, Rectangle scissors);
}
