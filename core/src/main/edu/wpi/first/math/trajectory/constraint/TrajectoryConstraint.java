// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.math.trajectory.constraint;

import edu.wpi.first.math.geometry.Pose2d;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An interface for defining user-defined velocity and acceleration constraints while generating trajectories.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(CentripetalAccelerationConstraint.class),
        @JsonSubTypes.Type(DifferentialDriveKinematicsConstraint.class),
        @JsonSubTypes.Type(DifferentialDriveVoltageConstraint.class),
        @JsonSubTypes.Type(EllipticalRegionConstraint.class),
        @JsonSubTypes.Type(MaxVelocityConstraint.class),
        @JsonSubTypes.Type(MecanumDriveKinematicsConstraint.class),
        @JsonSubTypes.Type(RectangularRegionConstraint.class),
        @JsonSubTypes.Type(SwerveDriveKinematicsConstraint.class),
        @JsonSubTypes.Type(SwerveDriveVoltageConstraint.class),
        @JsonSubTypes.Type(MaxAccelerationConstraint.class)
})
public interface TrajectoryConstraint {
    /**
     * Returns the max velocity given the current pose and curvature.
     *
     * @param poseMeters              The pose at the current point in the trajectory.
     * @param curvatureRadPerMeter    The curvature at the current point in the trajectory.
     * @param velocityMetersPerSecond The velocity at the current point in the trajectory before constraints are applied.
     * @return The absolute maximum velocity.
     */
    double getMaxVelocityMetersPerSecond(
            Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond);

    /**
     * Returns the minimum and maximum allowable acceleration for the trajectory given pose, curvature, and speed.
     *
     * @param poseMeters              The pose at the current point in the trajectory.
     * @param curvatureRadPerMeter    The curvature at the current point in the trajectory.
     * @param velocityMetersPerSecond The speed at the current point in the trajectory.
     * @return The min and max acceleration bounds.
     */
    MinMax getMinMaxAccelerationMetersPerSecondSq(
            Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond);

    TrajectoryConstraint copy();

    default void update() {}

    /**
     * Represents a minimum and maximum acceleration.
     */
    @SuppressWarnings("MemberName")
    class MinMax {
        public double minAccelerationMetersPerSecondSq = -Double.MAX_VALUE;
        public double maxAccelerationMetersPerSecondSq = +Double.MAX_VALUE;

        /**
         * Constructs a MinMax.
         *
         * @param minAccelerationMetersPerSecondSq The minimum acceleration.
         * @param maxAccelerationMetersPerSecondSq The maximum acceleration.
         */
        public MinMax(
                double minAccelerationMetersPerSecondSq, double maxAccelerationMetersPerSecondSq) {
            this.minAccelerationMetersPerSecondSq = minAccelerationMetersPerSecondSq;
            this.maxAccelerationMetersPerSecondSq = maxAccelerationMetersPerSecondSq;
        }

        /**
         * Constructs a MinMax with default values.
         */
        public MinMax() {}
    }
}
