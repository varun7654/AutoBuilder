// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.wpi.math.controller.SimpleMotorFeedforward;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Pose2d;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.ChassisSpeeds;
import com.dacubeking.autobuilder.gui.wpi.math.kinematics.DifferentialDriveKinematics;
import com.dacubeking.autobuilder.gui.wpi.util.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class that enforces constraints on differential drive voltage expenditure based on the motor
 * dynamics and the drive kinematics. Ensures that the acceleration of any wheel of the robot while
 * following the trajectory is never higher than what can be achieved with the given maximum
 * voltage.
 */
public class DifferentialDriveVoltageConstraint implements TrajectoryConstraint {
  @JsonProperty("feedforward") private final SimpleMotorFeedforward m_feedforward;
  @JsonProperty("kinematics") private final DifferentialDriveKinematics m_kinematics;
  @JsonProperty("maxVoltage") private final double m_maxVoltage;

  /**
   * Creates a new DifferentialDriveVoltageConstraint.
   *
   * @param feedforward A feedforward component describing the behavior of the drive.
   * @param kinematics A kinematics component describing the drive geometry.
   * @param maxVoltage The maximum voltage available to the motors while following the path. Should
   *     be somewhat less than the nominal battery voltage (12V) to account for "voltage sag" due to
   *     current draw.
   */
  @JsonCreator
  public DifferentialDriveVoltageConstraint(
      @JsonProperty("feedforward") SimpleMotorFeedforward feedforward,
      @JsonProperty("kinematics") DifferentialDriveKinematics kinematics,
      @JsonProperty("maxVoltage") double maxVoltage) {
    m_feedforward =
        ErrorMessages.requireNonNullParam(feedforward, "feedforward", "DifferentialDriveVoltageConstraint");
    m_kinematics =
        ErrorMessages.requireNonNullParam(kinematics, "kinematics", "DifferentialDriveVoltageConstraint");
    m_maxVoltage = maxVoltage;
  }

  @Override
  public double getMaxVelocityMetersPerSecond(
          Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public MinMax getMinMaxAccelerationMetersPerSecondSq(
      Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
    var wheelSpeeds =
        m_kinematics.toWheelSpeeds(
            new ChassisSpeeds(
                velocityMetersPerSecond, 0, velocityMetersPerSecond * curvatureRadPerMeter));

    double maxWheelSpeed =
        Math.max(wheelSpeeds.leftMetersPerSecond, wheelSpeeds.rightMetersPerSecond);
    double minWheelSpeed =
        Math.min(wheelSpeeds.leftMetersPerSecond, wheelSpeeds.rightMetersPerSecond);

    // Calculate maximum/minimum possible accelerations from motor dynamics
    // and max/min wheel speeds
    double maxWheelAcceleration =
        m_feedforward.maxAchievableAcceleration(m_maxVoltage, maxWheelSpeed);
    double minWheelAcceleration =
        m_feedforward.minAchievableAcceleration(m_maxVoltage, minWheelSpeed);

    // Robot chassis turning on radius = 1/|curvature|.  Outer wheel has radius
    // increased by half of the trackwidth T.  Inner wheel has radius decreased
    // by half of the trackwidth.  Achassis / radius = Aouter / (radius + T/2), so
    // Achassis = Aouter * radius / (radius + T/2) = Aouter / (1 + |curvature|T/2).
    // Inner wheel is similar.

    // sgn(speed) term added to correctly account for which wheel is on
    // outside of turn:
    // If moving forward, max acceleration constraint corresponds to wheel on outside of turn
    // If moving backward, max acceleration constraint corresponds to wheel on inside of turn

    // When velocity is zero, then wheel velocities are uniformly zero (robot cannot be
    // turning on its center) - we have to treat this as a special case, as it breaks
    // the signum function.  Both max and min acceleration are *reduced in magnitude*
    // in this case.

    double maxChassisAcceleration;
    double minChassisAcceleration;

    if (velocityMetersPerSecond == 0) {
      maxChassisAcceleration =
          maxWheelAcceleration
              / (1 + m_kinematics.trackWidthMeters * Math.abs(curvatureRadPerMeter) / 2);
      minChassisAcceleration =
          minWheelAcceleration
              / (1 + m_kinematics.trackWidthMeters * Math.abs(curvatureRadPerMeter) / 2);
    } else {
      maxChassisAcceleration =
          maxWheelAcceleration
              / (1
                  + m_kinematics.trackWidthMeters
                      * Math.abs(curvatureRadPerMeter)
                      * Math.signum(velocityMetersPerSecond)
                      / 2);
      minChassisAcceleration =
          minWheelAcceleration
              / (1
                  - m_kinematics.trackWidthMeters
                      * Math.abs(curvatureRadPerMeter)
                      * Math.signum(velocityMetersPerSecond)
                      / 2);
    }

    // When turning about a point inside of the wheelbase (i.e. radius less than half
    // the trackwidth), the inner wheel's direction changes, but the magnitude remains
    // the same.  The formula above changes sign for the inner wheel when this happens.
    // We can accurately account for this by simply negating the inner wheel.

    if ((m_kinematics.trackWidthMeters / 2) > (1 / Math.abs(curvatureRadPerMeter))) {
      if (velocityMetersPerSecond > 0) {
        minChassisAcceleration = -minChassisAcceleration;
      } else if (velocityMetersPerSecond < 0) {
        maxChassisAcceleration = -maxChassisAcceleration;
      }
    }

    return new MinMax(minChassisAcceleration, maxChassisAcceleration);
  }
}
