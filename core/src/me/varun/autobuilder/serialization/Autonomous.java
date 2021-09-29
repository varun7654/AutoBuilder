package me.varun.autobuilder.serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Autonomous implements Serializable {
    private final List<AbstractAutonomousStep> autonomousSteps;

    public Autonomous(List<AbstractAutonomousStep> autonomousSteps){

        this.autonomousSteps = autonomousSteps;
    }

    public List<AbstractAutonomousStep> getAutonomousSteps() {
        return autonomousSteps;
    }

    @Override
    public String toString() {
        return "Autonomous{" +
                "autonomousSteps=" + autonomousSteps +
                '}';
    }
}
