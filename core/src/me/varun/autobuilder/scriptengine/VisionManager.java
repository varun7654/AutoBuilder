package me.varun.autobuilder.scriptengine;

public class VisionManager {


    private static final VisionManager vm = new VisionManager();


    public synchronized VisionStatus getState() {
        return null;
    }

    public static VisionManager getInstance() {
        return vm;
    }

    private VisionManager() {
    }

    public enum VisionStatus {
        AIMING, IDLE, WIN
    }

    public void resetWin() {
    }


    synchronized public void update() {

    }

    public void stop() {
    }

    public synchronized void setState(VisionStatus state){
    }

    public synchronized boolean isFinished(){
        return false;
    }


}
