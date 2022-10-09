package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class DividerGuiElement implements GuiElement {

    Color dividerColor = Color.valueOf("d8dee4");

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        shapeRenderer.setColor(dividerColor);
        shapeRenderer.filledRectangle(drawStartX + 10, drawStartY, drawWidth - 25, 1);
        return 1;
    }
}
