package me.varun.autobuilder.serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Autonomous implements Serializable {
    List<AbstractAutonomousStep> autonomousSteps = new ArrayList<>();

    @Override
    public String toString() {
        return "Autonomous{" +
                "autonomousSteps=" + autonomousSteps +
                '}';
    }
}
