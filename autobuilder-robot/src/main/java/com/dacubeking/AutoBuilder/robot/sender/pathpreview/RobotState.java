package com.dacubeking.AutoBuilder.robot.sender.pathpreview;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.Timer;

public class RobotState {
    public final double x;
    public final double y;
    public final double theta;
    public final double vx;
    public final double vy;
    public final double vTheta;
    public final double time;
    public final String name;
    public final double timeCreated;


    /**
     * Constructs a new RobotState.
     *
     * @param x      The x position (meters).
     * @param y      The y position (meters).
     * @param theta  The angle (radians).
     * @param vx     The x velocity (meters/second).
     * @param vy     The y velocity (meters/second).
     * @param vTheta The angular velocity (radians/second).
     * @param time   Timestamp of this position (seconds).
     * @param name   name of this position. (ex: odometry pose, vision pose, etc.) ("Robot Position" is the default and is the one
     *               that is plotted in the GUI)
     */
    public RobotState(double x, double y, double theta, double vx, double vy, double vTheta, double time, String name) {
        this.x = x;
        this.y = y;
        this.theta = theta;
        this.vx = vx;
        this.vy = vy;
        this.vTheta = vTheta;
        this.time = time;
        this.name = name;
        this.timeCreated = Timer.getFPGATimestamp();
    }

    /**
     * Constructs a new RobotState.
     *
     * @param x      The x position (meters).
     * @param y      The y position (meters).
     * @param theta  The angle (radians)
     * @param vx     The x velocity (meters/second).
     * @param vy     The y velocity (meters/second).
     * @param vTheta The angular velocity (radians/second).
     * @param time   Timestamp of this position (seconds).
     */
    public RobotState(double x, double y, double theta, double vx, double vy, double vTheta, double time) {
        this(x, y, theta, vx, vy, vTheta, time, "Robot Position");
    }

    /**
     * Constructs a new RobotState.
     *
     * @param pose   The pose of the robot (meters, radians).
     * @param vx     The x velocity (meters/second).
     * @param vy     The y velocity (meters/second).
     * @param vTheta The angular velocity (radians/second).
     * @param time   Timestamp of this position (seconds).
     */
    public RobotState(Pose2d pose, double vx, double vy, double vTheta, double time) {
        this(pose.getX(), pose.getY(), pose.getRotation().getRadians(), vx, vy, vTheta, time);
    }

    /**
     * Constructs a new RobotState.
     *
     * @param x      The x position (meters).
     * @param y      The y position (meters).
     * @param theta  The angle (radians).
     * @param vx     The x velocity (meters/second).
     * @param vy     The y velocity (meters/second).
     * @param vTheta The angular velocity (radians/second).
     * @param name   name of this position. (ex: odometry pose, vision pose, etc.) ("Robot Position" is the default and is the one
     *               that is plotted in the GUI)
     */
    public RobotState(double x, double y, double theta, double vx, double vy, double vTheta, String name) {
        this(x, y, theta, vx, vy, vTheta, Timer.getFPGATimestamp(), name);
    }


    /**
     * Constructs a new RobotState.
     *
     * @param pose   The pose of the robot (meters, radians).
     * @param vx     The x velocity (meters/second).
     * @param vy     The y velocity (meters/second).
     * @param vTheta The angular velocity (radians/second).
     * @param name   name of this position. (ex: odometry pose, vision pose, etc.) ("Robot Position" is the default and is the one
     *               that is plotted in the GUI)
     */
    public RobotState(Pose2d pose, double vx, double vy, double vTheta, String name) {
        this(pose.getX(), pose.getY(), pose.getRotation().getRadians(), vx, vy, vTheta, name);
    }


    /**
     * Constructs a new RobotState.
     *
     * @param x      The x position (meters).
     * @param y      The y position (meters).
     * @param theta  The angle (radians).
     * @param vx     The x velocity (meters/second).
     * @param vy     The y velocity (meters/second).
     * @param vTheta The angular velocity (radians/second).
     */
    public RobotState(double x, double y, double theta, double vx, double vy, double vTheta) {
        this(x, y, theta, vx, vy, vTheta, "Robot Position");
    }

    /**
     * Constructs a new RobotState.
     *
     * @param pose   The pose of the robot (meters, radians).
     * @param vx     The x velocity (meters/second).
     * @param vy     The y velocity (meters/second).
     * @param vTheta The angular velocity (radians/second).
     */
    public RobotState(Pose2d pose, double vx, double vy, double vTheta) {
        this(pose.getX(), pose.getY(), pose.getRotation().getRadians(), vx, vy, vTheta, "Robot Position");
    }

    /**
     * Constructs a new RobotState.
     *
     * @param x     The x position (meters).
     * @param y     The y position (meters).
     * @param theta The angle (radians).
     * @param name  name of this position. (ex: odometry pose, vision pose, etc.) ("Robot Position" is the default and is the one
     *              that is plotted in the GUI)
     */
    public RobotState(double x, double y, double theta, String name) {
        this(x, y, theta, 0, 0, 0, name);
    }


    /**
     * Constructs a new RobotState.
     *
     * @param pose The pose of the robot (meters, radians).
     * @param name name of this position. (ex: odometry pose, vision pose, etc.) ("Robot Position" is the default and is the one
     *             that is plotted in the GUI)
     */
    public RobotState(Pose2d pose, String name) {
        this(pose.getX(), pose.getY(), pose.getRotation().getRadians(), name);
    }


    /**
     * Constructs a new RobotState.
     *
     * @param x     The x position (meters).
     * @param y     The y position (meters).
     * @param theta The angle (radians).
     */
    public RobotState(double x, double y, double theta) {
        this(x, y, theta, "Robot Position");
    }

    /**
     * Constructs a new RobotState.
     *
     * @param pose The pose of the robot (meters, radians).
     */
    public RobotState(Pose2d pose) {
        this(pose.getX(), pose.getY(), pose.getRotation().getRadians(), "Robot Position");
    }

    /**
     * Constructs a new RobotState.
     *
     * @param x     The x position (meters).
     * @param y     The y position (meters).
     * @param theta The angle (radians).
     * @param time  Timestamp of this position (seconds).
     */
    public RobotState(double x, double y, double theta, double time) {
        this(x, y, theta, 0, 0, 0, time, "Robot Position");
    }

    /**
     * Constructs a new RobotState.
     *
     * @param pose The pose of the robot.
     * @param time Timestamp of this position.
     */
    public RobotState(Pose2d pose, double time) {
        this(pose.getX(), pose.getY(), pose.getRotation().getRadians(), time);
    }

    /**
     * Constructs a new RobotState.
     *
     * @param x     The x position (meters).
     * @param y     The y position (meters).
     * @param theta The angle (radians).
     * @param time  Timestamp of this position.
     * @param name  name of this position. (ex: odometry pose, vision pose, etc.) ("Robot Position" is the default and is the one
     *              that is plotted in the GUI)
     */
    public RobotState(double x, double y, double theta, double time, String name) {
        this(x, y, theta, 0, 0, 0, time, name);
    }

    /**
     * Constructs a new RobotState.
     *
     * @param pose The pose of the robot.
     * @param time Timestamp of this position.
     * @param name name of this position. (ex: odometry pose, vision pose, etc.) ("Robot Position" is the default and is the one
     *             that is plotted in the GUI)
     */
    public RobotState(Pose2d pose, double time, String name) {
        this(pose.getX(), pose.getY(), pose.getRotation().getRadians(), time, name);
    }


    @Override
    public String toString() {
        return x + "," + y + "," + theta + "," + vx + "," + vy + "," + vTheta + "," + time + "," + name;
    }
}
