package me.varun.autobuilder.util;

import com.badlogic.gdx.math.Vector2;

public class MathUtil {
    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float len2(Vector2 vec, float x, float y) {
        return ((x - vec.x) * (x - vec.x)) + ((y - vec.y) * (y - vec.y));
    }
}
