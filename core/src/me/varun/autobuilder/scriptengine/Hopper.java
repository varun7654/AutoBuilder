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

    public void setSnailMotorStateActive(boolean slow) {
        setSnailMotorState(SnailMotorState.ACTIVE, slow);
    }

    public void setSnailMotorStateInactive(boolean slow) {
        setSnailMotorState(SnailMotorState.INACTIVE, slow);
    }

    public void setSnailMotorStateReverse(boolean slow) {
        setSnailMotorState(SnailMotorState.REVERSE, slow);
    }

    public void setFrontMotorStateActive() {
        setFrontMotorState(FrontMotorState.ACTIVE);
    }

    public void setFrontMotorStateInactive() {
        setFrontMotorState(FrontMotorState.INACTIVE);
    }

    public void setFrontMotorStateReverse() {
        setFrontMotorState(FrontMotorState.REVERSE);
    }


    public void update() {

    }
}