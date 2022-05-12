package com.dacubeking.autobuilder.gui.gui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {
    List<HudElement> hudElements = new ArrayList<>();

    public synchronized void setHudElements(List<HudElement> hudElements) {
        this.hudElements = hudElements;
    }

    public synchronized void render(ShapeDrawer shapeDrawer, Batch batch, float hudXOffset) {
        float xOffset = hudXOffset + 5;

        float yOffset = Gdx.graphics.getHeight() - 35;
        for (HudElement hudElement : hudElements) {
            if (xOffset > Gdx.graphics.getWidth() - 420 - hudElement.width) {
                xOffset = 0;
                yOffset -= 35; // Element has width 30 + 5 padding
            }
            hudElement.render(shapeDrawer, batch, xOffset, yOffset);
            xOffset += hudElement.width + 5;
        }
    }
}
