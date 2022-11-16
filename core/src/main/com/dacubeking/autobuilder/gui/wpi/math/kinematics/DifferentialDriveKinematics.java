// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.dacubeking.autobuilder.gui.wpi.math.kinematics;

import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import com.dacubeking.autobuilder.gui.wpi.math.MathSharedStore;
import com.dacubeking.autobuilder.gui.wpi.math.MathUsageId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Helper class that converts a chassis velocity (dx and dtheta components) to left and right wheel velocities for a differential
 * drive.
 *
 * <p>Inverse kinematics converts a desired chassis speed into left and right velocity components
 * whereas forward kinematics converts left and right component velocities into a linear and angular chassis speed.
 */
@SuppressWarnings("MemberName")
public class DifferentialDriveKinematics {
    @ConstraintField(name = "Track Width", description = "The distance between the left and right wheels on the robot. (m)")
    @JsonProperty("trackWidthMeters")
    public double trackWidthMeters;

    /**
     * Constructs a differential drive kinematics object.
     *
     * @param trackWidthMeters The track width of the drivetrain. Theoretically, this is the distance between the left wheels and
     *                         right wheels. However, the empirical value may be larger than the physical measured value due to
     *                         scrubbing effects.
     */
    @JsonCreator
    public DifferentialDriveKinematics(@JsonProperty("trackWidthMeters") double trackWidthMeters) {
        this.trackWidthMeters = trackWidthMeters;
        MathSharedStore.reportUsage(MathUsageId.kKinematics_DifferentialDrive, 1);
    }

    /**
     * Returns a chassis speed from left and right component velocities using forward kinematics.
     *
     * @param wheelSpeeds The left and right velocities.
     * @return The chassis speed.
     */
    public ChassisSpeeds toChassisSpeeds(DifferentialDriveWheelSpeeds wheelSpeeds) {
        return new ChassisSpeeds(
                (wheelSpeeds.leftMetersPerSecond + wheelSpeeds.rightMetersPerSecond) / 2,
                0,
                (wheelSpeeds.rightMetersPerSecond - wheelSpeeds.leftMetersPerSecond) / trackWidthMeters);
    }

    /**
     * Returns left and right component velocities from a chassis speed using inverse kinematics.
     *
     * @param chassisSpeeds The linear and angular (dx and dtheta) components that represent the chassis' speed.
     * @return The left and right velocities.
     */
    public DifferentialDriveWheelSpeeds toWheelSpeeds(ChassisSpeeds chassisSpeeds) {
        return new DifferentialDriveWheelSpeeds(
                chassisSpeeds.vxMetersPerSecond
                        - trackWidthMeters / 2 * chassisSpeeds.omegaRadiansPerSecond,
                chassisSpeeds.vxMetersPerSecond
                        + trackWidthMeters / 2 * chassisSpeeds.omegaRadiansPerSecond);
    }

    public DifferentialDriveKinematics copy() {
        return new DifferentialDriveKinematics(trackWidthMeters);
    }
}
