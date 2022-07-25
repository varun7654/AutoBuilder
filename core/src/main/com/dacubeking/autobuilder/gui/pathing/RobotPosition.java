package com.dacubeking.autobuilder.gui.pathing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param vtheta In Radians
 */
public record RobotPosition(double x, double y, double theta, double vx, double vy, double vtheta, double time, String name) {

    @Contract(pure = true)
    public static @Nullable RobotPosition fromString(@NotNull String s) {
        String[] split = s.split(",");
        if (split.length != 8) {
            return null;
        }

        return new RobotPosition(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
                Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]),
                Double.parseDouble(split[6]), split[7]);
    }
}
