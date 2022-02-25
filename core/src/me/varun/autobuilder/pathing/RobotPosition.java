package me.varun.autobuilder.pathing;

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
}
