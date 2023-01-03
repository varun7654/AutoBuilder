package com.dacubeking.autobuilder.gui.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Pose2d;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Translation2d;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

    @Contract(pure = true)
    public static float len2(@NotNull Vector2 vec, float x, float y) {
        return ((x - vec.x) * (x - vec.x)) + ((y - vec.y) * (y - vec.y));
    }

    @Contract("_ -> new")
    public static @NotNull Vector2 toRenderVector2(@NotNull Pose2d poseMeters) {
        float pointScaleFactor = AutoBuilder.getConfig().getPointScaleFactor();
        return new Vector2((float) poseMeters.getTranslation().getX() * pointScaleFactor,
                (float) poseMeters.getTranslation().getY() * pointScaleFactor);
    }

    @Contract("_ -> new")
    public static @NotNull Vector3 toRenderVector3(@NotNull Pose2d poseMeters) {
        float pointScaleFactor = AutoBuilder.getConfig().getPointScaleFactor();
        return new Vector3((float) poseMeters.getTranslation().getX() * pointScaleFactor,
                (float) poseMeters.getTranslation().getY() * pointScaleFactor,
                0);
    }

    public static float dist2(float x1, float y1, float x2, float y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static double dist2(double x1, double y1, double x2, double y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    @Contract(pure = true)
    public static double dist2(@NotNull Vector2 v1, @NotNull Vector2 v2) {
        return (v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y);
    }

    @Contract("_ -> new")
    public static @NotNull Vector3 toRenderVector3(@NotNull Translation2d translation2d) {
        float pointScaleFactor = AutoBuilder.getConfig().getPointScaleFactor();
        return new Vector3((float) translation2d.getX() * pointScaleFactor,
                (float) translation2d.getY() * pointScaleFactor,
                0);
    }

    @Contract("_, _ -> new")
    public static @NotNull Vector3 toRenderVector3(double x, double y) {
        float pointScaleFactor = AutoBuilder.getConfig().getPointScaleFactor();
        return new Vector3((float) x * pointScaleFactor,
                (float) y * pointScaleFactor,
                0);
    }

    @Contract("_, _ -> new")
    public static @NotNull Vector2 toRenderVector2(double x, double y) {
        float pointScaleFactor = AutoBuilder.getConfig().getPointScaleFactor();
        return new Vector2((float) x * pointScaleFactor,
                (float) y * pointScaleFactor);
    }

    /**
     * Returns closest point on segment to point
     *
     * @param ss - segment start point
     * @param se - segment end point
     * @param p  - point to found that is the closest point on segment
     * @return closest point on segment to p
     */
    public static Vector2 getClosestPointOnSegment(Vector2 ss, Vector2 se, Vector2 p) {
        return getClosestPointOnSegment(ss.x, ss.y, se.x, se.y, p.x, p.y);
    }

    /**
     * Returns closest point on segment to point
     *
     * @param sx1 - segment x coord 1
     * @param sy1 - segment y coord 1
     * @param sx2 - segment x coord 2
     * @param sy2 - segment y coord 2
     * @param px  - point x coord
     * @param py  - point y coord
     * @return closets point on segment to point
     */
    public static Vector2 getClosestPointOnSegment(float sx1, float sy1, float sx2, float sy2, float px, float py) {
        float xDelta = sx2 - sx1;
        float yDelta = sy2 - sy1;

        if ((xDelta == 0) && (yDelta == 0)) {
            return new Vector2(sx1, sy1);
        }

        double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

        final Vector2 closestPoint;
        if (u < 0) {
            closestPoint = new Vector2(sx1, sy1);
        } else if (u > 1) {
            closestPoint = new Vector2(sx2, sy2);
        } else {
            closestPoint = new Vector2((float) (sx1 + u * xDelta), (float) (sy1 + u * yDelta));
        }
        return closestPoint;
    }
}
