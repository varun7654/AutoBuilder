package me.varun.autobuilder.serialization;

import java.io.Serializable;

public abstract class AbstractAutonomousStep implements Serializable {

    private final boolean closed;

    protected AbstractAutonomousStep(boolean closed) {
        this.closed = closed;
    }

    public abstract void execute();

    public boolean isClosed() {
        return closed;
    }
}
