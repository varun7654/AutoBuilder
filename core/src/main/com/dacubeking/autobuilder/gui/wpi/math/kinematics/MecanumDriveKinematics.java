// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.dacubeking.autobuilder.gui.wpi.math.kinematics;

import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import com.dacubeking.autobuilder.gui.wpi.math.MathSharedStore;
import com.dacubeking.autobuilder.gui.wpi.math.MathUsageId;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Translation2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ejml.simple.SimpleMatrix;

/**
 * Helper class that converts a chassis velocity (dx, dy, and dtheta components) into individual wheel speeds.
 *
 * <p>The inverse kinematics (converting from a desired chassis velocity to individual wheel speeds)
 * uses the relative locations of the wheels with respect to the center of rotation. The center of rotation for inverse kinematics
 * is also variable. This means that you can set your set your center of rotation in a corner of the robot to perform special
 * evasion maneuvers.
 *
 * <p>Forward kinematics (converting an array of wheel speeds into the overall chassis motion) is
 * performs the exact opposite of what inverse kinematics does. Since this is an overdetermined system (more equations than
 * variables), we use a least-squares approximation.
 *
 * <p>The inverse kinematics: [wheelSpeeds] = [wheelLocations] * [chassisSpeeds] We take the
 * Moore-Penrose pseudoinverse of [wheelLocations] and then multiply by [wheelSpeeds] to get our chassis speeds.
 *
 * <p>Forward kinematics is also used for odometry -- determining the position of the robot on the
 * field using encoders and a gyro.
 */
public class MecanumDriveKinematics {
    private final SimpleMatrix m_inverseKinematics;
    private final SimpleMatrix m_forwardKinematics;

    @ConstraintField(name = "Front Left Location", description = "The location of the front left wheel.")
    @JsonProperty("frontLeftWheelMeters")
    private Translation2d m_frontLeftWheelMeters;
    @ConstraintField(name = "Front Right Location", description = "The location of the front right wheel.")
    @JsonProperty("frontRightWheelMeters")
    private Translation2d m_frontRightWheelMeters;
    @ConstraintField(name = "Rear Left Location", description = "The location of the rear left wheel.")
    @JsonProperty("rearLeftWheelMeters")
    private Translation2d m_rearLeftWheelMeters;
    @ConstraintField(name = "Rear Right Location", description = "The location of the rear right wheel.")
    @JsonProperty("rearRightWheelMeters")
    private Translation2d m_rearRightWheelMeters;

    private Translation2d m_prevCoR = new Translation2d();

    /**
     * Constructs a mecanum drive kinematics object.
     *
     * @param frontLeftWheelMeters  The location of the front-left wheel relative to the physical center of the robot.
     * @param frontRightWheelMeters The location of the front-right wheel relative to the physical center of the robot.
     * @param rearLeftWheelMeters   The location of the rear-left wheel relative to the physical center of the robot.
     * @param rearRightWheelMeters  The location of the rear-right wheel relative to the physical center of the robot.
     */
    @JsonCreator
    public MecanumDriveKinematics(@JsonProperty("frontLeftWheelMeters") Translation2d frontLeftWheelMeters,
                                  @JsonProperty("frontRightWheelMeters") Translation2d frontRightWheelMeters,
                                  @JsonProperty("rearLeftWheelMeters") Translation2d rearLeftWheelMeters,
                                  @JsonProperty("rearRightWheelMeters") Translation2d rearRightWheelMeters) {
        m_frontLeftWheelMeters = frontLeftWheelMeters;
        m_frontRightWheelMeters = frontRightWheelMeters;
        m_rearLeftWheelMeters = rearLeftWheelMeters;
        m_rearRightWheelMeters = rearRightWheelMeters;

        m_inverseKinematics = new SimpleMatrix(4, 3);

        setInverseKinematics(
                frontLeftWheelMeters, frontRightWheelMeters, rearLeftWheelMeters, rearRightWheelMeters);
        m_forwardKinematics = m_inverseKinematics.pseudoInverse();

        MathSharedStore.reportUsage(MathUsageId.kKinematics_MecanumDrive, 1);
    }

    /**
     * Performs inverse kinematics to return the wheel speeds from a desired chassis velocity. This method is often used to
     * convert joystick values into wheel speeds.
     *
     * <p>This function also supports variable centers of rotation. During normal operations, the
     * center of rotation is usually the same as the physical center of the robot; therefore, the argument is defaulted to that
     * use case. However, if you wish to change the center of rotation for evasive maneuvers, vision alignment, or for any other
     * use case, you can do so.
     *
     * @param chassisSpeeds          The desired chassis speed.
     * @param centerOfRotationMeters The center of rotation. For example, if you set the center of rotation at one corner of the
     *                               robot and provide a chassis speed that only has a dtheta component, the robot will rotate
     *                               around that corner.
     * @return The wheel speeds. Use caution because they are not normalized. Sometimes, a user input may cause one of the wheel
     * speeds to go above the attainable max velocity. Use the {@link MecanumDriveWheelSpeeds#normalize(double)} function to
     * rectify this issue.
     */
    public MecanumDriveWheelSpeeds toWheelSpeeds(
            ChassisSpeeds chassisSpeeds, Translation2d centerOfRotationMeters) {
        // We have a new center of rotation. We need to compute the matrix again.
        if (!centerOfRotationMeters.equals(m_prevCoR)) {
            var fl = m_frontLeftWheelMeters.minus(centerOfRotationMeters);
            var fr = m_frontRightWheelMeters.minus(centerOfRotationMeters);
            var rl = m_rearLeftWheelMeters.minus(centerOfRotationMeters);
            var rr = m_rearRightWheelMeters.minus(centerOfRotationMeters);

            setInverseKinematics(fl, fr, rl, rr);
            m_prevCoR = centerOfRotationMeters;
        }

        var chassisSpeedsVector = new SimpleMatrix(3, 1);
        chassisSpeedsVector.setColumn(
                0,
                0,
                chassisSpeeds.vxMetersPerSecond,
                chassisSpeeds.vyMetersPerSecond,
                chassisSpeeds.omegaRadiansPerSecond);

        var wheelsMatrix = m_inverseKinematics.mult(chassisSpeedsVector);
        return new MecanumDriveWheelSpeeds(
                wheelsMatrix.get(0, 0),
                wheelsMatrix.get(1, 0),
                wheelsMatrix.get(2, 0),
                wheelsMatrix.get(3, 0));
    }

    /**
     * Performs inverse kinematics. See {@link #toWheelSpeeds(ChassisSpeeds, Translation2d)} for more information.
     *
     * @param chassisSpeeds The desired chassis speed.
     * @return The wheel speeds.
     */
    public MecanumDriveWheelSpeeds toWheelSpeeds(ChassisSpeeds chassisSpeeds) {
        return toWheelSpeeds(chassisSpeeds, new Translation2d());
    }

    /**
     * Performs forward kinematics to return the resulting chassis state from the given wheel speeds. This method is often used
     * for odometry -- determining the robot's position on the field using data from the real-world speed of each wheel on the
     * robot.
     *
     * @param wheelSpeeds The current mecanum drive wheel speeds.
     * @return The resulting chassis speed.
     */
    public ChassisSpeeds toChassisSpeeds(MecanumDriveWheelSpeeds wheelSpeeds) {
        var wheelSpeedsMatrix = new SimpleMatrix(4, 1);
        wheelSpeedsMatrix.setColumn(
                0,
                0,
                wheelSpeeds.frontLeftMetersPerSecond,
                wheelSpeeds.frontRightMetersPerSecond,
                wheelSpeeds.rearLeftMetersPerSecond,
                wheelSpeeds.rearRightMetersPerSecond);
        var chassisSpeedsVector = m_forwardKinematics.mult(wheelSpeedsMatrix);

        return new ChassisSpeeds(
                chassisSpeedsVector.get(0, 0),
                chassisSpeedsVector.get(1, 0),
                chassisSpeedsVector.get(2, 0));
    }

    /**
     * Construct inverse kinematics matrix from wheel locations.
     *
     * @param fl The location of the front-left wheel relative to the physical center of the robot.
     * @param fr The location of the front-right wheel relative to the physical center of the robot.
     * @param rl The location of the rear-left wheel relative to the physical center of the robot.
     * @param rr The location of the rear-right wheel relative to the physical center of the robot.
     */
    private void setInverseKinematics(
            Translation2d fl, Translation2d fr, Translation2d rl, Translation2d rr) {
        m_inverseKinematics.setRow(0, 0, 1, -1, -(fl.getX() + fl.getY()));
        m_inverseKinematics.setRow(1, 0, 1, 1, fr.getX() - fr.getY());
        m_inverseKinematics.setRow(2, 0, 1, 1, rl.getX() - rl.getY());
        m_inverseKinematics.setRow(3, 0, 1, -1, -(rr.getX() + rr.getY()));
    }

    public MecanumDriveKinematics copy() {
        return new MecanumDriveKinematics(
                m_frontLeftWheelMeters,
                m_frontRightWheelMeters,
                m_rearLeftWheelMeters,
                m_rearRightWheelMeters);
    }
}
