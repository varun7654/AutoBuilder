package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * An {@link GuiElement} that renders nothing and serves as a space between other elements.
 */
public class SpaceGuiElement implements GuiElement {

    private final float space;

    /**
     * @param space The space in pixels.
     */
    public SpaceGuiElement(float space) {
        this.space = space;
    }

    @Override
    public float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                        float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed) {
        return space;
    }

    @Override
    public float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed) {
        return space;
    }

    @Override
    public void dispose() {
    }
}
