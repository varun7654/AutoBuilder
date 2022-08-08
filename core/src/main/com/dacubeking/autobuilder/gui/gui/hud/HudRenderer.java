package com.dacubeking.autobuilder.gui.gui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {
    List<HudElement> hudElements = new ArrayList<>();

    public synchronized void setHudElements(List<HudElement> hudElements) {
        this.hudElements.forEach(HudElement::dispose);
        this.hudElements = hudElements;
    }

    public synchronized void render(ShapeDrawer shapeDrawer, Batch batch, float hudXOffset) {
        float xOffset = hudXOffset + 5;

        float yOffset = Gdx.graphics.getHeight() - 35;
        for (HudElement hudElement : hudElements) {
            hudElement.render(shapeDrawer, batch, xOffset, yOffset);
            xOffset += hudElement.width + 5;
            if (xOffset > AutoBuilder.getInstance().pathGui.getPanelX()) {
                xOffset = hudXOffset + 5;
                yOffset -= 35; // Element has width 30 + 5 padding
            }
        }
    }
}
