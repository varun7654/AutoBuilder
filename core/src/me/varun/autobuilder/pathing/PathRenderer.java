package me.varun.autobuilder.pathing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.config.Config;
import me.varun.autobuilder.pathing.pointclicks.CloseTrajectoryPoint;
import org.jetbrains.annotations.NotNull;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

import static me.varun.autobuilder.AutoBuilder.LINE_THICKNESS;

public interface PathRenderer {

    void render(@NotNull ShapeDrawer renderer, @NotNull OrthographicCamera cam);

    Config config = AutoBuilder.getConfig();

    /**
     * Update the point that is selected. This should be called every frame.
     *
     * @param camera       the camera
     * @param mousePos     the current mouse position
     * @param lastMousePos the last mouse position
     */
    void updatePoint(OrthographicCamera camera, Vector3 mousePos, Vector3 lastMousePos);

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
     * @param mousePos     current mouse position
     * @param maxDistance2 the maximum distance to the mouse position squared
     * @return True if the rotation or control point is being touched.
     */
    boolean isTouchingSomething(Vector3 mousePos, float maxDistance2);


    /**
     * Draws the bounding box of the robot preview
     *
     * @param origin   Origin of the point (in pixels)
     * @param rotation Rotation of the point (in radians)
     * @param renderer shapeDrawer
     */
    default void renderRobotBoundingBox(Vector2 origin, float rotation, @NotNull ShapeDrawer renderer, Color mainColor, Color secondaryColor) {
        Vector2 leftTop = new Vector2(origin).add(-(config.getRobotWidth() / 2) * config.getPointScaleFactor(), (config.getRobotLength() / 2) * config.getPointScaleFactor());
        Vector2 rightTop = new Vector2(origin).add((config.getRobotWidth() / 2) * config.getPointScaleFactor(), (config.getRobotLength() / 2) * config.getPointScaleFactor());
        Vector2 leftBottom = new Vector2(origin).add(-(config.getRobotWidth() / 2) * config.getPointScaleFactor(), -(config.getRobotLength() / 2) * config.getPointScaleFactor());
        Vector2 rightBottom = new Vector2(origin).add((config.getRobotWidth() / 2) * config.getPointScaleFactor(), -(config.getRobotLength() / 2) * config.getPointScaleFactor());

        leftTop.rotateAroundRad(origin, rotation);
        rightTop.rotateAroundRad(origin, rotation);
        leftBottom.rotateAroundRad(origin, rotation);
        rightBottom.rotateAroundRad(origin, rotation);

        renderer.setColor(mainColor);
        renderer.line(leftTop, rightTop, LINE_THICKNESS);
        renderer.line(rightBottom, leftBottom, LINE_THICKNESS);
        renderer.line(leftBottom, leftTop, LINE_THICKNESS);

        renderer.setColor(secondaryColor);
        renderer.line(rightTop, rightBottom, LINE_THICKNESS);
    }
}
