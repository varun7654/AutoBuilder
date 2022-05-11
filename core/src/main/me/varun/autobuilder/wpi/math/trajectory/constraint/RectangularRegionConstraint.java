// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package me.varun.autobuilder.wpi.math.trajectory.constraint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.geometry.Translation2d;

/** Enforces a particular constraint only within a rectangular region. */
public class RectangularRegionConstraint implements TrajectoryConstraint {
  @JsonProperty("bottomLeftPoint") private final Translation2d m_bottomLeftPoint;
  @JsonProperty("topRightPoint") private final Translation2d m_topRightPoint;
  @JsonProperty("constraint") private final TrajectoryConstraint m_constraint;

  /**
   * Constructs a new RectangularRegionConstraint.
   *
   * @param bottomLeftPoint The bottom left point of the rectangular region in which to enforce the
   *     constraint.
   * @param topRightPoint The top right point of the rectangular region in which to enforce the
   *     constraint.
   * @param constraint The constraint to enforce when the robot is within the region.
   */
  @JsonCreator
  public RectangularRegionConstraint(
         @JsonProperty("bottomLeftPoint")Translation2d bottomLeftPoint,
         @JsonProperty("topRightPoint") Translation2d topRightPoint,
         @JsonProperty("constraint") TrajectoryConstraint constraint) {
    m_bottomLeftPoint = bottomLeftPoint;
    m_topRightPoint = topRightPoint;
    m_constraint = constraint;
  }

  @Override
  public double getMaxVelocityMetersPerSecond(
      Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
    if (isPoseInRegion(poseMeters)) {
      return m_constraint.getMaxVelocityMetersPerSecond(
          poseMeters, curvatureRadPerMeter, velocityMetersPerSecond);
    } else {
      return Double.POSITIVE_INFINITY;
    }
  }

  @Override
  public MinMax getMinMaxAccelerationMetersPerSecondSq(
      Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
    if (isPoseInRegion(poseMeters)) {
      return m_constraint.getMinMaxAccelerationMetersPerSecondSq(
          poseMeters, curvatureRadPerMeter, velocityMetersPerSecond);
    } else {
      return new MinMax();
    }
  }

  /**
   * Returns whether the specified robot pose is within the region that the constraint is enforced
   * in.
   *
   * @param robotPose The robot pose.
   * @return Whether the robot pose is within the constraint region.
   */
  public boolean isPoseInRegion(Pose2d robotPose) {
    return robotPose.getX() >= m_bottomLeftPoint.getX()
        && robotPose.getX() <= m_topRightPoint.getX()
        && robotPose.getY() >= m_bottomLeftPoint.getY()
        && robotPose.getY() <= m_topRightPoint.getY();
  }
}
