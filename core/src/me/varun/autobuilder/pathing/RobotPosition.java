package me.varun.autobuilder.pathing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class RobotPosition {
    public final double x;
    public final double y;
    public final double theta;

    public final double vx;
    public final double vy;
    /**
     * In Radians
     */
    public final double vtheta;

    public final double time;
    public final String name;

    public RobotPosition(double x, double y, double theta, double vx, double vy, double vtheta, double time,
                         String name) {
        this.x = x;
        this.y = y;
        this.theta = theta;
        this.vx = vx;
        this.vy = vy;
        this.vtheta = vtheta;
        this.time = time;
        this.name = name;
    }

    @Contract(pure = true)
    public static RobotPosition fromString(@NotNull String s) {
        String[] split = s.split(",");
        if (split.length != 8) {
            return null;
        }

        return new RobotPosition(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
                Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]),
                Double.parseDouble(split[6]), split[7]);
    }
}
