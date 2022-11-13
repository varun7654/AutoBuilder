package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * A divider between two {@link GuiElement}s.
 * <p>
 * It renders a line between the two elements.
 */
public class DividerGuiElement implements GuiElement {

    Color dividerColor = Color.valueOf("d8dee4");

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        shapeRenderer.setColor(dividerColor);
        shapeRenderer.line(drawStartX + 10, drawStartY, drawStartX + 10 + drawWidth - 25, drawStartY, 1);
        return 1;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        return 1;
    }

    @Override
    public void dispose() {
    }
}
