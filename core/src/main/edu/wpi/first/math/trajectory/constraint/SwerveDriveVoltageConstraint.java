package edu.wpi.first.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.Constraint;
import com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations.ConstraintField;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.util.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Constraint(name = "Swerve Drive Voltage", description = "Enforces a voltage limit on a swerve drive. This does not account" +
        " for the voltage required to turn the robot and doesn't account for each of the wheels individually.")
public class SwerveDriveVoltageConstraint implements TrajectoryConstraint {

    @ConstraintField(name = "Motor Feedforward", description = "The feedforward to use for the drivetrain.")
    @JsonProperty("feedforward")
    private final SimpleMotorFeedforward feedforward;

    @ConstraintField(name = "Max Voltage", description = "The maximum voltage available to the drivetrain.")
    @JsonProperty("maxVoltage")
    private final double maxVoltage;

    @JsonCreator
    public SwerveDriveVoltageConstraint(
            @JsonProperty("feedforward") SimpleMotorFeedforward feedforward,
            @JsonProperty("maxVoltage") double maxVoltage) {
        this.feedforward =
                ErrorMessages.requireNonNullParam(feedforward, "feedforward", "DifferentialDriveVoltageConstraint");
        this.maxVoltage = maxVoltage;
    }

    @Override
    public double getMaxVelocityMetersPerSecond(Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
        return Double.MAX_VALUE;
    }

    @Override
    public MinMax getMinMaxAccelerationMetersPerSecondSq(Pose2d poseMeters, double curvatureRadPerMeter,
                                                         double velocityMetersPerSecond) {
        var maxAccel = feedforward.maxAchievableAcceleration(maxVoltage, velocityMetersPerSecond);
        var minAccel = feedforward.minAchievableAcceleration(maxVoltage, velocityMetersPerSecond);
        return new MinMax(minAccel, maxAccel);
    }

    @Override
    public TrajectoryConstraint copy() {
        return new SwerveDriveVoltageConstraint(new SimpleMotorFeedforward(feedforward.ks, feedforward.kv, feedforward.ka),
                maxVoltage);
    }

    @Override
    public void update() {
        TrajectoryConstraint.super.update();
    }
}
