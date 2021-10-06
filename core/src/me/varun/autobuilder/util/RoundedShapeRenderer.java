package me.varun.autobuilder.util;

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
        drawer.filledCircle(x + radius, y + radius, radius);
        drawer.filledCircle(x + width - radius, y + radius, radius);
        drawer.filledCircle(x + width - radius, y + height - radius, radius);
        drawer.filledCircle(x + radius, y + height - radius, radius);
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
        drawer.filledCircle(x + radius, y + radius, radius, color);
        drawer.filledCircle(x + width - radius, y + radius, radius, color);
        drawer.filledCircle(x + width - radius, y + height - radius, radius, color);
        drawer.filledCircle(x + radius, y + height - radius, radius, color);
    }
}