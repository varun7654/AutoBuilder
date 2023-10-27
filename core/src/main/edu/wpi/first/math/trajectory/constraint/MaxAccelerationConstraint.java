package edu.wpi.first.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.Constraint;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import edu.wpi.first.math.geometry.Pose2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Constraint(name = "Max Acceleration", description = "Enforces a max acceleration")
public class MaxAccelerationConstraint implements TrajectoryConstraint {

    @ConstraintField(name = "Max Acceleration", description = "The max acceleration")
    @JsonProperty("maxAcceleration")
    private double maxAcceleration;

    @JsonCreator
    public MaxAccelerationConstraint(@JsonProperty("maxAcceleration") double maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
    }

    @Override
    public double getMaxVelocityMetersPerSecond(Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        return Double.MAX_VALUE;
    }

    @Override
    public MinMax getMinMaxAccelerationMetersPerSecondSq(Pose2d poseMeters, double curvatureRadPerMeter,
                                                         double velocityMetersPerSecond) {
        return new MinMax(-maxAcceleration, maxAcceleration);
    }

    @Override
    public TrajectoryConstraint copy() {
        return new MaxAccelerationConstraint(maxAcceleration);
    }
}
