// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.Constraint;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Enforces a particular constraint only within an elliptical region.
 */
@Constraint(name = "Elliptical Region", description = "Enforces a particular constraint only within an elliptical region.")
public class EllipticalRegionConstraint implements TrajectoryConstraint, PositionedConstraint {

    @ConstraintField(name = "Center", description = "The center of the ellipse.")
    @JsonProperty("center")
    private Translation2d m_center;

    @ConstraintField(name = "radii", description = """
            The radii of the ellipse.\s

            The x value is the radius in the x direction, and the y value is the radius in the y direction.""")
    @JsonProperty("radii")
    private Translation2d m_radii;

    @ConstraintField(name = "Constraint", description = "The constraint to enforce.")
    @JsonProperty("constraint")
    @Nullable
    private TrajectoryConstraint m_constraint;

    /**
     * Constructs a new EllipticalRegionConstraint.
     *
     * @param center     The center of the ellipse in which to enforce the constraint.
     * @param xWidth     The width of the ellipse in which to enforce the constraint.
     * @param yWidth     The height of the ellipse in which to enforce the constraint.
     * @param rotation   The rotation to apply to all radii around the origin.
     * @param constraint The constraint to enforce when the robot is within the region.
     */
    @SuppressWarnings("ParameterName")
    public EllipticalRegionConstraint(
            Translation2d center,
            double xWidth,
            double yWidth,
            Rotation2d rotation,
            @Nullable TrajectoryConstraint constraint) {
        m_center = center;
        m_radii = new Translation2d(xWidth / 2.0, yWidth / 2.0).rotateBy(rotation);
        m_constraint = constraint;
    }

    @JsonCreator
    public EllipticalRegionConstraint(@JsonProperty("center") Translation2d center,
                                      @JsonProperty("radii") Translation2d radii,
                                      @Nullable @JsonProperty("constraint") TrajectoryConstraint constraint) {
        m_center = center;
        m_radii = radii;
        m_constraint = constraint;
    }

    @Override
    public double getMaxVelocityMetersPerSecond(Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        if (isPoseInRegion(poseMeters) && m_constraint != null) {
            return m_constraint.getMaxVelocityMetersPerSecond(
                    poseMeters, curvatureRadPerMeter, velocityMetersPerSecond);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }


    @Override
    public MinMax getMinMaxAccelerationMetersPerSecondSq(
            Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        if (isPoseInRegion(poseMeters) && m_constraint != null) {
            return m_constraint.getMinMaxAccelerationMetersPerSecondSq(
                    poseMeters, curvatureRadPerMeter, velocityMetersPerSecond);
        } else {
            return new MinMax();
        }
    }

    @Override
    public TrajectoryConstraint copy() {
        return new EllipticalRegionConstraint(m_center.copy(), m_radii.copy(), m_constraint != null ? m_constraint.copy() : null);
    }

    /**
     * Returns whether the specified robot pose is within the region that the constraint is enforced in.
     *
     * @param robotPose The robot pose.
     * @return Whether the robot pose is within the constraint region.
     */
    public boolean isPoseInRegion(Pose2d robotPose) {
        // The region (disk) bounded by the ellipse is given by the equation:
        // ((x-h)^2)/Rx^2) + ((y-k)^2)/Ry^2) <= 1
        // If the inequality is satisfied, then it is inside the ellipse; otherwise
        // it is outside the ellipse.
        // Both sides have been multiplied by Rx^2 * Ry^2 for efficiency reasons.
        return Math.pow(robotPose.getX() - m_center.getX(), 2) * Math.pow(m_radii.getY(), 2)
                + Math.pow(robotPose.getY() - m_center.getY(), 2) * Math.pow(m_radii.getX(), 2)
                <= Math.pow(m_radii.getX(), 2) * Math.pow(m_radii.getY(), 2);
    }

    @Override
    public void reflectX(double x) {
        m_center = new Translation2d(-(m_center.getX() - x) + x, m_center.getY());
        m_radii = new Translation2d(m_radii.getX(), -m_radii.getY());
    }

    @Override
    public void reflectY(double y) {
        m_center = new Translation2d(m_center.getX(), -(m_center.getY() - y) + y);
        m_radii = new Translation2d(-m_radii.getX(), m_radii.getY());
    }
}
