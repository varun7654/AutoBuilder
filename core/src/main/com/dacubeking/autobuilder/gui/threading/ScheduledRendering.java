package com.dacubeking.autobuilder.gui.threading;

import com.dacubeking.autobuilder.gui.AutoBuilder;

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
