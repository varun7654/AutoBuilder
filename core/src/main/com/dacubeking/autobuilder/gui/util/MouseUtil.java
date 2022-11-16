package com.dacubeking.autobuilder.gui.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MouseUtil {
    private MouseUtil() {
    }

    @Contract(pure = true)
    public static int getMouseY() {
        return Gdx.graphics.getHeight() - Gdx.input.getY();
    }

    @Contract(pure = true)
    public static int getMouseX() {
        return Gdx.input.getX();
    }

    @Contract(pure = true)
    public static boolean isMouseOver(int x, int y, int width, int height) {
        return getMouseX() >= x && getMouseX() <= x + width &&
                getMouseY() >= y && getMouseY() <= y + height;
    }

    @Contract(pure = true)
    public static boolean isMouseOver(float x, float y, float width, float height) {
        return getMouseX() >= x && getMouseX() <= x + width &&
                getMouseY() >= y && getMouseY() <= y + height;
    }

    @Contract(pure = true)
    public static boolean isMouseOver(Vector2 mousePos, int x, int y, int width, int height) {
        return mousePos.x >= x && mousePos.x <= x + width &&
                mousePos.y >= y && mousePos.y <= y + height;
    }

    @Contract(pure = true)
    public static boolean isMouseOver(Vector2 mousePos, float x, float y, float width, float height) {
        return mousePos.x >= x && mousePos.x <= x + width &&
                mousePos.y >= y && mousePos.y <= y + height;
    }

    @Contract(pure = true)
    public static boolean isControlPressed() {
        return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
    }

    @Contract(pure = true)
    public static boolean isShiftPressed() {
        return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Vector2 getMousePos() {
        return new Vector2(getMouseX(), getMouseY());
    }

    @Contract(value = "_ -> param1", mutates = "param1")
    public static @NotNull Vector2 getMousePos(@NotNull Vector2 vector2) {
        vector2.set(getMouseX(), getMouseY());
        return vector2;
    }

    private static boolean isLeftMousePressed = false;

    private static boolean isLeftMouseJustUnpressed = false;

    public static void update() {
        isLeftMouseJustUnpressed = !Gdx.input.isButtonPressed(Input.Buttons.LEFT) && isLeftMousePressed;
        isLeftMousePressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    }

    public static boolean isIsLeftMouseJustUnpressed() {
        return isLeftMouseJustUnpressed;
    }

    public static boolean isAltPressed() {
        return Gdx.input.isKeyPressed(Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Keys.ALT_RIGHT);
    }
}
