package com.dacubeking.AutoBuilder.robot.drawable;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Renderer {
    private static final NetworkTableEntry drawables = NetworkTableInstance.getDefault().getEntry("autodata/drawables");

    public static void render(Drawable... drawable) {
        String[] drawableStrings = new String[drawable.length];
        for (int i = 0; i < drawable.length; i++) {
            drawableStrings[i] = drawable[i].toString();
        }
        drawables.setStringArray(drawableStrings);
    }
}
