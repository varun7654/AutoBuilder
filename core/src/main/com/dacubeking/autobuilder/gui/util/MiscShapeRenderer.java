package com.dacubeking.autobuilder.gui.util;

import com.badlogic.gdx.graphics.Color;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public final class MiscShapeRenderer {
    private MiscShapeRenderer() {
    }

    /**
     * Draws a plus sign.
     *
     * @param drawer The drawer to use.
     * @param x      X position of the bottom left corner of the plus sign.
     * @param y      Y position of the bottom left corner of the plus sign.
     * @param width  Width of the plus sign.
     * @param height Height of the plus sign.
     * @param color  Color of the plus sign.
     */
    public static void plusIcon(@NotNull ShapeDrawer drawer, float x, float y, float width, float height, Color color) {
        drawer.line(x, y + height / 2, x + width, y + height / 2, color);
        drawer.line(x + width / 2, y, x + width / 2, y + height, color);
    }

    /**
     * Draws a plus sign.
     *
     * @param drawer The drawer to use.
     * @param x      X position of the top left corner of the plus sign.
     * @param y      Y position of the top left corner of the plus sign.
     * @param width  Width of the plus sign.
     * @param height Height of the plus sign.
     * @param color  Color of the plus sign.
     */
    public static void plusIconTopLeft(@NotNull ShapeDrawer drawer, float x, float y, float width, float height, Color color) {
        plusIcon(drawer, x, y - height, width, height, color);
    }

    /**
     * Draws a plus sign.
     *
     * @param drawer The drawer to use.
     * @param x      X position of the center of the plus sign.
     * @param y      Y position of the center of the plus sign.
     * @param width  Width of the plus sign.
     * @param height Height of the plus sign.
     * @param color  Color of the plus sign.
     */
    public static void plusIconCentered(@NotNull ShapeDrawer drawer, float x, float y, float width, float height, Color color) {
        plusIcon(drawer, x - width / 2, y - height / 2, width, height, color);
    }
}
