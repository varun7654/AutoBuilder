package com.dacubeking.autobuilder.gui.util;

import com.badlogic.gdx.Gdx;

public final class MouseUtil {
    private MouseUtil() {
    }

    public static int getMouseY() {
        return Gdx.graphics.getHeight() - Gdx.input.getY();
    }

    public static int getMouseX() {
        return Gdx.input.getX();
    }

    public static boolean isMouseOver(int x, int y, int width, int height) {
        return getMouseX() >= x && getMouseX() <= x + width &&
                getMouseY() >= y && getMouseY() <= y + height;
    }
}
