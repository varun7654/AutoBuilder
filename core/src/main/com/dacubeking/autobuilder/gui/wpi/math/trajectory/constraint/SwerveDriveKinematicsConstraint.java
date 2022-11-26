// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.Constraint;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Pose2d;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.ChassisSpeeds;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.SwerveDriveKinematics;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class that enforces constraints on the swerve drive kinematics. This can be used to ensure that the trajectory is constructed
 * so that the commanded velocities for all 4 wheels of the drivetrain stay below a certain limit.
 */
@Constraint(name = "Swerve Drive Kinematics", description = "A class that enforces constraints on the swerve drive kinematics. " +
        "This can be used to ensure that the trajectory is constructed so that the commanded velocities for all 4 wheels of the" +
        " drivetrain stay below a certain limit.")
public class SwerveDriveKinematicsConstraint implements TrajectoryConstraint {

    @ConstraintField(name = "Max Speed", description = "The max speed that a module should be commanded to move at (m/s)")
    @JsonProperty("maxSpeedMetersPerSecond")
    private final double m_maxSpeedMetersPerSecond;
    @ConstraintField(name = "Kinematics", description = "Swerve drive kinematics")
    @JsonProperty("kinematics")
    private final SwerveDriveKinematics m_kinematics;

    /**
     * Constructs a swerve drive kinematics constraint.
     *
     * @param kinematics              Swerve drive kinematics.
     * @param maxSpeedMetersPerSecond The max speed that a side of the robot can travel at.
     */
    @JsonCreator
    public SwerveDriveKinematicsConstraint(@JsonProperty("kinematics") final SwerveDriveKinematics kinematics,
                                           @JsonProperty("maxSpeedMetersPerSecond") double maxSpeedMetersPerSecond) {
        m_maxSpeedMetersPerSecond = maxSpeedMetersPerSecond;
        m_kinematics = kinematics;
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
        // Represents the velocity of the chassis in the x direction
        var xdVelocity = velocityMetersPerSecond * poseMeters.getRotation().getCos();

        // Represents the velocity of the chassis in the y direction
        var ydVelocity = velocityMetersPerSecond * poseMeters.getRotation().getSin();

        // Create an object to represent the current chassis speeds.
        var chassisSpeeds =
                new ChassisSpeeds(xdVelocity, ydVelocity, velocityMetersPerSecond * curvatureRadPerMeter);

        // Get the wheel speeds and normalize them to within the max velocity.
        var wheelSpeeds = m_kinematics.toSwerveModuleStates(chassisSpeeds);
        SwerveDriveKinematics.normalizeWheelSpeeds(wheelSpeeds, m_maxSpeedMetersPerSecond);

        // Convert normalized wheel speeds back to chassis speeds
        var normSpeeds = m_kinematics.toChassisSpeeds(wheelSpeeds);

        // Return the new linear chassis speed.
        return Math.hypot(normSpeeds.vxMetersPerSecond, normSpeeds.vyMetersPerSecond);
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
        return new MinMax();
    }

    @Override
    public TrajectoryConstraint copy() {
        return new SwerveDriveKinematicsConstraint(m_kinematics.copy(), m_maxSpeedMetersPerSecond);
    }

    @Override
    public void update() {
        m_kinematics.update();
    }
}
