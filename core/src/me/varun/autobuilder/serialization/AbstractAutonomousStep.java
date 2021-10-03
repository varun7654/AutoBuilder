package me.varun.autobuilder.serialization;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @Type(value = TrajectoryAutonomousStep.class, name = "trajectory"),
        @Type(value = ScriptAutonomousStep.class, name = "script"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractAutonomousStep {

    private final boolean closed;

    @JsonCreator
    protected AbstractAutonomousStep(@JsonProperty(required = true, value = "closed") boolean closed) {
        this.closed = closed;
    }

    public abstract void execute();

    @JsonProperty("closed")
    public boolean isClosed() {
        return closed;
    }
}
