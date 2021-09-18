package me.varun.autobuilder.pathgenerator;

import com.badlogic.gdx.graphics.Color;
import me.varun.autobuilder.rendering.PathRenderer;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import me.varun.autobuilder.wpi.math.geometry.Translation2d;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryConfig;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static me.varun.autobuilder.AutoBuilder.TRAJECTORY_CONSTRAINTS;

public class PathGenerator{
    public static PathRenderer genPaths(ExecutorService executorService){
        ArrayList<Pose2d> points = new ArrayList<>();
        points.add(new Pose2d(new Translation2d(0,0), new Rotation2d(0)));
        points.add(new Pose2d(new Translation2d(5,10), new Rotation2d(45)));
        points.add(new Pose2d(new Translation2d(10,10), new Rotation2d(45)));
        points.add(new Pose2d(new Translation2d(25,15), new Rotation2d(45)));
        points.add(new Pose2d(new Translation2d(30,15), new Rotation2d(20)));
        points.add(new Pose2d(new Translation2d(30,10), new Rotation2d(90)));
        points.add(new Pose2d(new Translation2d(35,15), new Rotation2d(90)));
        Trajectory trajectory = TrajectoryGenerator.generateTrajectory(points, TRAJECTORY_CONSTRAINTS);
        return new PathRenderer(Color.CYAN, trajectory, points, executorService);
    }

}
