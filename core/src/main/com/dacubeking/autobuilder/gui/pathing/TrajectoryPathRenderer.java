package com.dacubeking.autobuilder.gui.pathing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.config.Config;
import com.dacubeking.autobuilder.gui.events.movablepoint.MovablePointEventHandler;
import com.dacubeking.autobuilder.gui.events.movablepoint.PointMoveEvent;
import com.dacubeking.autobuilder.gui.events.pathchange.PathChangeListener;
import com.dacubeking.autobuilder.gui.gui.hover.HoverManager;
import com.dacubeking.autobuilder.gui.gui.notification.Notification;
import com.dacubeking.autobuilder.gui.gui.notification.NotificationHandler;
import com.dacubeking.autobuilder.gui.gui.path.AbstractGuiItem;
import com.dacubeking.autobuilder.gui.gui.path.TrajectoryItem;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.pathing.pointclicks.ClosePoint;
import com.dacubeking.autobuilder.gui.pathing.pointclicks.CloseTrajectoryPoint;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import com.dacubeking.autobuilder.gui.util.Colors;
import com.dacubeking.autobuilder.gui.util.MathUtil;
import com.dacubeking.autobuilder.gui.util.shaders.ShaderLoader;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Pose2d;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Rotation2d;
import com.dacubeking.autobuilder.gui.wpi.math.spline.Spline;
import com.dacubeking.autobuilder.gui.wpi.math.spline.Spline.ControlVector;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.Trajectory;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.Trajectory.State;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.TrajectoryConfig;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.TrajectoryGenerator;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.TrajectoryGenerator.ControlVectorList;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TrajectoryPathRenderer extends PathRenderer implements MovablePointEventHandler, Serializable {

    @NotNull private static final ShaderProgram trajectoryShader = ShaderLoader.loadShader("trajectory");
    @NotNull private static final VertexAttributes shaderAttributes = new VertexAttributes(
            new VertexAttribute(Usage.Generic, 2, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.Generic, 1, "a_speedPercent")
    );
    private @Nullable Mesh mesh = null;
    @NotNull private final Color color;
    @NotNull private final List<Spline.ControlVector> controlVectors;
    @NotNull private final List<Rotation2d> rotation2dList;
    @NotNull private final List<MovablePointRenderer> pointRenderList;
    @NotNull private final ExecutorService executorService;
    @Nullable PathChangeListener pathChangeListener;
    boolean pointDeleted;
    CompletableFuture<Trajectory> completableFutureTrajectory;
    @Nullable private Trajectory trajectory;
    private boolean reversed = false;
    @Nullable private MovablePointRenderer controlPoint;
    @Nullable private MovablePointRenderer rotationPoint;
    @Nullable private PointRenderer highlightPoint;
    private int selectionPointIndex = -1;
    private float velocityStart;
    private float velocityEnd;
    private double robotPreviewTime = -1;
    @Nullable private TrajectoryPathRenderer attachedPath;
    private boolean isAttachedPathEnd;

    @NotNull Vector2 lastPointLeft = new Vector2();
    @NotNull Vector2 lastPointRight = new Vector2();
    @NotNull Vector2 nextPointLeft = new Vector2();
    @NotNull Vector2 nextPointRight = new Vector2();
    private int robotPreviewIndex;
    @NotNull private final List<TrajectoryConstraint> constraints;

    // Reused objects
    private final float @NotNull [] colorHsv = new float[3];


    DecimalFormat df = new DecimalFormat("#.##");

    public TrajectoryPathRenderer(@NotNull Color color, @NotNull ControlVectorList pointList,
                                  @NotNull List<Rotation2d> rotation2dList,
                                  @NotNull ExecutorService executorService, float velocityStart, float velocityEnd,
                                  @NotNull List<TrajectoryConstraint> constraints) {
        this.color = color;
        this.controlVectors = Collections.synchronizedList(pointList);
        this.rotation2dList = rotation2dList;

        pointRenderList = new ArrayList<>();

        for (ControlVector controlVector : controlVectors) {
            pointRenderList.add(
                    new MovablePointRenderer((float) controlVector.x[0], (float) controlVector.y[0], color,
                            AutoBuilder.getPointSize(), this));
        }

        this.executorService = executorService;

        this.velocityStart = velocityStart;
        this.velocityEnd = velocityEnd;
        this.constraints = constraints;
        updatePath();
    }


    public void setPathChangeListener(@NotNull PathChangeListener pathChangeListener) {
        this.pathChangeListener = pathChangeListener;
        updatePath();
    }

    private final @NotNull AtomicBoolean isDrawingCached = new AtomicBoolean(false);

    @Override
    protected void deleteRenderCache() {
        isDrawingCached.set(false);
        if (mesh != null) {
            mesh.dispose();
        }
        mesh = null;
    }

    @Override
    public void render(@NotNull ShapeDrawer renderer, @NotNull OrthographicCamera cam) {
        Config config = AutoBuilder.getConfig();
        float pointScaleFactor = config.getPointScaleFactor();
        //Get the first 2 points of the line at t = 0
        lastPointLeft.set(0, -AutoBuilder.getLineThickness() / 2);
        lastPointRight.set(0, AutoBuilder.getLineThickness() / 2);

        if (trajectory != null) {
            List<State> states = trajectory.getStates();
            if (states.size() > 0) {
                if (!isDrawingCached.getAndSet(true) || mesh == null) {
                    mesh = new Mesh(true, states.size() * 6 * 3, 0, shaderAttributes);
                    lastPointLeft.rotateRad((float) states.get(0).poseMeters.getRotation().getRadians());
                    lastPointRight.rotateRad((float) states.get(0).poseMeters.getRotation().getRadians());

                    lastPointLeft.add((float) states.get(0).poseMeters.getTranslation().getX() * pointScaleFactor,
                            (float) states.get(0).poseMeters.getTranslation().getY() * pointScaleFactor);
                    lastPointRight.add((float) states.get(0).poseMeters.getTranslation().getX() * pointScaleFactor,
                            (float) states.get(0).poseMeters.getTranslation().getY() * pointScaleFactor);
                    double maxSpeed = AutoBuilder.getConfig().getPathingConfig().maxVelocityMetersPerSecond;
                    float lastSpeedPercent = (float) (states.get(0).velocityMetersPerSecond / maxSpeed);


                    int i = 0;
                    float[] vertexData = new float[states.size() * 6 * 3];


                    for (State state : states) {
                        Pose2d cur = state.poseMeters;

                        //Use the speed of the path to determine its saturation
                        float speedPercent = (float) (Math.abs(state.velocityMetersPerSecond) / maxSpeed);

                        //Get the 2 points of the line at the current time
                        nextPointLeft.set(0, -AutoBuilder.getLineThickness() / 2);
                        nextPointRight.set(0, AutoBuilder.getLineThickness() / 2);

                        nextPointLeft.rotateRad((float) cur.getRotation().getRadians());
                        nextPointRight.rotateRad((float) cur.getRotation().getRadians());

                        nextPointLeft.add((float) cur.getTranslation().getX() * pointScaleFactor,
                                (float) cur.getTranslation().getY() * pointScaleFactor);
                        nextPointRight.add((float) cur.getTranslation().getX() * pointScaleFactor,
                                (float) cur.getTranslation().getY() * pointScaleFactor);


                        //Add the 4 vertices of the quad to the vertex data
                        // Triangle 1
                        vertexData[i++] = lastPointLeft.x;
                        vertexData[i++] = lastPointLeft.y;
                        vertexData[i++] = lastSpeedPercent;

                        vertexData[i++] = lastPointRight.x;
                        vertexData[i++] = lastPointRight.y;
                        vertexData[i++] = lastSpeedPercent;


                        vertexData[i++] = nextPointLeft.x;
                        vertexData[i++] = nextPointLeft.y;
                        vertexData[i++] = speedPercent;

                        // Triangle 2
                        vertexData[i++] = nextPointLeft.x;
                        vertexData[i++] = nextPointLeft.y;
                        vertexData[i++] = speedPercent;

                        vertexData[i++] = lastPointRight.x;
                        vertexData[i++] = lastPointRight.y;
                        vertexData[i++] = lastSpeedPercent;

                        vertexData[i++] = nextPointRight.x;
                        vertexData[i++] = nextPointRight.y;
                        vertexData[i++] = speedPercent;

                        //Set the last point to the current point
                        lastPointLeft.set(nextPointLeft);
                        lastPointRight.set(nextPointRight);
                        lastSpeedPercent = speedPercent;
                    }
                    mesh.setVertices(vertexData);
                }
                renderer.getBatch().end();


                color.toHsv(colorHsv);

                trajectoryShader.bind();
                trajectoryShader.setUniformMatrix("u_projTrans", renderer.getBatch().getProjectionMatrix());
                trajectoryShader.setUniformf("u_colorhv", colorHsv[0] / 360, colorHsv[2]);

                mesh.render(trajectoryShader, GL20.GL_TRIANGLES);

                renderer.getBatch().begin();
            }
        }


        if (controlPoint != null) {
            PointRenderer selectedPoint = pointRenderList.get(selectionPointIndex);
            renderer.line(selectedPoint.getRenderPos2(), controlPoint.getRenderPos2(), Color.WHITE,
                    AutoBuilder.getLineThickness());
            controlPoint.draw(renderer);

            if (rotationPoint != null && config.isHolonomic()) {
                renderer.line(selectedPoint.getRenderPos2(), rotationPoint.getRenderPos2(), Color.WHITE,
                        AutoBuilder.getLineThickness());
                rotationPoint.draw(renderer);
            }

            Vector2 origin = selectedPoint.getRenderPos2();
            float rotation = (float) (config.isHolonomic() ? rotation2dList.get(selectionPointIndex).getRadians() :
                    Math.atan2(controlVectors.get(selectionPointIndex).y[1], controlVectors.get(selectionPointIndex).x[1]));

            renderRobotBoundingBox(origin, rotation, renderer, getColor(), Color.WHITE);
        } else if (robotPreviewTime >= 0) {
            if (trajectory != null) {
                State state = trajectory.sample(robotPreviewTime);
                Vector2 origin = MathUtil.toRenderVector2(state.poseMeters);
                float rotation = (float) (config.isHolonomic() ? rotation2dList.get(robotPreviewIndex).getRadians() :
                        state.poseMeters.getRotation().getRadians());
                renderRobotBoundingBox(origin, rotation, renderer, getColor(), Color.WHITE);

                HoverManager.setHoverText(new TextBlock(Fonts.ROBOTO, 13, 300,
                        new TextComponent("Pose: x: ").setBold(true),
                        new TextComponent(df.format(state.poseMeters.getX()) + "m"),
                        new TextComponent(" y: ").setBold(true),
                        new TextComponent(df.format(state.poseMeters.getY()) + "m"),
                        new TextComponent(" theta: ").setBold(true),
                        new TextComponent(df.format(state.poseMeters.getRotation().getDegrees()) + "°\n"),
                        new TextComponent("Velocity: ").setBold(true),
                        new TextComponent(df.format(state.velocityMetersPerSecond) + "m/s\n"),
                        new TextComponent("Acceleration: ").setBold(true),
                        new TextComponent(df.format(state.accelerationMetersPerSecondSq) + "m/s²\n"),
                        new TextComponent("Curvature: ").setBold(true),
                        new TextComponent(df.format(Math.toDegrees(state.curvatureRadPerMeter)) + "°/m\n"),
                        new TextComponent("Time: ").setBold(true),
                        new TextComponent(df.format(state.timeSeconds) + "s")), 0, Gdx.graphics.getHeight() - 2);
            }
        }

        for (int i = 0; i < pointRenderList.size(); i++) {
            PointRenderer pointRenderer = pointRenderList.get(i);

            if (i == selectionPointIndex) {
                if (highlightPoint == null) {
                    highlightPoint = new PointRenderer(pointRenderer.getPos2(), Color.WHITE, 1.4f);
                } else {
                    highlightPoint.setPosition(pointRenderer.getPos2());
                }

                highlightPoint.draw(renderer);
            }
            pointRenderer.draw(renderer);
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
     *
     * @param maxDistance2 The maximum distance to the mouse position squared.
     * @return List of all points on the trajectory that are close to the mouse position.
     */
    @Override
    public @NotNull ArrayList<CloseTrajectoryPoint> getCloseTrajectoryPoints(float maxDistance2, Vector3 mousePos) {
        if (trajectory != null) {
            ArrayList<CloseTrajectoryPoint> closePoints = new ArrayList<>();
            int currentIndexPos = 0;
            Vector2 mousePosVector2 = new Vector2(mousePos.x, mousePos.y);
            for (int i = 0; i < trajectory.getStates().size() - 1; i++) {
                State state = trajectory.getStates().get(i);
                State stateNext = trajectory.getStates().get(i + 1);
                Vector2 renderVector = MathUtil.toRenderVector2(state.poseMeters);
                Vector2 renderVectorNext = MathUtil.toRenderVector2(stateNext.poseMeters);
                if (currentIndexPos + 1 < controlVectors.size() &&
                        MathUtil.toRenderVector2(controlVectors.get(currentIndexPos + 1).x[0],
                                        controlVectors.get(currentIndexPos + 1).y[0])
                                .dst2(renderVector) < 8f * state.velocityMetersPerSecond) {
                    currentIndexPos++;
                }

                Vector2 closestPoint = MathUtil.getClosestPointOnSegment(renderVector, renderVectorNext, mousePosVector2);

                double time = state.timeSeconds;
                if (!(closestPoint.epsilonEquals(renderVector, 0.01f) || closestPoint.epsilonEquals(renderVectorNext, 0.01f))) {
                    Vector2 distanceBetweenStates = renderVectorNext.cpy().sub(renderVector);
                    float distanceToClosestPoint = renderVector.dst(closestPoint);
                    float lenBetweenStates = distanceBetweenStates.len();
                    double interpolationAmount = distanceToClosestPoint / lenBetweenStates;

                    time = state.timeSeconds + interpolationAmount * (stateNext.timeSeconds - state.timeSeconds);
                } else if (closestPoint.epsilonEquals(renderVectorNext, 0.01f)) {
                    time = stateNext.timeSeconds;
                }


                float distanceToMouse2 = closestPoint.dst2(mousePosVector2);
                if (distanceToMouse2 < maxDistance2) {
                    closePoints.add(new CloseTrajectoryPoint(distanceToMouse2, this, currentIndexPos, time));
                }
            }
            return closePoints;
        }
        return new ArrayList<>();
    }

    /**
     * Delete a point
     *
     * @param closePoint object that was created by {@link TrajectoryPathRenderer#getClosePoints(float, Vector3)}
     */
    public void deletePoint(ClosePoint closePoint) {
        if (controlVectors.size() > 2) {
            if (selectionPointIndex > closePoint.index()) {
                selectionPointIndex--;
            } else if (selectionPointIndex == closePoint.index()) {
                removeSelection();
            }
            pointRenderList.remove(closePoint.index());
            controlVectors.remove(closePoint.index());
            rotation2dList.remove(closePoint.index());
            pointDeleted = true;
            updatePath();
        }
    }

    /**
     * Add a point
     *
     * @param closePoint object that was created by {@link TrajectoryPathRenderer#getCloseTrajectoryPoints(float, Vector3)}
     */
    public void addPoint(CloseTrajectoryPoint closePoint) {
        assert trajectory != null;
        double time = closePoint.pointTime();
        State newPoint = trajectory.sample(closePoint.pointTime());

        Vector2 controlVector;

        if (time - 0.1 >= 0 && time + 0.1 <= trajectory.getTotalTimeSeconds()) {
            Pose2d before = trajectory.sample(time - 0.1).poseMeters;
            Pose2d after = trajectory.sample(time + 0.1).poseMeters;
            controlVector = new Vector2(
                    (float) (after.getX() - before.getX()),
                    (float) (after.getY() - before.getY()));
            controlVector.setLength(1);
        } else {
            controlVector = new Vector2(1, 0);
        }

        controlVectors.add(closePoint.prevPointIndex() + 1,
                new ControlVector(
                        new double[]{newPoint.poseMeters.getX(), controlVector.x, 0},
                        new double[]{newPoint.poseMeters.getY(), controlVector.y, 0}
                ));

        pointRenderList.add(closePoint.prevPointIndex() + 1,
                new MovablePointRenderer(
                        (float) newPoint.poseMeters.getX(),
                        (float) newPoint.poseMeters.getY(),
                        color, AutoBuilder.getPointSize(), this
                ));

        rotation2dList.add(closePoint.prevPointIndex() + 1, newPoint.poseMeters.getRotation());

        if (selectionPointIndex > closePoint.prevPointIndex()) {
            selectionPointIndex++;
        }
        updatePath();
        UndoHandler.getInstance().somethingChanged();
    }

    /**
     * @param mousePos current mouse position
     * @return The distance to the closest point that is either the rotation or control point. If neither exist
     * <code>Double.MAX_VALUE</code>
     */
    @Override
    public double distToClosestPointNotMainPoint(Vector3 mousePos) {
        if (rotationPoint == null && controlPoint == null) {
            return Double.MAX_VALUE;
        }
        if (rotationPoint == null) {
            return controlPoint.getRenderPos3().dst2(mousePos);
        }
        if (controlPoint == null) {
            return rotationPoint.getRenderPos3().dst2(mousePos);
        }
        return Math.min(rotationPoint.getRenderPos3().dst2(mousePos), controlPoint.getRenderPos3().dst2(mousePos));
    }

    /**
     * @param closePoint object that was created by {@link TrajectoryPathRenderer#getClosePoints(float, Vector3)}
     * @param camera     the camera
     * @param mousePos   the current mouse position
     * @param mouseDiff  the difference between the current and last mouse position
     * @param itemList   the list of gui items that contains all the path items
     */
    public void selectPoint(@NotNull ClosePoint closePoint, @NotNull OrthographicCamera camera, @NotNull Vector3 mousePos,
                            @NotNull Vector3 mouseDiff, @NotNull List<AbstractGuiItem> itemList) {
        selectionPointIndex = closePoint.index();
        attachedPath = null;

        //get the path renderer of the previous/next path if needed
        if (selectionPointIndex == 0) {
            //We clicked on the first point, so we need to get the path renderer of the previous path
            TrajectoryPathRenderer lastTrajectoryPathRenderer = null;
            for (AbstractGuiItem item : itemList) {
                if (item instanceof TrajectoryItem trajectoryItem) {
                    if (trajectoryItem.getPathRenderer() == this) {
                        attachedPath = lastTrajectoryPathRenderer;
                        break;
                    }
                    lastTrajectoryPathRenderer = trajectoryItem.getPathRenderer();
                }
            }
            isAttachedPathEnd = true;
        } else if (selectionPointIndex == controlVectors.size() - 1) {
            //We clicked on the last point, so we need to get the path renderer of the next path
            boolean foundMyself = false;
            for (AbstractGuiItem item : itemList) {
                if (item instanceof TrajectoryItem trajectoryItem) {
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

            if (Math.abs(selectedPoint.getPos2().sub((float) otherPathPose2d.x[0], (float) otherPathPose2d.y[0]).len2())
                    < Math.pow((20 / AutoBuilder.getConfig().getPointScaleFactor() * camera.zoom), 2)) {
                updateConnectedPath(selectedPoint);
            } else {
                attachedPath = null;
            }
        }

        MovablePointRenderer point = pointRenderList.get(selectionPointIndex);

        ControlVector controlVector = controlVectors.get(selectionPointIndex);
        Vector2 pos2 = point.getPos2();
        float controlXPos = (float) (pos2.x + (controlVector.x[1] / AutoBuilder.CONTROL_VECTOR_SCALE));
        float controlYPos = (float) (pos2.y + (controlVector.y[1] / AutoBuilder.CONTROL_VECTOR_SCALE));
        controlPoint = new MovablePointRenderer(controlXPos, controlYPos, Color.GREEN, AutoBuilder.getPointSize(), this);


        float rotationXPos = (float) (pos2.x + rotation2dList.get(selectionPointIndex).getCos());
        float rotationYPos = (float) (pos2.y + rotation2dList.get(selectionPointIndex).getSin());
        rotationPoint = new MovablePointRenderer(rotationXPos, rotationYPos, Color.BLUE, AutoBuilder.getPointSize(), this);
    }

    private enum PointType {
        CONTROL, ROTATION, POSITION
    }

    private PointType controlPointSelected = PointType.POSITION;

    /**
     * Update the point that is selected. This should be called every frame.
     *
     * @param camera   the camera
     * @param mousePos the current mouse position
     */
    @Override
    public void updatePoint(OrthographicCamera camera, Vector3 mousePos, Vector3 mouseDiff) {
        if (selectionPointIndex != -1) {
            MovablePointRenderer selectedPoint = pointRenderList.get(selectionPointIndex);
            if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
                float distToCtrlPoint = controlPoint != null ? controlPoint.getRenderPos3().dst2(mousePos) : Float.MAX_VALUE;
                float distToRotPoint = rotationPoint != null ? rotationPoint.getRenderPos3().dst2(mousePos) : Float.MAX_VALUE;
                float distToPoint = selectedPoint.getRenderPos3().dst2(mousePos);

                if (distToCtrlPoint < distToPoint && distToCtrlPoint < distToRotPoint) {
                    controlPointSelected = PointType.CONTROL;
                } else if (distToRotPoint < distToPoint && distToRotPoint < distToCtrlPoint) {
                    controlPointSelected = PointType.ROTATION;
                } else {
                    controlPointSelected = PointType.POSITION;
                }
            }

            if (controlPointSelected == PointType.CONTROL) {
                if (controlPoint != null) {
                    controlPoint.update(camera, mousePos, mouseDiff);
                }
            } else if (controlPointSelected == PointType.ROTATION) {
                if (rotationPoint != null) {
                    rotationPoint.update(camera, mousePos, mouseDiff);
                }
            } else {
                selectedPoint.update(camera, mousePos, mouseDiff);
            }


            //update the attached path if needed
            if ((!mouseDiff.isZero() || (isAttachedPathEnd)) && Gdx.input.isButtonPressed(Buttons.LEFT)) {
                updateConnectedPath(selectedPoint);
            }
        }
    }

    private void updateConnectedPath(MovablePointRenderer selectedPoint) {
        if (attachedPath == null) {
            return;
        }
        Config config = AutoBuilder.getConfig();
        if (isAttachedPathEnd) {
            ControlVector attachedPathControlVector = attachedPath.controlVectors.get(
                    attachedPath.controlVectors.size() - 1);

            Vector2 attachedPathVector = new Vector2((float) attachedPathControlVector.x[1],
                    (float) attachedPathControlVector.y[1]);
            if (!config.isHolonomic()) {
                ControlVector thisControlVector = controlVectors.get(0);
                attachedPathVector.setAngleRad((float) Math.atan2(thisControlVector.y[1], thisControlVector.x[1]));
            }

            attachedPath.controlVectors.set(attachedPath.controlVectors.size() - 1, new ControlVector(
                    new double[]{selectedPoint.getPos2().x, attachedPathVector.x, attachedPathControlVector.x[2]},
                    new double[]{selectedPoint.getPos2().y, attachedPathVector.y, attachedPathControlVector.y[2]}));
            attachedPath.pointRenderList.get(attachedPath.pointRenderList.size() - 1)
                    .setPosition(selectedPoint.getPos2());
        } else {
            ControlVector attachedPathControlVector = attachedPath.controlVectors.get(0);

            Vector2 attachedPathVector = new Vector2((float) attachedPathControlVector.x[1],
                    (float) attachedPathControlVector.y[1]);
            if (!config.isHolonomic()) {
                ControlVector thisControlVector = controlVectors.get(controlVectors.size() - 1);
                attachedPathVector.setAngleRad((float) Math.atan2(thisControlVector.y[1], thisControlVector.x[1]));
            }

            attachedPath.controlVectors.set(0, new ControlVector(
                    new double[]{selectedPoint.getPos2().x, attachedPathVector.x, attachedPathControlVector.x[2]},
                    new double[]{selectedPoint.getPos2().y, attachedPathVector.y, attachedPathControlVector.y[2]}));
            attachedPath.pointRenderList.get(0).setPosition(selectedPoint.getPos2());
        }
        attachedPath.updatePath();
    }

    /**
     * Sets the time on the trajectory that the robot pose preview should be shown. This should be called every frame when it will
     * be shown.
     */
    @Override
    public void setRobotPathPreviewPoint(CloseTrajectoryPoint closePoint) {
        this.robotPreviewTime = closePoint.pointTime();
        this.robotPreviewIndex = closePoint.prevPointIndex();
    }


    @Override
    public void onPointMove(@NotNull PointMoveEvent event) {
        if (event.getPoint() == controlPoint) {
            MovablePointRenderer referencePoint = pointRenderList.get(selectionPointIndex);

            ControlVector previousControlVector = controlVectors.get(selectionPointIndex);
            float dist2 = MathUtil.dist2(event.getNewPos().x, event.getNewPos().y, referencePoint.getX(), referencePoint.getY());
            double x;
            double y;
            if (dist2 < Math.pow(AutoBuilder.MIN_CONTROL_VECTOR_DISTANCE, 2)) {
                double angle = Math.atan2(event.getNewPos().y - referencePoint.getY(),
                        event.getNewPos().x - referencePoint.getX());
                x = Math.cos(angle) * AutoBuilder.MIN_CONTROL_VECTOR_DISTANCE;
                y = Math.sin(angle) * AutoBuilder.MIN_CONTROL_VECTOR_DISTANCE;
                event.setPosition((float) x + referencePoint.getX(), (float) y + referencePoint.getY());
            } else {
                x = event.getNewPos().x - referencePoint.getX();
                y = event.getNewPos().y - referencePoint.getY();
            }

            ControlVector controlVector = new ControlVector(
                    new double[]{previousControlVector.x[0], x * AutoBuilder.CONTROL_VECTOR_SCALE, 0},
                    new double[]{previousControlVector.y[0], y * AutoBuilder.CONTROL_VECTOR_SCALE, 0});
            controlVectors.set(selectionPointIndex, controlVector);
        } else if (event.getPoint() == rotationPoint) {
            MovablePointRenderer referencePoint = pointRenderList.get(selectionPointIndex);
            Rotation2d rotation2d = new Rotation2d(
                    Math.atan2(event.getNewPos().y - referencePoint.getY(), event.getNewPos().x - referencePoint.getX()));
            rotation2dList.set(selectionPointIndex, rotation2d);
            event.setPosition((float) (referencePoint.x + rotation2d.getCos()), (float) (referencePoint.y + rotation2d.getSin()));
        } else {
            selectionPointIndex = pointRenderList.indexOf(event.getPoint());
            ControlVector controlVector = controlVectors.get(selectionPointIndex);

            float xPos = (float) (event.getNewPos().x + (controlVector.x[1] / AutoBuilder.CONTROL_VECTOR_SCALE));
            float yPos = (float) (event.getNewPos().y + (controlVector.y[1] / AutoBuilder.CONTROL_VECTOR_SCALE));
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
        updatePath(true, false);
    }

    int updateRequestedPathCounter = 0;
    int updatedPathCounter = 0;
    int forceUpdateCounter = -1;
    private final Lock trajectoryMutex = new ReentrantLock();

    public void updatePath(boolean updateListener) {
        updatePath(updateListener, false);
    }

    public void updatePath(boolean updateListener, boolean mustUpdate) {
        Config config = AutoBuilder.getConfig();
        TrajectoryConfig trajectoryConfig = new TrajectoryConfig(config.getPathingConfig().maxVelocityMetersPerSecond,
                config.getPathingConfig().maxAccelerationMetersPerSecondSq);
        for (TrajectoryConstraint trajectoryConstraint : config.getPathingConfig().trajectoryConstraints) {
            trajectoryConfig.addConstraint(trajectoryConstraint);
        }

        for (TrajectoryConstraint constraint : constraints) {
            trajectoryConfig.addConstraint(constraint);
        }

        trajectoryConfig.setReversed(isReversed() && !config.isHolonomic());
        trajectoryConfig.setStartVelocity(velocityStart);
        trajectoryConfig.setEndVelocity(velocityEnd);

        trajectoryMutex.lock();
        try {
            updateRequestedPathCounter += 1;
            if (forceUpdateCounter < updatedPathCounter) {
                forceUpdateCounter = updateRequestedPathCounter;
            }
        } finally {
            trajectoryMutex.unlock();
        }

        // Generate the new path on another thread
        final ControlVectorList controlVectorsList = new ControlVectorList(controlVectors);
        completableFutureTrajectory = CompletableFuture.supplyAsync(() -> {
            trajectoryMutex.lock();
            try {
                updatedPathCounter += 1;
                if (updatedPathCounter != updateRequestedPathCounter && !mustUpdate
                        && forceUpdateCounter != updatedPathCounter) {
                    return null;
                }
            } finally {
                trajectoryMutex.unlock();
            }

            Trajectory trajectory;
            try {
                trajectory = TrajectoryGenerator.generateTrajectory(controlVectorsList, trajectoryConfig);
            } catch (Exception e) {
                NotificationHandler.addNotification(new Notification(Colors.LIGHT_RED,
                        new TextBlock(Fonts.ROBOTO, 18,
                                new TextComponent(e.getClass().getSimpleName() + ": \n" + e.getMessage())),
                        3000));
                e.printStackTrace();
                throw e;
            }

            AutoBuilder.requestRendering();
            this.trajectory = trajectory;
            isDrawingCached.set(false);
            return trajectory;
        }, executorService);

        completableFutureTrajectory.thenRun(() -> UndoHandler.getInstance().triggerSave());


        if (updateListener && pathChangeListener != null) {
            pathChangeListener.onPathChange();
        }
    }


    /**
     * If a point is currently selected, it will update the positions of the handles to match the values stored in the
     * trajectory.
     */
    public void updatePointHandles() {
        if (controlPoint != null) {
            MovablePointRenderer point = pointRenderList.get(selectionPointIndex);

            ControlVector controlVector = controlVectors.get(selectionPointIndex);
            Vector2 pos2 = point.getPos2();
            float controlXPos = (float) (pos2.x + (controlVector.x[1] / AutoBuilder.CONTROL_VECTOR_SCALE));
            float controlYPos = (float) (pos2.y + (controlVector.y[1] / AutoBuilder.CONTROL_VECTOR_SCALE));
            controlPoint.setPosition(controlXPos, controlYPos);


            if (rotationPoint != null) {
                float rotationXPos = (float) (pos2.x + rotation2dList.get(selectionPointIndex).getCos());
                float rotationYPos = (float) (pos2.y + rotation2dList.get(selectionPointIndex).getSin());
                rotationPoint.setPosition(rotationXPos, rotationYPos);
            }
        }
    }

    @NotNull
    public Trajectory getNotNullTrajectory() throws ExecutionException {
        try {
            Trajectory trajectory = completableFutureTrajectory.get();
            if (trajectory == null) {
                updatePath(false, true);
                trajectory = completableFutureTrajectory.get();
            }
            return trajectory;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public Trajectory getTrajectory() {
        return trajectory;
    }

    public @NotNull List<Spline.ControlVector> getControlVectors() {
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
                    MathUtil.toRenderVector3(controlVectors.get(currentIndexPos + 1).x[0],
                                    controlVectors.get(currentIndexPos + 1).y[0])
                            .dst2(renderVector) < 8f) {
                currentIndexPos++;
                rotationsAndTimes.add(new TimedRotation(i, rotation2dList.get(currentIndexPos)));
            }
        }

        if (rotation2dList.size() - rotationsAndTimes.size() > 1) {
            NotificationHandler.addNotification(new Notification(
                    Color.ORANGE,
                    "Warning: Path has " + (rotation2dList.size() - rotationsAndTimes.size()) + " extra rotations at the end of" +
                            " the path",
                    2000));
        }

        for (int i = currentIndexPos + 1; i < rotation2dList.size(); i++) {
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

    public @NotNull List<TrajectoryConstraint> getConstraints() {
        return constraints;
    }
}
