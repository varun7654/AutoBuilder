package me.varun.autobuilder.scriptengine;


public class Shooter{

    public Shooter(){
    }

    private static final Shooter instance = new Shooter();

    public static Shooter getInstance() {
        return instance;
    }

    public enum ShooterState {
        OFF, SPINNING, HOMING, EJECT
    }

    public void configPID(){
    }

    /**
     * Sets the flywheel speed. The shooter will turn off is the speed is set to 0
     * and will automatically turn on if it is greater than 0.
     *
     * Will do nothing is the robot is currently homeing.
     * @param speed Speed to set flywheel too
     */
    public synchronized void setSpeed(double speed) {
    }
    /**
     * Note the shooter will only fire
     * if the hood has reach the correct position and if the shooter is inside the max deviation
     * @param fire sets if the shooter should fire balls
     */
    public synchronized void setFiring(boolean fire){
    }

    public synchronized void update(){
    }
    /**
     * Will check if the hood angle is within acceptable ranges before setting the position
     * @param angle sets the hood angle starting from the closed position
     */
    public synchronized void setHoodAngle(double angle) {
    }
    /**
     *
     * @return the current hood angle
     */
    public double getHoodAngle() {
        return 0;
    }
    /**
     * Starts the homeing routine for the hood. Fallback timer will begin one the robot is enabled.
     */
    public synchronized void homeHood(){
    }

    /**
     *
     * @return has the hood finished homeing
     */
    public synchronized boolean isHomed(){
        return false;
    }

    /**
     *
     * @param eject set to true if the shooter should run in eject mode
     */
    public synchronized void setEject(boolean eject){
    }

    /**
     *
     * @return returns true one the shooter has reached an acceptable speed
     */
    public synchronized boolean isShooterSpeedOKAuto(){
        return false;
    }




    private double getRPM(){
        return 0;


    }

    public boolean getHomeSwitch(){
        return false;
    }
}