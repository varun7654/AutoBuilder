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
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.pathing.pointclicks.ClosePoint;
import me.varun.autobuilder.pathing.pointclicks.CloseTrajectoryPoint;
import me.varun.autobuilder.util.MathUtil;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import me.varun.autobuilder.wpi.math.geometry.Translation2d;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryConfig;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator;
import me.varun.autobuilder.wpi.math.trajectory.constraint.TrajectoryConstraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static me.varun.autobuilder.AutoBuilder.*;

public class PathRenderer implements MovablePointEventHandler, Serializable {
    @NotNull private final Color color;
    @NotNull private final List<Pose2d> point2DList;
    @NotNull private final List<MovablePointRenderer> pointRenderList;
    @NotNull private final ExecutorService executorService;
    @Nullable PathChangeListener pathChangeListener;
    boolean pointDeleted;
    CompletableFuture<Trajectory> completableFutureTrajectory;
    Config config = AutoBuilder.getConfig();
    @Nullable private Trajectory trajectory;
    private boolean reversed = false;
    @Nullable private MovablePointRenderer rotationPoint;
    @Nullable private PointRenderer highlightPoint;
    private int selectionPointIndex = -1;
    private float velocityStart = 0;
    private float velocityEnd = 0;
    private float robotPreviewTime = -1;
    @Nullable private PathRenderer attachedPath;
    private boolean isAttachedPathEnd;

    @NotNull Vector2 lastPointLeft = new Vector2();
    @NotNull Vector2 lastPointRight = new Vector2();
    @NotNull Vector2 nextPointLeft = new Vector2();
    @NotNull Vector2 nextPointRight = new Vector2();

    public PathRenderer(@NotNull Color color, @NotNull List<Pose2d> pointList, @NotNull ExecutorService executorService,
                        float velocityStart, float velocityEnd) {
        this.color = color;
        this.point2DList = pointList;

        pointRenderList = new ArrayList<>();

        for (Pose2d pose2d : point2DList) {
            pointRenderList.add(new MovablePointRenderer((float) pose2d.getX(), (float) pose2d.getY(), color, POINT_SIZE, this));
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
            double speed = Math.abs(trajectory.sample(i).velocityMetersPerSecond);
            float[] color = new float[3];
            this.color.toHsv(color);
            color[1] = (float) (0.9 * (speed / maxVelocityMetersPerSecond) + 0.1);
            Color speedColor = new Color().fromHsv(color);
            speedColor.set(speedColor.r, speedColor.g, speedColor.b, 1);
            nextPointLeft.set(0, -LINE_THICKNESS / 2);
            nextPointRight.set(0, LINE_THICKNESS / 2);

            nextPointLeft.rotateRad((float) cur.getRotation().getRadians());
            nextPointRight.rotateRad((float) cur.getRotation().getRadians());

            nextPointLeft.add((float) cur.getTranslation().getX() * config.getPointScaleFactor(),
                    (float) cur.getTranslation().getY() * config.getPointScaleFactor());
            nextPointRight.add((float) cur.getTranslation().getX() * config.getPointScaleFactor(),
                    (float) cur.getTranslation().getY() * config.getPointScaleFactor());

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

        if (rotationPoint != null) {
            renderer.line(pointRenderList.get(selectionPointIndex).getRenderPos2(), rotationPoint.getRenderPos2(), LINE_THICKNESS);
            rotationPoint.draw(renderer, cam);

            float rotation = (float) point2DList.get(selectionPointIndex).getRotation().getRadians();
            Vector2 origin = pointRenderList.get(selectionPointIndex).getRenderPos2();

            renderRobotBoundingBox(origin, rotation, renderer);
        } else if (robotPreviewTime >= 0) {
            Pose2d hoverPose = trajectory.sample(robotPreviewTime).poseMeters;
            float rotation = (float) hoverPose.getRotation().getRadians();
            Vector2 origin = MathUtil.toRenderVector2(hoverPose);

            renderRobotBoundingBox(origin, rotation, renderer);
        }

        for (int i = 0; i < pointRenderList.size(); i++) {
            PointRenderer pointRenderer = pointRenderList.get(i);

            if (i == selectionPointIndex) {
                if (highlightPoint == null)
                    highlightPoint = new PointRenderer(pointRenderer.getPos2(), Color.WHITE, POINT_SIZE * 1.4f);
                else highlightPoint.setPosition(pointRenderer.getPos2());

                highlightPoint.draw(renderer, cam);
            }
            pointRenderer.draw(renderer, cam);
        }

        robotPreviewTime = -1;
    }

    /**
     * Get points that are close to the mouse position and returns a list of them.
     * @param maxDistance2 The maximum distance to the mouse position squared.
     * @param mousePos
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

                if (currentIndexPos + 1 < point2DList.size() &&
                        MathUtil.toRenderVector3(point2DList.get(currentIndexPos + 1)).dst2(renderVector) < 2f) {
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

    public void deletePoint(ClosePoint closePoint) {
        if (point2DList.size() > 2) {
            if (selectionPointIndex > closePoint.index) {
                selectionPointIndex--;
            } else if (selectionPointIndex == closePoint.index) {
                removeSelection();
            }
            pointRenderList.remove(closePoint.index);
            point2DList.remove(closePoint.index);
            pointDeleted = true;
            updatePath();
        }
    }

    public void addPoint(CloseTrajectoryPoint closePoint) {
        System.out.println("Adding point after " + closePoint.prevPointIndex);
        assert trajectory != null;
        Pose2d newPoint = trajectory.sample(closePoint.pointTime).poseMeters;
        point2DList.add(closePoint.prevPointIndex + 1, newPoint);
        pointRenderList.add(closePoint.prevPointIndex + 1, new MovablePointRenderer((float) newPoint.getX(), (float) newPoint.getY(), color, POINT_SIZE, this));
        if (selectionPointIndex > closePoint.prevPointIndex) selectionPointIndex++;
        updatePath();
        UndoHandler.getInstance().somethingChanged();
    }

    public boolean isTouchingRotationPoint(Vector3 mousePos, float maxDistance2) {
        if (rotationPoint == null) return false;
        return rotationPoint.getRenderPos3().dst2(mousePos) < maxDistance2;
    }

    public void selectPoint(ClosePoint closePoint, OrthographicCamera camera, Vector3 mousePos, Vector3 lastMousePos, List<AbstractGuiItem> itemList) {
        selectionPointIndex = closePoint.index;
        attachedPath = null;

        //get the path renderer of the previous/next path if needed
        if(selectionPointIndex == 0){ //Get previous path
            PathRenderer lastPathRenderer = null;
            for (AbstractGuiItem item : itemList) {
                if(item instanceof TrajectoryItem){
                    TrajectoryItem trajectoryItem = (TrajectoryItem) item;
                    if(trajectoryItem.getPathRenderer() == this){
                        attachedPath = lastPathRenderer;
                        break;
                    }
                    lastPathRenderer = trajectoryItem.getPathRenderer();
                }
            }
            isAttachedPathEnd = true;
        } else if(selectionPointIndex == point2DList.size() - 1){
            boolean foundMyself = false;
            for (AbstractGuiItem item : itemList) {
                if(item instanceof TrajectoryItem){
                    TrajectoryItem trajectoryItem = (TrajectoryItem) item;
                    if(trajectoryItem.getPathRenderer() == this){
                        foundMyself = true;
                    } else if(foundMyself){
                        attachedPath = trajectoryItem.getPathRenderer();
                        break;
                    }
                }
            }
            isAttachedPathEnd = false;
        }

        if(attachedPath != null){
            MovablePointRenderer selectedPoint = pointRenderList.get(selectionPointIndex);
            Pose2d otherPathPose2d;
            if(isAttachedPathEnd){
                otherPathPose2d = attachedPath.point2DList.get(attachedPath.point2DList.size() - 1);
            } else {
                otherPathPose2d = attachedPath.point2DList.get(0);
            }

            if(!(Math.abs(selectedPoint.getPos2().sub((float) otherPathPose2d.getX(), (float) otherPathPose2d.getY()).len2())
                    < Math.pow((20 / config.getPointScaleFactor() * camera.zoom), 2))){
                attachedPath = null;
                System.out.println("Not close enough to other path");
            }
        }

        MovablePointRenderer point = pointRenderList.get(selectionPointIndex);
        point.update(camera, mousePos, lastMousePos);
    }

    public void updatePoint(OrthographicCamera camera, Vector3 mousePos, Vector3 lastMousePos) {
        if(selectionPointIndex != -1){
            MovablePointRenderer selectedPoint = pointRenderList.get(selectionPointIndex);
            selectedPoint.update(camera, mousePos, lastMousePos);
            if (rotationPoint != null){
                rotationPoint.update(camera, mousePos, lastMousePos);
            }

            if(attachedPath != null){
                if(isAttachedPathEnd){
                    attachedPath.point2DList.set(attachedPath.point2DList.size() - 1, point2DList.get(selectionPointIndex));
                    attachedPath.pointRenderList.get(attachedPath.pointRenderList.size() - 1).setPosition(selectedPoint.getPos2());
                } else {
                    attachedPath.point2DList.set(0, point2DList.get(selectionPointIndex));
                    attachedPath.pointRenderList.get(0).setPosition(selectedPoint.getPos2());
                }
                attachedPath.updatePath();
            }
        }
    }

    public void setRobotPathPreviewPoint(CloseTrajectoryPoint closePoint) {
        this.robotPreviewTime = closePoint.pointTime;
    }

    @Override
    public void onPointClick(@NotNull PointClickEvent event) {
        if (pointRenderList.contains(event.getPoint())) { //Should Only be called once per path
            if (event.isLeftClick()) {
                selectionPointIndex = pointRenderList.indexOf(event.getPoint());
                Rotation2d rotation = point2DList.get(selectionPointIndex).getRotation();

                float xPos = (float) (event.getPos().x + rotation.getCos() * 1);
                float yPos = (float) (event.getPos().y + rotation.getSin() * 1);
                rotationPoint = new MovablePointRenderer(xPos, yPos, Color.GREEN, POINT_SIZE, this);
            }
        }
    }


    @Override
    public void onPointMove(@NotNull PointMoveEvent event) {
        if (event.getPoint() == rotationPoint) {
            MovablePointRenderer referencePoint = pointRenderList.get(selectionPointIndex);
            Rotation2d rotation2d = new Rotation2d(Math.atan2(event.getNewPos().y - referencePoint.getY(), event.getNewPos().x - referencePoint.getX()));
            double posX = (float) (referencePoint.getX() + rotation2d.getCos());
            double posY = (float) (referencePoint.getY() + rotation2d.getSin());
            event.setPosition((float) posX, (float) posY);

            Pose2d pose2d = new Pose2d(point2DList.get(selectionPointIndex).getTranslation(), rotation2d);
            point2DList.set(selectionPointIndex, pose2d);

        } else {
            selectionPointIndex = pointRenderList.indexOf(event.getPoint());
            Rotation2d rotation = point2DList.get(selectionPointIndex).getRotation();

            float xPos = (float) (event.getNewPos().x + rotation.getCos() * 1);
            float yPos = (float) (event.getNewPos().y + rotation.getSin() * 1);
            rotationPoint = new MovablePointRenderer(xPos, yPos, Color.GREEN, POINT_SIZE, this);

            Pose2d pose2d = new Pose2d(new Translation2d(event.getNewPos().x, event.getNewPos().y), point2DList.get(selectionPointIndex).getRotation());
            point2DList.set(selectionPointIndex, pose2d);
        }
        updatePath();
    }

    public void updatePath() {
        updatePath(true);
    }

    public void updatePath(boolean updateListener) {
        //trajectory = TrajectoryGenerator.generateTrajectory(point2DList, TRAJECTORY_CONSTRAINTS);
        //System.out.println(trajectory.getTotalTimeSeconds());
        completableFutureTrajectory = CompletableFuture.supplyAsync(() -> {
            TrajectoryConfig trajectoryConfig = new TrajectoryConfig(maxVelocityMetersPerSecond,
                    maxAccelerationMetersPerSecondSq);
            for (TrajectoryConstraint trajectoryConstraint : trajectoryConstraints) {
                trajectoryConfig.addConstraint(trajectoryConstraint);
            }
            trajectoryConfig.setReversed(isReversed());
            trajectoryConfig.setStartVelocity(velocityStart);
            trajectoryConfig.setEndVelocity(velocityEnd);
            return trajectory = TrajectoryGenerator.generateTrajectory(point2DList, trajectoryConfig);
        }, executorService);


        if (updateListener && pathChangeListener != null) pathChangeListener.onPathChange();
    }

    @NotNull
    public Trajectory getNotNullTrajectory() {
        try {
            return completableFutureTrajectory.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null; //Should never happen
    }

    @Nullable
    public Trajectory getTrajectory() {
        return trajectory;
    }

    public @NotNull List<Pose2d> getPoint2DList() {
        return point2DList;
    }

    public void removeSelection() {
        selectionPointIndex = -1;
        rotationPoint = null;
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
        for (Pose2d pose2d : point2DList) {
            stringBuilder.append("[Translation: (").
                    append(pose2d.getX()).append(", ").
                    append(pose2d.getY()).append("), Rotation: ").
                    append(pose2d.getRotation().getDegrees())
                    .append("] ");
        }
        return "PathRenderer{" +
                "color=" + color +
                ", point2DList=" + stringBuilder +
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
     * @param origin Origin of the point (in pixels)
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
