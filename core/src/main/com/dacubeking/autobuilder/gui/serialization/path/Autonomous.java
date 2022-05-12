package com.dacubeking.autobuilder.gui.serialization.path;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Autonomous {
    private final List<AbstractAutonomousStep> autonomousSteps;

    @JsonCreator
    public Autonomous(@JsonProperty(required = true, value = "autonomousSteps") List<AbstractAutonomousStep> autonomousSteps) {

        this.autonomousSteps = autonomousSteps;
    }

    @JsonProperty
    public List<AbstractAutonomousStep> getAutonomousSteps() {
        return autonomousSteps;
    }


    @JsonIgnore
    public boolean deployable = true;

    @Override
    public String toString() {
        return "Autonomous{" +
                "autonomousSteps=" + autonomousSteps +
                '}';
    }
}
