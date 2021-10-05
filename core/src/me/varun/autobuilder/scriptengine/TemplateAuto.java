package me.varun.autobuilder.scriptengine;

@SuppressWarnings("unused")
public class TemplateAuto implements Runnable {

    //CollisionManager collisionManager = CollisionManager.getInstance();

    int side = 1;

    boolean killSwitch = false;
    boolean done = false;
    //Translation 2D is in inches.

    public TemplateAuto(Translation2D start) {
    }

    public TemplateAuto(Translation2D start, int side) {

    }

    public Translation2D here() {
        return new Translation2D();
    }

    public Rotation2D dir() {
        return new Rotation2D();
    }

    synchronized public void killSwitch() {
        killSwitch = true;
    }

    synchronized public boolean isDead() {
        return killSwitch;
    }

    synchronized public boolean isFinished() {
        return done;
    }

    @Override
    public void run(){}

    public void turnOnIntakeTrack() {
    }


    public void turnOffIntakeTrack() {
    }

    synchronized public void shootBalls (int amountOfBalls){


    }



}

