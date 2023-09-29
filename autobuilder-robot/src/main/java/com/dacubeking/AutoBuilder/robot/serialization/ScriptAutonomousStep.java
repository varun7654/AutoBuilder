package com.dacubeking.AutoBuilder.robot.serialization;

import com.dacubeking.AutoBuilder.robot.serialization.command.CommandExecutionFailedException;
import com.dacubeking.AutoBuilder.robot.serialization.command.SendableScript;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;


@JsonIgnoreProperties(ignoreUnknown = true)
@Internal
public class ScriptAutonomousStep extends AbstractAutonomousStep {

    private final SendableScript sendableScript;

    @JsonCreator
    private ScriptAutonomousStep(@JsonProperty(required = true, value = "sendableScript") SendableScript sendableScript) {
        this.sendableScript = sendableScript;
    }

    @Override
    @NotNull
    public String toString() {
        return "ScriptAutonomousStep{" + "sendableScript='" + sendableScript + '\'' + '}';
    }

    @JsonProperty
    private SendableScript getSendableScript() {
        return sendableScript;
    }

    /**
     * Runs the script
     */
    @Override
    public void execute(@NotNull List<SendableScript> scriptsToExecuteByTime,
                        @NotNull List<SendableScript> scriptsToExecuteByPercent)
            throws InterruptedException, CommandExecutionFailedException, ExecutionException {

        if (sendableScript.getDelayType() == SendableScript.DelayType.TIME) {
            scriptsToExecuteByTime.add(sendableScript);
            return;
        }

        if (sendableScript.getDelayType() == SendableScript.DelayType.PERCENT) {
            scriptsToExecuteByPercent.add(sendableScript);
            return;
        }

        sendableScript.execute();
    }
}