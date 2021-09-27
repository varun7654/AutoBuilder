package me.varun.autobuilder.serialization;

import java.io.Serializable;

public abstract class AbstractAutonomousStep implements Serializable {

    public abstract void execute();
}
