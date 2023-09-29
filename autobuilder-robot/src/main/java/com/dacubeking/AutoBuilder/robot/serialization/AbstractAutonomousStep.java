package com.dacubeking.AutoBuilder.robot.serialization;

import com.dacubeking.AutoBuilder.robot.serialization.command.CommandExecutionFailedException;
import com.dacubeking.AutoBuilder.robot.serialization.command.SendableScript;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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

    @JsonCreator
    protected AbstractAutonomousStep() {
    }

    /**
     * Execute this autonomous step.
     *
     * @param scriptsToExecuteByTime    A mutable arraylist representing scripts to run while driving the autonomous path.
     * @param scriptsToExecuteByPercent A mutable arraylist representing scripts to run while driving the autonomous path.
     * @throws InterruptedException            Thrown if the thread is interrupted (ex: auto is killed).
     * @throws CommandExecutionFailedException Thrown if a script fails to execute.
     * @throws ExecutionException              Thrown if something goes wrong running a command on the main thread.
     */
    public abstract void execute(List<SendableScript> scriptsToExecuteByTime,
                                 List<SendableScript> scriptsToExecuteByPercent) throws InterruptedException,
            CommandExecutionFailedException, ExecutionException;
}