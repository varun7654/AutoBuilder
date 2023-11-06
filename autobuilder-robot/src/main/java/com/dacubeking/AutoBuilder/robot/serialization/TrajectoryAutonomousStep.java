package com.dacubeking.AutoBuilder.robot.serialization;

import com.dacubeking.AutoBuilder.robot.annotations.AutoBuilderRobotSide;
import com.dacubeking.AutoBuilder.robot.robotinterface.AutonomousContainer;
import com.dacubeking.AutoBuilder.robot.robotinterface.TrajectoryBuilderInfo;
import com.dacubeking.AutoBuilder.robot.serialization.command.CommandExecutionFailedException;
import com.dacubeking.AutoBuilder.robot.serialization.command.SendableScript;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.spline.Spline.ControlVector;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.constraint.TrajectoryConstraint;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;


@JsonIgnoreProperties(ignoreUnknown = true)
@Internal
public class TrajectoryAutonomousStep extends AbstractAutonomousStep {
    public static final double PERIOD_TIME_S = 0.02;
    private final List<Trajectory.State> states;
    private final List<ControlVector> controlVectors;
    private final boolean reversed;
    private final float color;
    private final float velocityStart;
    private final float velocityEnd;
    private final List<TimedRotation> rotations;
    @NotNull private final List<TrajectoryConstraint> constraints;

    @JsonIgnore
    private final Trajectory trajectory;

    @JsonCreator
    public TrajectoryAutonomousStep(@JsonProperty(required = true, value = "states") @Nullable List<Trajectory.State> m_states,
                                    @JsonProperty(required = true, value = "pointList") @Nullable List<ControlVector> controlVectors,
                                    @JsonProperty(required = true, value = "rotations") List<TimedRotation> rotations,
                                    @JsonProperty(required = true, value = "reversed") boolean reversed,
                                    @JsonProperty(required = true, value = "color") float color,
                                    @JsonProperty(required = true, value = "closed") boolean closed,
                                    @JsonProperty(defaultValue = "0", value = "velocityStart") float velocityStart,
                                    @JsonProperty(defaultValue = "0", value = "velocityEnd") float velocityEnd,
                                    @JsonProperty(defaultValue = "null", value = "constraints") List<TrajectoryConstraint> constraints) {
        super(closed);
        this.reversed = reversed;
        this.color = color;
        this.controlVectors = controlVectors;
        this.rotations = rotations;

        this.states = m_states;
        this.velocityStart = velocityStart;
        this.velocityEnd = velocityEnd;
        this.constraints = constraints == null ? List.of() : constraints;


        Trajectory trajectory;
        if (states == null) {
            trajectory = new Trajectory();
        } else {
            trajectory = new Trajectory(states);
        }
        this.trajectory = trajectory;
    }

    @JsonProperty("pointList")
    public List<ControlVector> getControlVectors() {
        return controlVectors;
    }

    @NotNull
    public Trajectory getTrajectory() {
        return trajectory;
    }

    @Override
    @AutoBuilderRobotSide
    public void end() {
        pathDriveCommand.cancel();
        pathDriveCommand = null;
    }

    @Override
    @AutoBuilderRobotSide
    public void initialize() {
        timer = new Timer();
        rotationIndex = 1; // Start at the second rotation (the first is the starting rotation)
        isFirstRun = true;
    }

    private int rotationIndex;
    private boolean isFirstRun;

    private Timer timer;
    private Command pathDriveCommand = null;

    /**
     * Executes the trajectory
     *
     * @param scriptsToExecuteByTime    An arraylist of the scripts to run during this path.
     * @param scriptsToExecuteByPercent An arraylist of the scripts to run during this path.
     * @throws CommandExecutionFailedException If a script fails to execute.
     * @throws ExecutionException              Something goes wrong running a command on the main thread.
     */
    @Override
    @AutoBuilderRobotSide
    public boolean execute(@NotNull List<SendableScript> scriptsToExecuteByTime,
                           @NotNull List<SendableScript> scriptsToExecuteByPercent) throws CommandExecutionFailedException,
            ExecutionException {

        if (isFirstRun) {
            if (!rotations.isEmpty()) {
                //Scripts for non-holonomic won't have any rotations (since the rotation is based on the driven path)
                targetRotation = rotations.get(0).rotation;
            }
            this.pathDriveCommand = AutonomousContainer.getInstance().getTrajectoryFollowerSupplier()
                    .apply(new TrajectoryBuilderInfo(trajectory, this::getTargetRotation));
            pathDriveCommand.schedule();
            timer.restart();
        }

        final double elapsedTime = timer.get();

        assert pathDriveCommand != null;
        if (pathDriveCommand.isFinished()) {
            //Execute any remaining scripts
            while (!scriptsToExecuteByTime.isEmpty()) {
                // We have a script to execute, and it is time to execute it
                if (scriptsToExecuteByTime.get(0).execute()) {
                    scriptsToExecuteByTime.remove(0);
                } else {
                    break; // We've been told something hasn't finished executing. Continue & see if it's completed next time
                }
            }

            while (!scriptsToExecuteByPercent.isEmpty()) {
                // We have a script to execute, and it is time to execute it
                if (scriptsToExecuteByPercent.get(0).execute()) {
                    scriptsToExecuteByPercent.remove(0);
                } else {
                    break; // We've been told something hasn't finished executing. Continue & see if it's completed next time
                }
            }
            return scriptsToExecuteByPercent.isEmpty() && scriptsToExecuteByTime.isEmpty(); //If some commands are still
            // executing we need to wait at this step until they're done.
        } else { // Wait till the auto is done
            if (rotationIndex < rotations.size() && elapsedTime > rotations.get(rotationIndex).time) {
                // We've passed the time for the next rotation
                targetRotation = rotations.get(rotationIndex).rotation; //Set the rotation
                rotationIndex++; // Increment the rotation index
            }

            while (!scriptsToExecuteByTime.isEmpty()
                    && scriptsToExecuteByTime.get(0).getDelay() <= elapsedTime) {
                // We have a script to execute, and it is time to execute it
                if (scriptsToExecuteByTime.get(0).execute()) {
                    scriptsToExecuteByTime.remove(0);
                } else {
                    break; // We've been told something hasn't finished executing. Continue & see if it's completed next time
                }
            }

            while (!scriptsToExecuteByPercent.isEmpty()
                    && scriptsToExecuteByPercent.get(0).getDelay() <= elapsedTime / trajectory.getTotalTimeSeconds()) {
                // We have a script to execute, and it is time to execute it
                if (scriptsToExecuteByPercent.get(0).execute()) {
                    scriptsToExecuteByPercent.remove(0);
                } else {
                    break; // We've been told something hasn't finished executing. Continue & see if it's completed next time
                }
            }
            return false;
        }
    }


    private Rotation2d targetRotation = new Rotation2d();

    @AutoBuilderRobotSide
    public Rotation2d getTargetRotation() {
        return targetRotation;
    }

    @Override
    public String toString() {
        return "TrajectoryAutonomousStep{" +
                "trajectory=" + trajectory +
                ", rotations=" + rotations +
                '}';
    }

    @JsonProperty
    public float getColor() {
        return color;
    }

    @JsonProperty("states")
    public List<Trajectory.State> getStates() {
        return states;
    }

    @JsonProperty
    public float getVelocityStart() {
        return velocityStart;
    }

    @JsonProperty
    public float getVelocityEnd() {
        return velocityEnd;
    }

    @JsonProperty
    public List<TimedRotation> getRotations() {
        return rotations;
    }

    @JsonProperty
    public @NotNull List<TrajectoryConstraint> getConstraints() {
        return constraints;
    }

    @JsonProperty
    public boolean isReversed() {
        return reversed;
    }
}