package me.varun.autobuilder.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;

public class MathUtil {
    private static MathUtil instance = new MathUtil();
    public static MathUtil getInstance() {
        return instance;
    }

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


    public static Vector2 toRenderVector2(Pose2d poseMeters) {
        return new Vector2((float) poseMeters.getTranslation().getX() * AutoBuilder.getConfig().getPointScaleFactor(),
                (float) poseMeters.getTranslation().getY() * AutoBuilder.getConfig().getPointScaleFactor());
    }

    public static Vector3 toRenderVector3(Pose2d poseMeters) {
        return new Vector3((float) poseMeters.getTranslation().getX() * AutoBuilder.getConfig().getPointScaleFactor(),
                (float) poseMeters.getTranslation().getY() * AutoBuilder.getConfig().getPointScaleFactor(),
                0);
    }

    public void printThing(String str) {
        System.out.println("String: " + str);
    }

    public void printThing(int i) {
        System.out.println("Int: " + i);
    }

    public void printThing(double b, double str) {
        System.out.println("Double: " + b + " String: " + str);
    }

    public void addThing(double a, double b) {
        System.out.println(a+b);
    }

}
