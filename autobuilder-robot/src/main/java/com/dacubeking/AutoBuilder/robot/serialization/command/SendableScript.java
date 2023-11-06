package com.dacubeking.AutoBuilder.robot.serialization.command;

import com.dacubeking.AutoBuilder.robot.annotations.AutoBuilderRobotSide;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class SendableScript implements Comparable<SendableScript> {


    @JsonIgnore
    @AutoBuilderRobotSide
    public LinkedList<SendableCommand> executionQueue;

    /**
     * @return true if all the commands have finished executing, false if some commands still need to be executed.
     */
    @AutoBuilderRobotSide
    public boolean execute() throws CommandExecutionFailedException, ExecutionException {
        if (executionQueue.isEmpty()) {
            return true;
        }

        SendableCommand command = executionQueue.peek();


        // Keep executing commands until we reach one we need to wait at.
        while (command.execute()) {
            executionQueue.remove();
        }

        return executionQueue.isEmpty();
    }

    public void initialize() {
        commands.forEach(SendableCommand::setFirstRun);
        executionQueue = new LinkedList<>(commands);
    }

    @Override
    public String toString() {
        return "SendableScript{" +
                "delayType=" + delayType +
                ", delay=" + delay +
                ", deployable=" + deployable +
                ", commands=" + commands +
                '}';
    }

    public enum DelayType {
        NONE,
        TIME,
        PERCENT
    }

    private DelayType delayType;
    private double delay;
    private boolean deployable;

    private final ArrayList<SendableCommand> commands;


    @JsonCreator
    public SendableScript(@JsonProperty("delayType") DelayType delayType,
                          @JsonProperty("delay") double delay,
                          @JsonProperty("commands") ArrayList<SendableCommand> commands) {
        this.delayType = delayType;
        this.delay = delay;
        this.commands = commands;
    }

    public SendableScript() {
        this(DelayType.NONE, 0, new ArrayList<>());
    }

    @JsonProperty("delayType")
    public DelayType getDelayType() {
        return delayType;
    }

    @JsonProperty("delay")
    public double getDelay() {
        return delay;
    }

    @JsonProperty("commands")
    public ArrayList<SendableCommand> getCommands() {
        return commands;
    }

    @JsonIgnore
    public boolean isDeployable() {
        return deployable;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public void setDelayType(DelayType delayType) {
        this.delayType = delayType;
    }

    public void setDeployable(boolean deployable) {
        this.deployable = deployable;
    }

    @Override
    public int compareTo(@NotNull SendableScript o) {
        return Double.compare(delay, o.delay);
    }
}