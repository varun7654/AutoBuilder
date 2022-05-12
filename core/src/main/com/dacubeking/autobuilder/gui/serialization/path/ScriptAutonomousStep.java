package com.dacubeking.autobuilder.gui.serialization.path;

import com.dacubeking.autobuilder.gui.scripting.sendable.SendableScript;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @Override
    public void execute() {

    }

    @JsonProperty("script")
    public String getScript() {
        return script;
    }

    @JsonProperty("valid")
    public boolean isValid() {
        return valid;
    }

    @JsonProperty("sendableScript")
    public SendableScript getSendableScript() {
        return sendableScript;
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
