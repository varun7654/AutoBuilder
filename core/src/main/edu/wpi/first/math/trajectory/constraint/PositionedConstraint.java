package edu.wpi.first.math.trajectory.constraint;

public interface PositionedConstraint {

    /**
     * @param x The line perpendicular to the x-axis to reflect the constraint about.
     */
    void reflectX(double x);

    /**
     * @param y The line perpendicular to the y-axis to reflect the constraint about.
     */
    void reflectY(double y);
}
