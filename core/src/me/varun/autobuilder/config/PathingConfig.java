package me.varun.autobuilder.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.varun.autobuilder.wpi.math.trajectory.constraint.CentripetalAccelerationConstraint;
import me.varun.autobuilder.wpi.math.trajectory.constraint.TrajectoryConstraint;

import java.util.ArrayList;

public class PathingConfig {
    public double maxVelocityMetersPerSecond;
    public double maxAccelerationMetersPerSecondSq;
    public ArrayList<TrajectoryConstraint> trajectoryConstraints;

    @JsonCreator
    public PathingConfig(@JsonProperty(value = "maxVelocityMetersPerSecond") Double maxVelocityMetersPerSecond,
                         @JsonProperty(value = "maxAccelerationMetersPerSecondSq") Double maxAccelerationMetersPerSecondSq,
                         @JsonProperty(value = "trajectoryConstraints") ArrayList<TrajectoryConstraint> trajectoryConstraints) {
        this.maxVelocityMetersPerSecond = maxVelocityMetersPerSecond == null ? 80 * .0254 : maxVelocityMetersPerSecond;
        this.maxAccelerationMetersPerSecondSq = maxAccelerationMetersPerSecondSq == null ? 140 * 0.0254 : maxAccelerationMetersPerSecondSq;
        if(trajectoryConstraints == null){
            this.trajectoryConstraints = new ArrayList<>();
            this.trajectoryConstraints.add(new CentripetalAccelerationConstraint(80 * 0.0254));
        } else {
            this.trajectoryConstraints = trajectoryConstraints;
        }
    }
    @JsonCreator
    public PathingConfig() {
        this(null, null, null);
    }
}
