// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.Constraint;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import edu.wpi.first.math.geometry.Pose2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a constraint that enforces a max velocity. This can be composed with the {@link EllipticalRegionConstraint} or
 * {@link RectangularRegionConstraint} to enforce a max velocity in a region.
 */
@Constraint(name = "Max Velocity", description = "Enforces a max velocity")
public class MaxVelocityConstraint implements TrajectoryConstraint {
    @ConstraintField(name = "Max Velocity", description = "The max velocity")
    @JsonProperty("maxVelocity")
    private double m_maxVelocity;

    /**
     * Constructs a new MaxVelocityConstraint.
     *
     * @param maxVelocityMetersPerSecond The max velocity.
     */
    @JsonCreator
    public MaxVelocityConstraint(@JsonProperty("maxVelocity") double maxVelocityMetersPerSecond) {
        m_maxVelocity = maxVelocityMetersPerSecond;
    }

    @Override
    public double getMaxVelocityMetersPerSecond(
            Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        return m_maxVelocity;
    }

    @Override
    public TrajectoryConstraint.MinMax getMinMaxAccelerationMetersPerSecondSq(
            Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        return new MinMax();
    }

    @Override
    public TrajectoryConstraint copy() {
        return new MaxVelocityConstraint(m_maxVelocity);
    }
}
