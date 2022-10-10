package com.dacubeking.autobuilder.gui.pathing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.pathing.pointclicks.CloseTrajectoryPoint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

public interface PathRenderer {

    void render(@NotNull ShapeDrawer renderer, @NotNull OrthographicCamera cam);

    /**
     * Update the point that is selected. This should be called every frame.
     *
     * @param camera    the camera
     * @param mousePos  the current mouse position
     * @param mouseDiff the difference between the current and last mouse position
     */
    void updatePoint(OrthographicCamera camera, Vector3 mousePos, Vector3 mouseDiff);

    /**
     * Get a list of all points that are close to the mouse position.
     *
     * @param maxDistance2 The maximum distance to the mouse position squared.
     * @return List of all points on the trajectory that are close to the mouse position.
     */
    @NotNull ArrayList<CloseTrajectoryPoint> getCloseTrajectoryPoints(float maxDistance2, Vector3 mousePos);

    /**
     * Sets the time on the trajectory that the robot pose preview should be shown. This should be called every frame when it will
     * be shown.
     */
    void setRobotPathPreviewPoint(CloseTrajectoryPoint closePoint);

    /**
     * @param mousePos current mouse position
     * @return the distance to the closest point that isn't a main point on the path
     */
    double distToClosestPointNotMainPoint(Vector3 mousePos);


    /**
     * Draws the bounding box of the robot preview
     *
     * @param origin   Origin of the point (in pixels)
     * @param rotation Rotation of the point (in radians)
     * @param renderer shapeDrawer
     */
    default void renderRobotBoundingBox(Vector2 origin, float rotation, @NotNull ShapeDrawer renderer, Color mainColor,
                                        Color secondaryColor) {
        float robotWidth = AutoBuilder.getConfig().getRobotWidth();
        float robotLength = AutoBuilder.getConfig().getRobotLength();
        float pointScaleFactor = AutoBuilder.getConfig().getPointScaleFactor();
        Vector2 leftTop = new Vector2(origin).add(-(robotWidth / 2) * pointScaleFactor,
                (robotLength / 2) * pointScaleFactor);
        Vector2 rightTop = new Vector2(origin).add((robotWidth / 2) * pointScaleFactor,
                (robotLength / 2) * pointScaleFactor);
        Vector2 leftBottom = new Vector2(origin).add(-(robotWidth / 2) * pointScaleFactor,
                -(robotLength / 2) * pointScaleFactor);
        Vector2 rightBottom = new Vector2(origin).add((robotWidth / 2) * pointScaleFactor,
                -(robotLength / 2) * pointScaleFactor);

        leftTop.rotateAroundRad(origin, rotation);
        rightTop.rotateAroundRad(origin, rotation);
        leftBottom.rotateAroundRad(origin, rotation);
        rightBottom.rotateAroundRad(origin, rotation);

        renderer.setColor(mainColor);
        renderer.line(leftTop, rightTop, AutoBuilder.LINE_THICKNESS);
        renderer.line(rightBottom, leftBottom, AutoBuilder.LINE_THICKNESS);
        renderer.line(leftBottom, leftTop, AutoBuilder.LINE_THICKNESS);

        renderer.setColor(secondaryColor);
        renderer.line(rightTop, rightBottom, AutoBuilder.LINE_THICKNESS);
    }
}
