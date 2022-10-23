package com.dacubeking.autobuilder.gui.util;

import com.badlogic.gdx.graphics.Color;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

public final class MiscShapeRenderer {
    private MiscShapeRenderer() {
    }

    public static void plusIcon(@NotNull ShapeDrawer drawer, float x, float y, float width, float height, Color color) {
        drawer.line(x, y + height / 2, x + width, y + height / 2, color);
        drawer.line(x + width / 2, y, x + width / 2, y + height, color);
    }

    public static void plusIconTopLeft(@NotNull ShapeDrawer drawer, float x, float y, float width, float height, Color color) {
        plusIcon(drawer, x, y - height, width, height, color);
    }

    public static void plusIconCentered(@NotNull ShapeDrawer drawer, float x, float y, float width, float height, Color color) {
        plusIcon(drawer, x - width / 2, y - height / 2, width, height, color);
    }
}
