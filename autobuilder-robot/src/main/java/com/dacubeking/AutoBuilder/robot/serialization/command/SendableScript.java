package com.dacubeking.AutoBuilder.robot.serialization.command;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SendableScript implements Comparable<SendableScript> {


    /**
     * @throws InterruptedException if the thread is interrupted while executing the commands
     */
    public void execute() throws InterruptedException, CommandExecutionFailedException, ExecutionException {
        for (SendableCommand command : commands) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Interrupted while trying to execute a script ");
            }
            command.execute();
        }
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