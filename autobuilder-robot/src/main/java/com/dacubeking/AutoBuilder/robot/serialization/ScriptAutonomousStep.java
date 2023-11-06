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

    private final String script;
    private final boolean valid;
    private final SendableScript sendableScript;

    @JsonCreator
    public ScriptAutonomousStep(@JsonProperty(required = true, value = "script") String script,
                                @JsonProperty(required = true, value = "closed") boolean closed,
                                @JsonProperty(required = true, value = "valid") boolean valid,
                                @JsonProperty(required = true, value = "sendableScript") SendableScript sendableScript) {
        super(closed);
        this.script = script;
        this.valid = valid;
        this.sendableScript = sendableScript;
    }

    @JsonProperty
    public SendableScript getSendableScript() {
        return sendableScript;
    }

    /**
     * Runs the script
     */
    @Override
    public boolean execute(@NotNull List<SendableScript> scriptsToExecuteByTime,
                           @NotNull List<SendableScript> scriptsToExecuteByPercent)
            throws CommandExecutionFailedException, ExecutionException {

        if (sendableScript.getDelayType() == SendableScript.DelayType.TIME) {
            scriptsToExecuteByTime.add(sendableScript);
        } else if (sendableScript.getDelayType() == SendableScript.DelayType.PERCENT) {
            scriptsToExecuteByPercent.add(sendableScript);
        } else {
            return sendableScript.execute();
        }

        return true;
    }

    @Override
    public void initialize() {
        sendableScript.initialize();
    }

    @Override
    public void end() {

    }


    @JsonProperty("script")
    public String getScript() {
        return script;
    }

    @JsonProperty("valid")
    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return "ScriptAutonomousStep{" +
                "script='" + script + '\'' +
                ", valid=" + valid +
                ", sendableScript=" + sendableScript +
                '}';
    }
}