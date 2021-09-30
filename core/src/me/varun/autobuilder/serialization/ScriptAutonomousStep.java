package me.varun.autobuilder.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScriptAutonomousStep extends AbstractAutonomousStep{

    private final String script;
    private final boolean valid;

    @JsonCreator
    public ScriptAutonomousStep(@JsonProperty(required = true, value = "script") String script,
                                @JsonProperty(required = true, value = "closed") boolean closed,
                                @JsonProperty(required = true, value = "valid") boolean valid) {
        super(closed);
        this.script = script;
        this.valid = valid;
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
}
