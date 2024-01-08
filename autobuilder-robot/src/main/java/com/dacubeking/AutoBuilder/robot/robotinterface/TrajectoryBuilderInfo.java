package com.dacubeking.AutoBuilder.robot.robotinterface;

import com.dacubeking.AutoBuilder.robot.annotations.AutoBuilderRobotSide;
import com.dacubeking.AutoBuilder.robot.command.AutoBuilderSwerveControllerCommand;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Stores the trajectory and the target pose supplier that should be used to follow the trajectory.
 * <p>
 * <b>Important Considerations:</b>
 * <ul>
 * <li>Your code needs to stop the robot from moving when this command finishes/is canceled. Setting the default
 * command for your drivetrain to use your controller inputs w/ brake mode enabled should achieve this.</li>
 * <li>Consider using {@link AutoBuilderSwerveControllerCommand} instead of {@link SwerveControllerCommand}</li>
 * </ul>
 *
 * @param trajectory             The trajectory to follow
 * @param desiredHeadingSupplier A supplier that returns the target pose for the trajectory follower. If you're using a
 *                               non-holonomic drivetrain, you can ignore this.
 */
@AutoBuilderRobotSide
public record TrajectoryBuilderInfo(
        @NotNull Trajectory trajectory,
        @NotNull Supplier<Rotation2d> desiredHeadingSupplier
) {}
