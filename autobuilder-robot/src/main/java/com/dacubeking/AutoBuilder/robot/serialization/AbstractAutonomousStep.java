package com.dacubeking.AutoBuilder.robot.serialization;

import com.dacubeking.AutoBuilder.robot.annotations.AutoBuilderRobotSide;
import com.dacubeking.AutoBuilder.robot.serialization.command.CommandExecutionFailedException;
import com.dacubeking.AutoBuilder.robot.serialization.command.SendableScript;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A class that represents an autonomous step.
 *
 * @see TrajectoryAutonomousStep
 * @see ScriptAutonomousStep
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @Type(value = TrajectoryAutonomousStep.class, name = "trajectory"),
        @Type(value = ScriptAutonomousStep.class, name = "script"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Internal
public abstract class AbstractAutonomousStep {

    private final boolean closed;

    @JsonCreator
    protected AbstractAutonomousStep(@JsonProperty(required = true, value = "closed") boolean closed) {
        this.closed = closed;
    }

    /**
     * Execute this autonomous step.
     *
     * @param scriptsToExecuteByTime    A mutable arraylist representing scripts to run while driving the autonomous path.
     * @param scriptsToExecuteByPercent A mutable arraylist representing scripts to run while driving the autonomous path.
     * @return true if the autonomous step is finished, false otherwise
     * @throws CommandExecutionFailedException Thrown if a script fails to execute.
     * @throws ExecutionException              Thrown if something goes wrong running a command on the main thread.
     */
    @AutoBuilderRobotSide
    public abstract boolean execute(List<SendableScript> scriptsToExecuteByTime,
                                    List<SendableScript> scriptsToExecuteByPercent) throws CommandExecutionFailedException,
            ExecutionException;

    /**
     * End this autonomous step.
     */
    @AutoBuilderRobotSide
    public abstract void end();

    /**
     * Initialize this autonomous step.
     */
    @AutoBuilderRobotSide
    public abstract void initialize();


    @JsonProperty("closed")
    public boolean isClosed() {
        return closed;
    }
}