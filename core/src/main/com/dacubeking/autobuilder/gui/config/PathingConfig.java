package com.dacubeking.autobuilder.gui.config;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.MecanumDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.trajectory.constraint.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PathingConfig {
    public double maxVelocityMetersPerSecond;
    public double maxAccelerationMetersPerSecondSq;
    public ArrayList<TrajectoryConstraint> trajectoryConstraints;

    @JsonCreator
    public PathingConfig(@JsonProperty(value = "maxVelocityMetersPerSecond") Double maxVelocityMetersPerSecond,
                         @JsonProperty(value = "maxAccelerationMetersPerSecondSq") Double maxAccelerationMetersPerSecondSq,
                         @JsonProperty(value = "trajectoryConstraints") ArrayList<TrajectoryConstraint> trajectoryConstraints) {
        this.maxVelocityMetersPerSecond = maxVelocityMetersPerSecond == null ? 80 * .0254 : maxVelocityMetersPerSecond;
        this.maxAccelerationMetersPerSecondSq = maxAccelerationMetersPerSecondSq == null ? 140 * 0.0254 :
                maxAccelerationMetersPerSecondSq;
        if (trajectoryConstraints == null) {
            this.trajectoryConstraints = new ArrayList<>();
            this.trajectoryConstraints.add(new CentripetalAccelerationConstraint(80 * 0.0254));
            this.trajectoryConstraints.add(new DifferentialDriveVoltageConstraint(new SimpleMotorFeedforward(5, 1),
                    new DifferentialDriveKinematics(0.93), 10));
            this.trajectoryConstraints.add(new DifferentialDriveKinematicsConstraint(new DifferentialDriveKinematics(0.93), 2));
            this.trajectoryConstraints.add(
                    new EllipticalRegionConstraint(new Translation2d(20, 25), 0.5, 0.5, Rotation2d.fromDegrees(45),
                            new MaxVelocityConstraint(0.5)));
            this.trajectoryConstraints.add(new MaxVelocityConstraint(2));
            this.trajectoryConstraints.add(new MecanumDriveKinematicsConstraint(
                    new MecanumDriveKinematics(new Translation2d(-0.5, 0.5), new Translation2d(0.5, 0.5),
                            new Translation2d(-0.5, 0.5), new Translation2d(-0.5, -0.5)), 2));
            this.trajectoryConstraints.add(new RectangularRegionConstraint(new Translation2d(0, 0), new Translation2d(20, 20),
                    new MaxVelocityConstraint(2)));
            this.trajectoryConstraints.add(new SwerveDriveKinematicsConstraint(
                    new SwerveDriveKinematics(new Translation2d(-0.5, 0.5), new Translation2d(0.5, 0.5),
                            new Translation2d(-0.5, 0.5), new Translation2d(-0.5, -0.5)), 2));
        } else {
            this.trajectoryConstraints = trajectoryConstraints;
        }
    }

    @JsonCreator
    public PathingConfig() {
        this(null, null, null);
    }

    public PathingConfig(@NotNull PathingConfig pathingConfig) {
        this(pathingConfig.maxVelocityMetersPerSecond, pathingConfig.maxAccelerationMetersPerSecondSq,
                // Copy all the trajectories to ensure mutable state doesn't change unexpectedly
                new ArrayList<>() {{
                    for (TrajectoryConstraint trajectoryConstraint : pathingConfig.trajectoryConstraints) {
                        add(trajectoryConstraint.copy());
                    }
                }});
    }
}
