package me.varun.autobuilder.threading;

import me.varun.autobuilder.AutoBuilder;

public class ScheduledRendering implements Runnable {

    public long delay;

    public ScheduledRendering(long delay) {
        this.delay = delay;
    }


    @Override
    public void run() {
        AutoBuilder.requestRendering();
    }
}
