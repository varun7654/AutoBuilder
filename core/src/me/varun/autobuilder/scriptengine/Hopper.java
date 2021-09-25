package me.varun.autobuilder.scriptengine;

public class Hopper{
    public enum  FrontMotorState {
        ACTIVE, REVERSE, INACTIVE
    }
    public enum SnailMotorState {
        ACTIVE, REVERSE, INACTIVE
    }

    private static final Hopper instance = new Hopper();
    public static Hopper getInstance() {
        return instance;
    }


    private Hopper() {

    }

    public FrontMotorState getFrontMotorState() {
        return null;
    }

    public SnailMotorState getSnailMotorState() {
        return null;
    }

    public double getCurrent() {
        return 0;
    }

    public void setFrontSpeed(double Frontspeed) {
    }

    public void setSnailSpeed(double Snailspeed) {
    }
    public void setFrontMotorState(final FrontMotorState frontMotorState) {
    }

    public void setSnailMotorState(SnailMotorState snailMotorState, boolean slow) {
    }

    public void update() {

    }
}