package me.varun.autobuilder.serialization;

import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;

import java.io.Serializable;
import java.util.List;

public class SerializableTrajectoryState extends Trajectory.State implements Serializable {

    double timeSeconds;
    double velocityMetersPerSecond;
    double accelerationMetersPerSecondSq;
    SerializablePose2d poseMeters;
    double curvatureRadPerMete;

    public SerializableTrajectoryState(Trajectory.State state) {
        timeSeconds = state.timeSeconds;
        velocityMetersPerSecond = state.velocityMetersPerSecond;
        accelerationMetersPerSecondSq = state.accelerationMetersPerSecondSq;
        poseMeters = new SerializablePose2d(state.poseMeters);
        curvatureRadPerMete = state.curvatureRadPerMeter;
    }

    public Trajectory.State getState(){
        return new Trajectory.State(timeSeconds, velocityMetersPerSecond, accelerationMetersPerSecondSq, poseMeters.getPose2d(), curvatureRadPerMeter);
    }
}
