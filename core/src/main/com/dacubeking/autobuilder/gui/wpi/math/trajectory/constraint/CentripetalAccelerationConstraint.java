// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.wpi.math.geometry.Pose2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A constraint on the maximum absolute centripetal acceleration allowed when traversing a trajectory. The centripetal
 * acceleration of a robot is defined as the velocity squared divided by the radius of curvature.
 *
 * <p>Effectively, limiting the maximum centripetal acceleration will cause the robot to slow down
 * around tight turns, making it easier to track trajectories with sharp turns.
 */
public class CentripetalAccelerationConstraint implements TrajectoryConstraint {
    @JsonProperty("maxCentripetalAccelerationMetersPerSecondSq") private final double m_maxCentripetalAccelerationMetersPerSecondSq;

    /**
     * Constructs a centripetal acceleration constraint.
     *
     * @param maxCentripetalAccelerationMetersPerSecondSq The max centripetal acceleration.
     */
    @JsonCreator
    public CentripetalAccelerationConstraint(
            @JsonProperty("maxCentripetalAccelerationMetersPerSecondSq") double maxCentripetalAccelerationMetersPerSecondSq) {
        m_maxCentripetalAccelerationMetersPerSecondSq = maxCentripetalAccelerationMetersPerSecondSq;
    }

    /**
     * Returns the max velocity given the current pose and curvature.
     *
     * @param poseMeters              The pose at the current point in the trajectory.
     * @param curvatureRadPerMeter    The curvature at the current point in the trajectory.
     * @param velocityMetersPerSecond The velocity at the current point in the trajectory before constraints are applied.
     * @return The absolute maximum velocity.
     */
    @Override
    public double getMaxVelocityMetersPerSecond(
            Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        // ac = v^2 / r
        // k (curvature) = 1 / r

        // therefore, ac = v^2 * k
        // ac / k = v^2
        // v = std::sqrt(ac / k)

        return Math.sqrt(
                m_maxCentripetalAccelerationMetersPerSecondSq / Math.abs(curvatureRadPerMeter));
    }

    /**
     * Returns the minimum and maximum allowable acceleration for the trajectory given pose, curvature, and speed.
     *
     * @param poseMeters              The pose at the current point in the trajectory.
     * @param curvatureRadPerMeter    The curvature at the current point in the trajectory.
     * @param velocityMetersPerSecond The speed at the current point in the trajectory.
     * @return The min and max acceleration bounds.
     */
    @Override
    public MinMax getMinMaxAccelerationMetersPerSecondSq(
            Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        // The acceleration of the robot has no impact on the centripetal acceleration
        // of the robot.
        return new MinMax();
    }

    public double getMaxCentripetalAccelerationMetersPerSecondSq() {
        return m_maxCentripetalAccelerationMetersPerSecondSq;
    }
}
