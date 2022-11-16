package com.dacubeking.autobuilder.gui.util;

import com.badlogic.gdx.graphics.Color;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class RoundedShapeRenderer {
    /**
     * Draws a rectangle with rounded corners of the given radius.
     */
    public static void roundedRect(ShapeDrawer drawer, float x, float y, float width, float height, float radius) {
        // Central rectangle
        drawer.filledRectangle(x + radius, y + radius, width - 2 * radius, height - 2 * radius);

        // Four side rectangles, in clockwise order
        drawer.filledRectangle(x + radius, y, width - 2 * radius, radius);
        drawer.filledRectangle(x + width - radius, y + radius, radius, height - 2 * radius);
        drawer.filledRectangle(x + radius, y + height - radius, width - 2 * radius, radius);
        drawer.filledRectangle(x, y + radius, radius, height - 2 * radius);

        // Four arches, clockwise too
        drawer.sector(x + radius, y + radius, radius, (float) Math.PI, (float) Math.PI / 2);
        drawer.sector(x + width - radius, y + radius, radius, (float) (3 * Math.PI) / 2, (float) Math.PI / 2);
        drawer.sector(x + width - radius, y + height - radius, radius, (float) 0, (float) Math.PI / 2);
        drawer.sector(x + radius, y + height - radius, radius, (float) Math.PI / 2, (float) Math.PI / 2);
    }

    public static void roundedRect(ShapeDrawer drawer, float x, float y, float width, float height, float radius, Color color) {
        // Central rectangle
        drawer.filledRectangle(x + radius, y + radius, width - 2 * radius, height - 2 * radius, color);

        // Four side rectangles, in clockwise order
        drawer.filledRectangle(x + radius, y, width - 2 * radius, radius, color);
        drawer.filledRectangle(x + width - radius, y + radius, radius, height - 2 * radius, color);
        drawer.filledRectangle(x + radius, y + height - radius, width - 2 * radius, radius, color);
        drawer.filledRectangle(x, y + radius, radius, height - 2 * radius, color);

        // Four arches, clockwise too
        drawer.setColor(color);
        drawer.sector(x + radius, y + radius, radius, (float) Math.PI, (float) Math.PI / 2);
        drawer.sector(x + width - radius, y + radius, radius, (float) (3 * Math.PI) / 2, (float) Math.PI / 2);
        drawer.sector(x + width - radius, y + height - radius, radius, (float) 0, (float) Math.PI / 2);
        drawer.sector(x + radius, y + height - radius, radius, (float) Math.PI / 2, (float) Math.PI / 2);
    }

    public static void roundedRectTopLeft(ShapeDrawer drawer, float x, float y, float width, float height, float radius,
                                          Color color) {
        roundedRect(drawer, x, y - height, width, height, radius, color);
    }
}