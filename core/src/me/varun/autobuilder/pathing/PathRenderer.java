package me.varun.autobuilder.pathing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import me.varun.autobuilder.callback.movablepoint.MovablePointEventHandler;
import me.varun.autobuilder.callback.movablepoint.PointClickEvent;
import me.varun.autobuilder.callback.movablepoint.PointMoveEvent;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import me.varun.autobuilder.wpi.math.geometry.Translation2d;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static me.varun.autobuilder.AutoBuilder.POINT_SCALE_FACTOR;
import static me.varun.autobuilder.AutoBuilder.TRAJECTORY_CONSTRAINTS;

public class PathRenderer extends MovablePointEventHandler {
    private final Color color;
    private Trajectory trajectory;
    private final List<Pose2d> point2DList;
    private final List<MovablePointRenderer> pointRenderList;

    private MovablePointRenderer rotationPoint;
    private PointRenderer highlightPoint;
    private int selectionPointIndex = -1;

    private final ExecutorService executorService;

    public PathRenderer(Color color, Trajectory trajectory, List<Pose2d> pointList, ExecutorService executorService){
        this.color = color;
        this.trajectory = trajectory;
        this.point2DList = pointList;

        pointRenderList = new ArrayList<>();

        for (Pose2d pose2d : point2DList) {
            pointRenderList.add(new MovablePointRenderer((float) pose2d.getX(), (float) pose2d.getY(), Color.ORANGE, 5, this));
        }

        this.executorService = executorService;
    }

    public void render(ShapeRenderer renderer, OrthographicCamera cam){
        for( double i = 0.1; i<trajectory.getTotalTimeSeconds(); i += 0.05){
            Pose2d prev = trajectory.sample(i-0.05).poseMeters;
            Pose2d cur = trajectory.sample(i).poseMeters;
            double speed = trajectory.sample(i).velocityMetersPerSecond;
            float[] color = new float[3];
            this.color.toHsv(color);
            color[0] = (float) (speed*255/TRAJECTORY_CONSTRAINTS.getMaxVelocity());
            Color speedColor = new Color().fromHsv(color);
            renderer.setColor(speedColor);
            renderer.line((float) prev.getX()*50,(float) prev.getY()*50, (float) cur.getX()*50, (float) cur.getY()*50);
        }

        if(rotationPoint != null) {
            rotationPoint.draw(renderer, cam);
            renderer.line(pointRenderList.get(selectionPointIndex).getRenderPos3(), rotationPoint.getRenderPos3());
        }

        for (int i = 0; i< pointRenderList.size(); i++) {
            PointRenderer pointRenderer = pointRenderList.get(i);

            if(i == selectionPointIndex){
                if(highlightPoint == null) highlightPoint = new PointRenderer(pointRenderer.getPos2(), Color.WHITE, 7);
                else highlightPoint.setPosition(pointRenderer.getPos2());

                highlightPoint.draw(renderer, cam);
            }
            pointRenderer.draw(renderer, cam);
        }
    }

    boolean pointDeleted;

    public boolean update(OrthographicCamera cam, Vector3 mousePos, Vector3 lastMousePos){
        boolean moving = false;
        pointDeleted = false;

        if(rotationPoint != null) {
            moving = rotationPoint.update(cam,new Vector3(mousePos), new Vector3(lastMousePos));
        }

        if(!moving){
            for (MovablePointRenderer pointRenderer : new ArrayList<>(pointRenderList)) {
                if(pointRenderer.update(cam, new Vector3(mousePos), new Vector3(lastMousePos))){
                    moving = true;
                    break;
                }
            }
        }



        int currentIndexPos = 0;
        if(!moving && !pointDeleted){
            if(Gdx.app.getInput().isButtonJustPressed(Input.Buttons.RIGHT)){
                for( double i = 0; i<trajectory.getTotalTimeSeconds(); i += 0.01){
                    Pose2d cur = trajectory.sample(i).poseMeters;
                    double diffX = cur.getX() - mousePos.x / POINT_SCALE_FACTOR;
                    double diffY = cur.getY() - mousePos.y / POINT_SCALE_FACTOR;

                    if(currentIndexPos + 1 < point2DList.size() && point2DList.get(currentIndexPos + 1).getTranslation().getDistance(cur.getTranslation()) < 0.1){
                        currentIndexPos++;
                    }

                    if(Math.abs(diffX) < .1 && Math.abs(diffY) < .1) {
                        System.out.println(currentIndexPos);
                        point2DList.add(currentIndexPos+1, cur);
                        pointRenderList.add(currentIndexPos+1, new MovablePointRenderer((float) cur.getX(), (float) cur.getY(), Color.BLUE, 5, this));
                        if(selectionPointIndex > currentIndexPos ) selectionPointIndex++;
                        updatePath();
                        break;
                    }


                }
            }
        }

        return moving;
    }

    @Override
    public void onPointClick(PointClickEvent event) {
        if(pointRenderList.contains(event.getPoint())){
            if(event.isLeftClick()){
                selectionPointIndex = pointRenderList.indexOf(event.getPoint());
                Rotation2d rotation = point2DList.get(selectionPointIndex).getRotation();

                float xPos = (float) (event.getPos().x + rotation.getCos()*1);
                float yPos = (float) (event.getPos().y + rotation.getSin()*1);
                rotationPoint = new MovablePointRenderer(xPos, yPos, Color.GREEN, 5,this);
            }
            if(event.isRightClick()){
                System.out.println("here");
                int removeIndex = pointRenderList.indexOf(event.getPoint());
                if (selectionPointIndex > removeIndex){
                    selectionPointIndex--;
                } else if(selectionPointIndex == removeIndex){
                    selectionPointIndex = 0;
                    rotationPoint = null;
                    highlightPoint = null;
                }
                pointRenderList.remove(removeIndex);
                point2DList.remove(removeIndex);
                pointDeleted = true;
            }
        }

        updatePath();
    }


    @Override
    public void onPointMove(PointMoveEvent event) {
        if(event.getPoint() == rotationPoint){
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

            float xPos = (float) (event.getNewPos().x + rotation.getCos()*1);
            float yPos = (float) (event.getNewPos().y + rotation.getSin()*1);
            rotationPoint = new MovablePointRenderer(xPos, yPos, Color.GREEN, 5, this);

            Pose2d pose2d = new Pose2d(new Translation2d(event.getNewPos().x, event.getNewPos().y), point2DList.get(selectionPointIndex).getRotation());
            point2DList.set(selectionPointIndex, pose2d);
        }
        updatePath();
    }

    private void updatePath(){
        //trajectory = TrajectoryGenerator.generateTrajectory(point2DList, TRAJECTORY_CONSTRAINTS);
        //System.out.println(trajectory.getTotalTimeSeconds());
        executorService.submit(() -> trajectory = TrajectoryGenerator.generateTrajectory(point2DList, TRAJECTORY_CONSTRAINTS));
    }
}
