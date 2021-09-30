package me.varun.autobuilder.scriptengine;

public class Intake{

    public enum DeployState {
        DEPLOY, UNDEPLOY
    }
    public enum IntakeState {
        INTAKE, OFF, EJECT
    }


    private static final Intake instance = new Intake();
    public static Intake getInstance() {
        return instance;
    }

    private Intake() {
    }

    public synchronized DeployState getDeployState() {
        return null;
    }

    public synchronized IntakeState getIntakeState() {
        return null;
    }

    public void setDeployState(final DeployState deployState) {
    }

    public synchronized void setIntakeState(IntakeState intakeState) {
    }

    public void setDeployStateDeploy() {
        setDeployState(DeployState.DEPLOY);
    }

    public void setDeployStateUnDeploy() {
        setDeployState(DeployState.UNDEPLOY);
    }

    public synchronized void setIntakeStateIntake() {
        setIntakeState(IntakeState.INTAKE);
    }

    public synchronized void setIntakeStateOff() {
        setIntakeState(IntakeState.OFF);
    }

    public synchronized void setIntakeStateEject() {
        setIntakeState(IntakeState.EJECT);
    }

    public synchronized void setSpeed(double speed) {
    }

    public synchronized double getCurrent() {
        return 0;
    }
}