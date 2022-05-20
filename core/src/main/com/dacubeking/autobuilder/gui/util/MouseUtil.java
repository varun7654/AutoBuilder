package com.dacubeking.autobuilder.gui.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

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

    public static boolean isMouseOver(Vector2 mousePos, int x, int y, int width, int height) {
        return mousePos.x >= x && mousePos.x <= x + width &&
                mousePos.y >= y && mousePos.y <= y + height;
    }

    public static boolean isControlPressed() {
        return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
    }

    public static boolean isShiftPressed() {
        return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
    }
}
