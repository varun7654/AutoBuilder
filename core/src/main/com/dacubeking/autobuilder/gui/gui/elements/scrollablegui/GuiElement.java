package com.dacubeking.autobuilder.gui.gui.elements.scrollablegui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * An element that can rendered in a GUI.
 */
public interface GuiElement {

    float render(@NotNull ShapeDrawer shapeRenderer, @NotNull PolygonSpriteBatch spriteBatch, float drawStartX,
                 float drawStartY, float drawWidth, Camera camera, boolean isLeftMouseJustUnpressed);

    float getHeight(float drawStartX, float drawStartY, float drawWidth, boolean isLeftMouseJustUnpressed);

    void dispose();

    default void onUnfocus() {
    }
}
