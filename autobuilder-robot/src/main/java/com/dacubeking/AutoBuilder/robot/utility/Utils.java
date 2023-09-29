package com.dacubeking.AutoBuilder.robot.utility;

import edu.wpi.first.wpilibj.util.Color8Bit;

public final class Utils {

    private Utils() {
        // Private constructor to prevent instantiation.
    }

    public static String getColorAsHex(Color8Bit color) {
        return String.format("#%02x%02x%02x", color.red, color.green, color.blue);
    }
}
