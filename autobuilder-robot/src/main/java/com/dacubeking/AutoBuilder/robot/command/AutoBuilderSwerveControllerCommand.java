package com.dacubeking.AutoBuilder.robot.command;

import com.dacubeking.AutoBuilder.robot.robotinterface.TrajectoryBuilderInfo;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static edu.wpi.first.util.ErrorMessages.requireNonNullParam;

/**
 * {@link SwerveControllerCommand}, but adds a check for position error before finishing a path, and removes constructors the
 * imply the heading from the trajectory. (as the {@link TrajectoryBuilderInfo#desiredHeadingSupplier()} should be passed to the
 * desiredRotation
 */
public class AutoBuilderSwerveControllerCommand extends SwerveControllerCommand {
    /**
     * Constructs a new SwerveControllerCommand that when executed will follow the provided trajectory. This command will not
     * return output voltages but rather raw module states from the position controllers which need to be put into a velocity
     * PID.
     *
     * <p>Note: The controllers will *not* set the outputVolts to zero upon completion of the path.
     * This is left to the user to do since it is not appropriate for paths with nonstationary endstates.
     *
     * @param trajectory         The trajectory to follow.
     * @param pose               A function that supplies the robot pose - use one of the odometry classes to provide this.
     * @param kinematics         The kinematics for the robot drivetrain.
     * @param xController        The Trajectory Tracker PID controller for the robot's x position.
     * @param yController        The Trajectory Tracker PID controller for the robot's y position.
     * @param thetaController    The Trajectory Tracker PID controller for angle for the robot.
     * @param tolerance          tolerance for the HolonomicDriveController. Will prevent the path from completing if the robot is
     *                           not within the defined tolerance. see: {@link HolonomicDriveController#setTolerance(Pose2d)}
     * @param desiredRotation    The angle that the drivetrain should be facing. This is sampled at each time step.
     * @param outputModuleStates The raw output module states from the position controllers.
     * @param requirements       The subsystems to require.
     */
    public AutoBuilderSwerveControllerCommand(Trajectory trajectory,
                                              Supplier<Pose2d> pose,
                                              SwerveDriveKinematics kinematics,
                                              PIDController xController,
                                              PIDController yController,
                                              ProfiledPIDController thetaController,
                                              Pose2d tolerance,
                                              Supplier<Rotation2d> desiredRotation,
                                              Consumer<SwerveModuleState[]> outputModuleStates,
                                              Subsystem... requirements) {

        this(
                trajectory,
                pose,
                kinematics,
                new HolonomicDriveController(
                        requireNonNullParam(xController, "xController", "SwerveControllerCommand"),
                        requireNonNullParam(yController, "yController", "SwerveControllerCommand"),
                        requireNonNullParam(thetaController, "thetaController", "SwerveControllerCommand")),
                tolerance,
                desiredRotation,
                outputModuleStates,
                requirements);
    }

    /**
     * Constructs a new SwerveControllerCommand that when executed will follow the provided trajectory. This command will not
     * return output voltages but rather raw module states from the position controllers which need to be put into a velocity
     * PID.
     *
     * <p>Note: The controllers will *not* set the outputVolts to zero upon completion of the path-
     * this is left to the user, since it is not appropriate for paths with nonstationary endstates.
     *
     * @param trajectory         The trajectory to follow.
     * @param pose               A function that supplies the robot pose - use one of the odometry classes to provide this.
     * @param kinematics         The kinematics for the robot drivetrain.
     * @param controller         The HolonomicDriveController for the drivetrain.
     * @param tolerance          tolerance for the HolonomicDriveController. Will prevent the path from completing if the robot is
     *                           not within the defined tolerance. see: {@link HolonomicDriveController#setTolerance(Pose2d)}
     * @param desiredRotation    The angle that the drivetrain should be facing. This is sampled at each time step.
     * @param outputModuleStates The raw output module states from the position controllers.
     * @param requirements       The subsystems to require.
     */
    public AutoBuilderSwerveControllerCommand(Trajectory trajectory, Supplier<Pose2d> pose, SwerveDriveKinematics kinematics,
                                              HolonomicDriveController controller,
                                              Pose2d tolerance, Supplier<Rotation2d> desiredRotation,
                                              Consumer<SwerveModuleState[]> outputModuleStates, Subsystem... requirements) {
        super(trajectory, pose, kinematics, controller, desiredRotation, outputModuleStates, requirements);
        this.controller = controller;
        controller.setTolerance(tolerance);
    }

    private final HolonomicDriveController controller;

    @Override
    public boolean isFinished() {
        assert controller != null;
        return controller.atReference();
    }
}
