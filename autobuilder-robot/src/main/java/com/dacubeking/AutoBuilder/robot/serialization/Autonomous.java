package com.dacubeking.AutoBuilder.robot.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Internal
public class Autonomous {
    private final List<AbstractAutonomousStep> autonomousSteps;
    private boolean deployable;

    @JsonCreator
    public Autonomous(@JsonProperty(required = true, value = "autonomousSteps") List<AbstractAutonomousStep> autonomousSteps) {
        this.autonomousSteps = autonomousSteps;
    }

    public void setDeployable(boolean deployable) {
        this.deployable = deployable;
    }

    public boolean isDeployable() {
        return deployable;
    }

    @JsonProperty
    public List<AbstractAutonomousStep> getAutonomousSteps() {
        return autonomousSteps;
    }

    @Override
    @NotNull
    public String toString() {
        return "Autonomous{" +
                "autonomousSteps=" + autonomousSteps +
                '}';
    }
}