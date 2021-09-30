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

    public synchronized void setStateWin(){
        setState(VisionStatus.WIN);
    }

    public synchronized void setStateIdle(){
        setState(VisionStatus.IDLE);
    }

    public synchronized void setStateAiming(){
        setState(VisionStatus.AIMING);
    }

    public synchronized boolean isFinished(){
        return false;
    }


}
