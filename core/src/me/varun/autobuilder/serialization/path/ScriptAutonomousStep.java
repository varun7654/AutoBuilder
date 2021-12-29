package me.varun.autobuilder.serialization.path;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.varun.autobuilder.scripting.sendable.SendableScript;

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

    @Override
    public String toString() {
        return "ScriptAutonomousStep{" +
                "script='" + script + '\'' +
                '}';
    }

    @JsonProperty
    public String getScript() {
        return script;
    }

    @JsonProperty
    public boolean isValid() {
        return valid;
    }

    @JsonProperty
    public SendableScript getSendableScript() {
        return sendableScript;
    }
}
