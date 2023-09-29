package com.dacubeking.AutoBuilder.robot.drawable;

import edu.wpi.first.wpilibj.util.Color8Bit;

/**
 * A drawable object that can be drawn to the screen.
 */
public abstract class Drawable {
    public final Color8Bit color;

    protected Drawable(Color8Bit color) {
        this.color = color;
    }

    @Override
    abstract public String toString();
}
