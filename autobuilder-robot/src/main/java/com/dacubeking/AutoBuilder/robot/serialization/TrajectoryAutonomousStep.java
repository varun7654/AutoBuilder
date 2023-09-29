package com.dacubeking.AutoBuilder.robot.serialization;

import com.dacubeking.AutoBuilder.robot.serialization.command.CommandExecutionFailedException;
import com.dacubeking.AutoBuilder.robot.serialization.command.SendableScript;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.Trajectory.State;
import edu.wpi.first.wpilibj.Timer;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.dacubeking.AutoBuilder.robot.robotinterface.AutonomousContainer.getCommandTranslator;

@JsonIgnoreProperties(ignoreUnknown = true)
@Internal
public class TrajectoryAutonomousStep extends AbstractAutonomousStep {
    public static final double PERIOD_TIME_S = 0.02;
    private final @NotNull Trajectory trajectory;
    private final @NotNull List<TimedRotation> rotations;

    @JsonCreator
    private TrajectoryAutonomousStep(@JsonProperty(required = true, value = "states") @NotNull List<State> states,
                                     @JsonProperty(required = true, value = "rotations") @NotNull List<TimedRotation> rotations) {
        this.trajectory = new Trajectory(states);
        this.rotations = rotations;
    }

    @NotNull
    public Trajectory getTrajectory() {
        return trajectory;
    }

    public @NotNull List<TimedRotation> getRotations() {
        return rotations;
    }

    /**
     * Executes the trajectory
     *
     * @param scriptsToExecuteByTime    An arraylist of the scripts to run during this path.
     * @param scriptsToExecuteByPercent An arraylist of the scripts to run during this path.
     * @throws InterruptedException            If the thread is interrupted (ex: the auto is killed).
     * @throws CommandExecutionFailedException If a script fails to execute.
     * @throws ExecutionException              Something goes wrong running a command on the main thread.
     */
    @Override
    public void execute(@NotNull List<SendableScript> scriptsToExecuteByTime,
                        @NotNull List<SendableScript> scriptsToExecuteByPercent)
            throws InterruptedException, CommandExecutionFailedException, ExecutionException {
        //Sort the lists to make sure they are sorted by time
        Collections.sort(scriptsToExecuteByTime);
        Collections.sort(scriptsToExecuteByPercent);

        if (rotations.size() > 0) {
            //Scripts for non-holonomic won't have any rotations (since the rotation is based on the driven path)
            getCommandTranslator().setAutonomousRotation(rotations.get(0).rotation);
        }
        getCommandTranslator().setNewTrajectory(trajectory); //Send the auto to our drive class to be executed

        int rotationIndex = 1; // Start at the second rotation (the first is the starting rotation)
        while (!getCommandTranslator().isTrajectoryDone()) { // Wait till the auto is done
            double startTime = Timer.getFPGATimestamp();
            final double elapsedTime = getCommandTranslator().getTrajectoryElapsedTime();

            if (rotationIndex < rotations.size() && elapsedTime > rotations.get(rotationIndex).time) {
                // We've passed the time for the next rotation
                getCommandTranslator().setAutonomousRotation(rotations.get(rotationIndex).rotation); //Set the rotation
                rotationIndex++; // Increment the rotation index
            }

            if (!scriptsToExecuteByTime.isEmpty()
                    && scriptsToExecuteByTime.get(0).getDelay() <= elapsedTime) {
                // We have a script to execute, and it is time to execute it
                scriptsToExecuteByTime.get(0).execute();
                scriptsToExecuteByTime.remove(0);
            }

            if (!scriptsToExecuteByPercent.isEmpty()
                    && scriptsToExecuteByPercent.get(0).getDelay() <= elapsedTime / trajectory.getTotalTimeSeconds()) {
                // We have a script to execute, and it is time to execute it
                scriptsToExecuteByPercent.get(0).execute();
                scriptsToExecuteByPercent.remove(0);
            }
            //noinspection BusyWait
            Thread.sleep((long) (1000 * Math.max(startTime + PERIOD_TIME_S - Timer.getFPGATimestamp(), 0)));
        }
        getCommandTranslator().stopRobot();

        //Execute any remaining scripts
        for (SendableScript sendableScript : scriptsToExecuteByTime) {
            sendableScript.execute();
        }
        for (SendableScript sendableScript : scriptsToExecuteByPercent) {
            sendableScript.execute();
        }

        scriptsToExecuteByTime.clear();
        scriptsToExecuteByPercent.clear();
    }

    @Override
    public String toString() {
        return "TrajectoryAutonomousStep{" +
                "trajectory=" + trajectory +
                ", rotations=" + rotations +
                '}';
    }
}