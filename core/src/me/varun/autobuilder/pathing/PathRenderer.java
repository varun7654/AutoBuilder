package me.varun.autobuilder.pathing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.UndoHandler;
import me.varun.autobuilder.config.Config;
import me.varun.autobuilder.events.movablepoint.MovablePointEventHandler;
import me.varun.autobuilder.events.movablepoint.PointClickEvent;
import me.varun.autobuilder.events.movablepoint.PointMoveEvent;
import me.varun.autobuilder.events.pathchange.PathChangeListener;
import me.varun.autobuilder.gui.notification.Notification;
import me.varun.autobuilder.gui.notification.NotificationHandler;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.pathing.pointclicks.ClosePoint;
import me.varun.autobuilder.pathing.pointclicks.CloseTrajectoryPoint;
import me.varun.autobuilder.util.MathUtil;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import me.varun.autobuilder.wpi.math.spline.Spline.ControlVector;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryConfig;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator.ControlVectorList;
import me.varun.autobuilder.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static me.varun.autobuilder.AutoBuilder.*;

public class PathRenderer implements MovablePointEventHandler, Serializable {
    @NotNull private final Color color;
    @NotNull private final ControlVectorList controlVectors;
    @NotNull private final List<Rotation2d> rotation2dList;
    @NotNull private final List<MovablePointRenderer> pointRenderList;
    @NotNull private final ExecutorService executorService;
    @Nullable PathChangeListener pathChangeListener;
    boolean pointDeleted;
    CompletableFuture<Trajectory> completableFutureTrajectory;
    Config config = AutoBuilder.getConfig();
    @Nullable private Trajectory trajectory;
    private boolean reversed = false;
    @Nullable private MovablePointRenderer controlPoint;
    @Nullable private MovablePointRenderer rotationPoint;
    @Nullable private PointRenderer highlightPoint;
    private int selectionPointIndex = -1;
    private float velocityStart;
    private float velocityEnd;
    private float robotPreviewTime = -1;
    @Nullable private PathRenderer attachedPath;
    private boolean isAttachedPathEnd;

    @NotNull Vector2 lastPointLeft = new Vector2();
    @NotNull Vector2 lastPointRight = new Vector2();
    @NotNull Vector2 nextPointLeft = new Vector2();
    @NotNull Vector2 nextPointRight = new Vector2();
    private int robotPreviewIndex;

    public PathRenderer(@NotNull Color color, @NotNull ControlVectorList pointList, @NotNull List<Rotation2d> rotation2dList,
                        @NotNull ExecutorService executorService, float velocityStart, float velocityEnd) {
        this.color = color;
        this.controlVectors = pointList;
        this.rotation2dList = rotation2dList;

        pointRenderList = new ArrayList<>();

        for (ControlVector controlVector : controlVectors) {
            pointRenderList.add(new MovablePointRenderer((float) controlVector.x[0], (float) controlVector.y[0], color, POINT_SIZE, this));
        }

        this.executorService = executorService;
        updatePath();

        this.velocityStart = velocityStart;
        this.velocityEnd = velocityEnd;
    }

    public void setPathChangeListener(@NotNull PathChangeListener pathChangeListener) {
        this.pathChangeListener = pathChangeListener;
        updatePath();
    }

    public void render(@NotNull ShapeDrawer renderer, @NotNull OrthographicCamera cam) {
        if (trajectory == null) return;

        //Get the first 2 points of the line at t = 0
        lastPointLeft.set(0, -LINE_THICKNESS / 2);
        lastPointRight.set(0, LINE_THICKNESS / 2);

        lastPointLeft.rotateRad((float) trajectory.sample(0).poseMeters.getRotation().getRadians());
        lastPointRight.rotateRad((float) trajectory.sample(0).poseMeters.getRotation().getRadians());

        lastPointLeft.add((float) trajectory.sample(0).poseMeters.getTranslation().getX() * config.getPointScaleFactor(),
                (float) trajectory.sample(0).poseMeters.getTranslation().getY() * config.getPointScaleFactor());
        lastPointRight.add((float) trajectory.sample(0).poseMeters.getTranslation().getX() * config.getPointScaleFactor(),
                (float) trajectory.sample(0).poseMeters.getTranslation().getY() * config.getPointScaleFactor());

        for (double i = 0.01; i < trajectory.getTotalTimeSeconds(); i += 0.01) {
            Pose2d cur = trajectory.sample(i).poseMeters;

            //Use the speed of the path to determine its saturation
            double speed = Math.abs(trajectory.sample(i).velocityMetersPerSecond);
            float[] color = new float[3];
            this.color.toHsv(color);
            color[1] = (float) (0.9 * (speed / getConfig().getPathingConfig().maxVelocityMetersPerSecond) + 0.1);
            Color speedColor = new Color().fromHsv(color);
            speedColor.set(speedColor.r, speedColor.g, speedColor.b, 1);

            //Get the 2 points of the line at the current time
            nextPointLeft.set(0, -LINE_THICKNESS / 2);
            nextPointRight.set(0, LINE_THICKNESS / 2);

            nextPointLeft.rotateRad((float) cur.getRotation().getRadians());
            nextPointRight.rotateRad((float) cur.getRotation().getRadians());

            nextPointLeft.add((float) cur.getTranslation().getX() * config.getPointScaleFactor(),
                    (float) cur.getTranslation().getY() * config.getPointScaleFactor());
            nextPointRight.add((float) cur.getTranslation().getX() * config.getPointScaleFactor(),
                    (float) cur.getTranslation().getY() * config.getPointScaleFactor());

            //Render the line
            renderer.setColor(speedColor);
            renderer.filledPolygon(new float[]{
                    lastPointLeft.x, lastPointLeft.y,
                    lastPointRight.x, lastPointRight.y,
                    nextPointRight.x, nextPointRight.y,
                    nextPointLeft.x, nextPointLeft.y
            });

            lastPointLeft.set(nextPointLeft);
            lastPointRight.set(nextPointRight);
        }

        if (controlPoint != null) {
            PointRenderer selectedPoint = pointRenderList.get(selectionPointIndex);
            renderer.line(selectedPoint.getRenderPos2(), controlPoint.getRenderPos2(), Color.WHITE, LINE_THICKNESS);
            controlPoint.draw(renderer, cam);

            if (rotationPoint != null) {
                renderer.line(selectedPoint.getRenderPos2(), rotationPoint.getRenderPos2(), Color.WHITE, LINE_THICKNESS);
                rotationPoint.draw(renderer, cam);
            }

            Vector2 origin = selectedPoint.getRenderPos2();
            renderRobotBoundingBox(origin, (float) rotation2dList.get(selectionPointIndex).getRadians(), renderer);
        } else if (robotPreviewTime >= 0) {
            Pose2d hoverPose = trajectory.sample(robotPreviewTime).poseMeters;
            Vector2 origin = MathUtil.toRenderVector2(hoverPose);

            renderRobotBoundingBox(origin, (float) rotation2dList.get(robotPreviewIndex).getRadians(), renderer);
        }

        for (int i = 0; i < pointRenderList.size(); i++) {
            PointRenderer pointRenderer = pointRenderList.get(i);

            if (i == selectionPointIndex) {
                if (highlightPoint == null) {
                    highlightPoint = new PointRenderer(pointRenderer.getPos2(), Color.WHITE, POINT_SIZE * 1.4f);
                } else {
                    highlightPoint.setPosition(pointRenderer.getPos2());
                }

                highlightPoint.draw(renderer, cam);
            }
            pointRenderer.draw(renderer, cam);
        }

        //Reset the robot preview time so that it won't be visible in the next frame. (Requires that it is set again)
        robotPreviewTime = -1;
    }

    /**
     * Get points that are close to the mouse position and returns a list of them.
     *
     * @param maxDistance2 The maximum distance to the mouse position squared.
     * @param mousePos     The current mouse position.
     * @return List of all points that are close to the mouse position.
     */
    public @NotNull ArrayList<ClosePoint> getClosePoints(float maxDistance2, Vector3 mousePos) {
        ArrayList<ClosePoint> closePoints = new ArrayList<>();
        for (int i = 0; i < pointRenderList.size(); i++) {
            PointRenderer pointRenderer = pointRenderList.get(i);
            float distanceToMouse2 = pointRenderer.getRenderPos3().dst2(mousePos);
            if (distanceToMouse2 < maxDistance2) {
                closePoints.add(new ClosePoint(distanceToMouse2, this, i));
            }
        }
        return closePoints;
    }

    /**
     * Get a list of all points that are close to the mouse position.
     * @param maxDistance2 The maximum distance to the mouse position squared.
     * @return List of all points on the trajectory that are close to the mouse position.
     */
    public @NotNull ArrayList<CloseTrajectoryPoint> getCloseTrajectoryPoints(float maxDistance2, Vector3 mousePos) {
        if(trajectory != null){
            ArrayList<CloseTrajectoryPoint> closePoints = new ArrayList<>();
            int currentIndexPos = 0;
            for (float i = 0; i < trajectory.getTotalTimeSeconds(); i += 0.01f) {
                Vector3 renderVector = MathUtil.toRenderVector3(trajectory.sample(i).poseMeters);

                if (currentIndexPos + 1 < controlVectors.size() &&
                        MathUtil.toRenderVector3(controlVectors.get(currentIndexPos + 1).x[0], controlVectors.get(currentIndexPos + 1).y[0])
                                .dst2(renderVector) < 8f) {
                    currentIndexPos++;
                }

                float distanceToMouse2 = renderVector.dst2(mousePos);
                if (distanceToMouse2 < maxDistance2) {
                    closePoints.add(new CloseTrajectoryPoint(distanceToMouse2, this, currentIndexPos, i));
                }
            }
            return closePoints;
        }
        return new ArrayList<>();
    }

    /**
     * Delete a point
     *
     * @param closePoint object that was created by {@link PathRenderer#getClosePoints(float, Vector3)}
     */
    public void deletePoint(ClosePoint closePoint) {
        if (controlVectors.size() > 2) {
            if (selectionPointIndex > closePoint.index) {
                selectionPointIndex--;
            } else if (selectionPointIndex == closePoint.index) {
                removeSelection();
            }
            pointRenderList.remove(closePoint.index);
            controlVectors.remove(closePoint.index);
            pointDeleted = true;
            updatePath();
        }
    }

    /**
     * Add a point
     *
     * @param closePoint object that was created by {@link PathRenderer#getCloseTrajectoryPoints(float, Vector3)}
     */
    public void addPoint(CloseTrajectoryPoint closePoint) {
        assert trajectory != null;
        Pose2d newPoint = trajectory.sample(closePoint.pointTime).poseMeters;
        controlVectors.add(closePoint.prevPointIndex + 1, new ControlVector(new double[]{newPoint.getX(), 1, 0}, new double[]{newPoint.getY(), 0, 0}));
        pointRenderList.add(closePoint.prevPointIndex + 1,
                new MovablePointRenderer((float) newPoint.getX(), (float) newPoint.getY(), color, POINT_SIZE, this));
        rotation2dList.add(closePoint.prevPointIndex + 1, new Rotation2d(0));
        if (selectionPointIndex > closePoint.prevPointIndex) selectionPointIndex++;
        updatePath();
        UndoHandler.getInstance().somethingChanged();
    }

    /**
     * @param mousePos     current mouse position
     * @param maxDistance2 the maximum distance to the mouse position squared
     * @return True if the rotation point is being touched.
     */
    public boolean isTouchingSomething(Vector3 mousePos, float maxDistance2) {
        if (controlPoint != null && controlPoint.getRenderPos3().dst2(mousePos) < maxDistance2) return true;
        if (rotationPoint != null && rotationPoint.getRenderPos3().dst2(mousePos) < maxDistance2) return true;
        return false;
    }

    /**
     * @param closePoint   object that was created by {@link PathRenderer#getClosePoints(float, Vector3)}
     * @param camera       the camera
     * @param mousePos     the current mouse position
     * @param lastMousePos the last mouse position
     * @param itemList     the list of gui items that contains all the path items
     */
    public void selectPoint(@NotNull ClosePoint closePoint, @NotNull OrthographicCamera camera, @NotNull Vector3 mousePos,
                            @NotNull Vector3 lastMousePos, @NotNull List<AbstractGuiItem> itemList) {
        selectionPointIndex = closePoint.index;
        attachedPath = null;

        //get the path renderer of the previous/next path if needed
        if (selectionPointIndex == 0) {
            //We clicked on the first point, so we need to get the path renderer of the previous path
            PathRenderer lastPathRenderer = null;
            for (AbstractGuiItem item : itemList) {
                if (item instanceof TrajectoryItem) {
                    TrajectoryItem trajectoryItem = (TrajectoryItem) item;
                    if (trajectoryItem.getPathRenderer() == this) {
                        attachedPath = lastPathRenderer;
                        break;
                    }
                    lastPathRenderer = trajectoryItem.getPathRenderer();
                }
            }
            isAttachedPathEnd = true;
        } else if (selectionPointIndex == controlVectors.size() - 1) {
            //We clicked on the last point, so we need to get the path renderer of the next path
            boolean foundMyself = false;
            for (AbstractGuiItem item : itemList) {
                if (item instanceof TrajectoryItem) {
                    TrajectoryItem trajectoryItem = (TrajectoryItem) item;
                    if (trajectoryItem.getPathRenderer() == this) {
                        foundMyself = true;
                    } else if (foundMyself) {
                        attachedPath = trajectoryItem.getPathRenderer();
                        break;
                    }
                }
            }
            isAttachedPathEnd = false;
        }

        if (attachedPath != null) {
            MovablePointRenderer selectedPoint = pointRenderList.get(selectionPointIndex);
            ControlVector otherPathPose2d;
            if (isAttachedPathEnd) {
                otherPathPose2d = attachedPath.controlVectors.get(attachedPath.controlVectors.size() - 1);
            } else {
                otherPathPose2d = attachedPath.controlVectors.get(0);
            }

            if (!(Math.abs(selectedPoint.getPos2().sub((float) otherPathPose2d.x[0], (float) otherPathPose2d.y[0]).len2())
                    < Math.pow((20 / config.getPointScaleFactor() * camera.zoom), 2))) {
                attachedPath = null;
                System.out.println("Not close enough to other path");
            }
        }

        MovablePointRenderer point = pointRenderList.get(selectionPointIndex);
        point.update(camera, mousePos, lastMousePos);
    }

    /**
     * Update the point that is selected. This should be called every frame.
     *
     * @param camera       the camera
     * @param mousePos     the current mouse position
     * @param lastMousePos the last mouse position
     */
    public void updatePoint(OrthographicCamera camera, Vector3 mousePos, Vector3 lastMousePos) {
        if (selectionPointIndex != -1) {
            MovablePointRenderer selectedPoint = pointRenderList.get(selectionPointIndex);
            selectedPoint.update(camera, mousePos, lastMousePos);
            if (controlPoint != null) {
                controlPoint.update(camera, mousePos, lastMousePos);
            }

            if (rotationPoint != null) {
                rotationPoint.update(camera, mousePos, lastMousePos);
            }

            //update the attached path if needed
            if (attachedPath != null) {
                if (isAttachedPathEnd) {
                    ControlVector attachedPathControlVector = attachedPath.controlVectors.get(attachedPath.controlVectors.size() - 1);
                    attachedPath.controlVectors.set(attachedPath.controlVectors.size() - 1, new ControlVector(
                            new double[]{selectedPoint.getPos2().x, attachedPathControlVector.x[1], attachedPathControlVector.x[2]},
                            new double[]{selectedPoint.getPos2().y, attachedPathControlVector.y[1], attachedPathControlVector.y[2]}));
                    attachedPath.pointRenderList.get(attachedPath.pointRenderList.size() - 1)
                            .setPosition(selectedPoint.getPos2());
                } else {
                    ControlVector attachedPathControlVector = attachedPath.controlVectors.get(0);
                    attachedPath.controlVectors.set(0, new ControlVector(
                            new double[]{selectedPoint.getPos2().x, attachedPathControlVector.x[1], attachedPathControlVector.x[2]},
                            new double[]{selectedPoint.getPos2().y, attachedPathControlVector.y[1], attachedPathControlVector.y[2]}));
                    attachedPath.pointRenderList.get(0).setPosition(selectedPoint.getPos2());
                }
                attachedPath.updatePath();
            }
        }
    }

    /**
     * Sets the time on the trajectory that the robot pose preview should be shown. This should be called every frame when it will
     * be shown.
     *
     * @param closePoint
     */
    public void setRobotPathPreviewPoint(CloseTrajectoryPoint closePoint) {
        this.robotPreviewTime = closePoint.pointTime;
        this.robotPreviewIndex = closePoint.prevPointIndex;
    }

    @Override
    public void onPointClick(@NotNull PointClickEvent event) {
        if (pointRenderList.contains(event.getPoint())) { //Should Only be called once per path
            if (event.isLeftClick()) {
                selectionPointIndex = pointRenderList.indexOf(event.getPoint());
                ControlVector controlVector = controlVectors.get(selectionPointIndex);

                float controlXPos = (float) (event.getPos().x + (controlVector.x[1] / CONTROL_VECTOR_SCALE));
                float controlYPos = (float) (event.getPos().y + (controlVector.y[1] / CONTROL_VECTOR_SCALE));
                controlPoint = new MovablePointRenderer(controlXPos, controlYPos, Color.GREEN, POINT_SIZE, this);


                float rotationXPos = (float) (event.getPos().x + rotation2dList.get(selectionPointIndex).getCos());
                float rotationYPos = (float) (event.getPos().y + rotation2dList.get(selectionPointIndex).getSin());
                rotationPoint = new MovablePointRenderer(rotationXPos, rotationYPos, Color.BLUE, POINT_SIZE, this);
            }
        }
    }


    @Override
    public void onPointMove(@NotNull PointMoveEvent event) {
        if (event.getPoint() == controlPoint) {
            MovablePointRenderer referencePoint = pointRenderList.get(selectionPointIndex);

            ControlVector previousControlVector = controlVectors.get(selectionPointIndex);
            ControlVector controlVector = new ControlVector(
                    new double[]{previousControlVector.x[0], (event.getNewPos().x - referencePoint.getX()) * CONTROL_VECTOR_SCALE, 0},
                    new double[]{previousControlVector.y[0], (event.getNewPos().y - referencePoint.getY()) * CONTROL_VECTOR_SCALE, 0});
            controlVectors.set(selectionPointIndex, controlVector);

        } else if (event.getPoint() == rotationPoint) {
            MovablePointRenderer referencePoint = pointRenderList.get(selectionPointIndex);
            Rotation2d rotation2d = new Rotation2d(Math.atan2(event.getNewPos().y - referencePoint.getY(), event.getNewPos().x - referencePoint.getX()));
            rotation2dList.set(selectionPointIndex, rotation2d);
            event.setPosition((float) (referencePoint.x + rotation2d.getCos()), (float) (referencePoint.y + rotation2d.getSin()));
        } else {
            selectionPointIndex = pointRenderList.indexOf(event.getPoint());
            ControlVector controlVector = controlVectors.get(selectionPointIndex);

            float xPos = (float) (event.getNewPos().x + (controlVector.x[1] / CONTROL_VECTOR_SCALE));
            float yPos = (float) (event.getNewPos().y + (controlVector.y[1] / CONTROL_VECTOR_SCALE));
            assert controlPoint != null;
            controlPoint.setPosition(xPos, yPos);

            float rotationXPos = (float) (event.getNewPos().x + rotation2dList.get(selectionPointIndex).getCos());
            float rotationYPos = (float) (event.getNewPos().y + rotation2dList.get(selectionPointIndex).getSin());
            assert rotationPoint != null;
            rotationPoint.setPosition(rotationXPos, rotationYPos);

            ControlVector previousControlVector = controlVectors.get(selectionPointIndex);
            ControlVector newControlVector = new ControlVector(
                    new double[]{event.getNewPos().x, previousControlVector.x[1], 0},
                    new double[]{event.getNewPos().y, previousControlVector.y[1], 0});
            controlVectors.set(selectionPointIndex, newControlVector);
        }
        updatePath();
    }

    public void updatePath() {
        updatePath(true);
    }

    public void updatePath(boolean updateListener) {
        // Generate the new path on another thread
        completableFutureTrajectory = CompletableFuture.supplyAsync(() -> {
            TrajectoryConfig trajectoryConfig = new TrajectoryConfig(config.getPathingConfig().maxVelocityMetersPerSecond,
                    config.getPathingConfig().maxAccelerationMetersPerSecondSq);
            for (TrajectoryConstraint trajectoryConstraint : config.getPathingConfig().trajectoryConstraints) {
                trajectoryConfig.addConstraint(trajectoryConstraint);
            }
            trajectoryConfig.setReversed(isReversed());
            trajectoryConfig.setStartVelocity(velocityStart);
            trajectoryConfig.setEndVelocity(velocityEnd);

            return trajectory = TrajectoryGenerator.generateTrajectory(controlVectors, trajectoryConfig);
        }, executorService);


        if (updateListener && pathChangeListener != null) pathChangeListener.onPathChange();
    }

    @NotNull
    public Trajectory getNotNullTrajectory() throws ExecutionException {
        try {
            return completableFutureTrajectory.get();
        } catch (InterruptedException e) {
            return null; //Should never happen
        }
    }

    @Nullable
    public Trajectory getTrajectory() {
        return trajectory;
    }

    public @NotNull ControlVectorList getControlVectors() {
        return controlVectors;
    }

    public @NotNull List<Rotation2d> getRotations() {
        return rotation2dList;
    }

    /**
     * @return List of rotation points and the times they should be used
     * @throws NullPointerException if the trajectory has not been generated yet. Call {@link #getTrajectory()} to get the
     *                              trajectory.
     */
    public List<TimedRotation> getRotationsAndTimes() {
        List<TimedRotation> rotationsAndTimes = new ArrayList<>(rotation2dList.size());
        int currentIndexPos = 0;
        rotationsAndTimes.add(new TimedRotation(0.0, rotation2dList.get(0)));
        for (double i = 0; i < Objects.requireNonNull(trajectory).getTotalTimeSeconds(); i += 0.01f) {
            Vector3 renderVector = MathUtil.toRenderVector3(trajectory.sample(i).poseMeters);

            if (currentIndexPos + 1 < controlVectors.size() &&
                    MathUtil.toRenderVector3(controlVectors.get(currentIndexPos + 1).x[0], controlVectors.get(currentIndexPos + 1).y[0])
                            .dst2(renderVector) < 8f) {
                currentIndexPos++;
                rotationsAndTimes.add(new TimedRotation(i, rotation2dList.get(0)));
            }

        }

        if (rotation2dList.size() - rotationsAndTimes.size() > 1) {
            NotificationHandler.addNotification(new Notification(
                    Color.ORANGE,
                    "Warning: Path has " + (rotation2dList.size() - rotationsAndTimes.size()) + " extra rotations at the end of the path",
                    2000));
        }

        for (int i = currentIndexPos; i < rotation2dList.size(); i++) {
            rotationsAndTimes.add(new TimedRotation(trajectory.getTotalTimeSeconds(), rotation2dList.get(i)));
        }

        assert rotationsAndTimes.size() == rotation2dList.size();
        return rotationsAndTimes;
    }

    public void removeSelection() {
        selectionPointIndex = -1;
        controlPoint = null;
        highlightPoint = null;

    }

    public @NotNull Color getColor() {
        return color;
    }

    public List<MovablePointRenderer> getPointList() {
        return pointRenderList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Points{");
        for (ControlVector controlVector : controlVectors) {
            stringBuilder.append("[Translation: (")
                    .append(controlVector.x[0]).append(", ").append(controlVector.y[0])
                    .append("), Control Point: (")
                    .append(controlVector.x[1]).append(", ").append(controlVector.y[1])
                    .append(")] ");
        }
        return "PathRenderer{" +
                "color=" + color +
                ", controlVectors=" + stringBuilder +
                '}';
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public float getVelocityStart() {
        return velocityStart;
    }

    public void setVelocityStart(float velocityStart) {
        this.velocityStart = velocityStart;
    }

    public float getVelocityEnd() {
        return velocityEnd;
    }

    public void setVelocityEnd(float velocityEnd) {
        this.velocityEnd = velocityEnd;
    }

    public int getSelectionPoint() {
        return selectionPointIndex;
    }

    /**
     * Draws the bounding box of the robot preview
     *
     * @param origin   Origin of the point (in pixels)
     * @param rotation Rotation of the point (in radians)
     * @param renderer shapeDrawer
     */
    public void renderRobotBoundingBox(Vector2 origin, float rotation, @NotNull ShapeDrawer renderer){
        Vector2 leftTop = new Vector2(origin).add(-(config.getRobotWidth() / 2) * config.getPointScaleFactor(), (config.getRobotLength() / 2) * config.getPointScaleFactor());
        Vector2 rightTop = new Vector2(origin).add((config.getRobotWidth() / 2) * config.getPointScaleFactor(), (config.getRobotLength() / 2) * config.getPointScaleFactor());
        Vector2 leftBottom = new Vector2(origin).add(-(config.getRobotWidth() / 2) * config.getPointScaleFactor(), -(config.getRobotLength() / 2) * config.getPointScaleFactor());
        Vector2 rightBottom = new Vector2(origin).add((config.getRobotWidth() / 2) * config.getPointScaleFactor(), -(config.getRobotLength() / 2) * config.getPointScaleFactor());

        leftTop.rotateAroundRad(origin, rotation);
        rightTop.rotateAroundRad(origin, rotation);
        leftBottom.rotateAroundRad(origin, rotation);
        rightBottom.rotateAroundRad(origin, rotation);

        renderer.setColor(getColor());
        renderer.line(leftTop, rightTop, LINE_THICKNESS);
        renderer.line(rightBottom, leftBottom, LINE_THICKNESS);
        renderer.line(leftBottom, leftTop, LINE_THICKNESS);

        renderer.setColor(Color.WHITE);
        renderer.line(rightTop, rightBottom, LINE_THICKNESS);
    }
}
