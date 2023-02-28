// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.Constraint;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Pose2d;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Translation2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Enforces a particular constraint only within a rectangular region.
 */
@Constraint(name = "Rectangular Region", description = "Enforces a particular constraint only within a rectangular region.")
public class RectangularRegionConstraint implements TrajectoryConstraint, PositionedConstraint {

    @ConstraintField(name = "Bottom Left", description = "The bottom left corner of the region.")
    @JsonProperty("bottomLeftPoint")
    private Translation2d m_bottomLeftPoint;

    @ConstraintField(name = "Top Right", description = "The top right corner of the region.")
    @JsonProperty("topRightPoint")
    private Translation2d m_topRightPoint;

    @ConstraintField(name = "Constraint", description = "The constraint to enforce.")
    @JsonProperty("constraint")
    private @Nullable TrajectoryConstraint m_constraint;

    /**
     * Constructs a new RectangularRegionConstraint.
     *
     * @param bottomLeftPoint The bottom left point of the rectangular region in which to enforce the constraint.
     * @param topRightPoint   The top right point of the rectangular region in which to enforce the constraint.
     * @param constraint      The constraint to enforce when the robot is within the region.
     */
    @JsonCreator
    public RectangularRegionConstraint(
            @JsonProperty("bottomLeftPoint") Translation2d bottomLeftPoint,
            @JsonProperty("topRightPoint") Translation2d topRightPoint,
            @Nullable @JsonProperty("constraint") TrajectoryConstraint constraint) {
        m_bottomLeftPoint = bottomLeftPoint;
        m_topRightPoint = topRightPoint;
        m_constraint = constraint;
    }

    @Override
    public double getMaxVelocityMetersPerSecond(
            Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        if (m_constraint == null) {
            return Double.POSITIVE_INFINITY;
        }

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

        if (m_constraint == null) {
            return new MinMax();
        }

        if (isPoseInRegion(poseMeters)) {
            return m_constraint.getMinMaxAccelerationMetersPerSecondSq(
                    poseMeters, curvatureRadPerMeter, velocityMetersPerSecond);
        } else {
            return new MinMax();
        }
    }

    @Override
    public TrajectoryConstraint copy() {
        return new RectangularRegionConstraint(m_bottomLeftPoint.copy(), m_topRightPoint.copy(),
                m_constraint != null ? m_constraint.copy() : null);
    }

    /**
     * Returns whether the specified robot pose is within the region that the constraint is enforced in.
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

    @Override
    public void reflectX(double x) {
        // Switch the x values of the bottom left and top right points.
        m_bottomLeftPoint = new Translation2d(-(m_topRightPoint.getX() - x) + x, m_bottomLeftPoint.getY());
        m_topRightPoint = new Translation2d(-(m_bottomLeftPoint.getX() - x) + x, m_topRightPoint.getY());

        if (m_constraint instanceof PositionedConstraint) {
            ((PositionedConstraint) m_constraint).reflectX(x);
        }
    }

    @Override
    public void reflectY(double y) {
        // Switch the y values of the bottom left and top right points.
        m_bottomLeftPoint = new Translation2d(m_bottomLeftPoint.getX(), -(m_topRightPoint.getY() - y) + y);
        m_topRightPoint = new Translation2d(m_topRightPoint.getX(), -(m_bottomLeftPoint.getY() - y) + y);

        if (m_constraint instanceof PositionedConstraint) {
            ((PositionedConstraint) m_constraint).reflectY(y);
        }
    }
}
