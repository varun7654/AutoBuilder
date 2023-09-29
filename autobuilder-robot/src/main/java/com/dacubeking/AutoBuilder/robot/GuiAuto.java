package com.dacubeking.AutoBuilder.robot;

import com.dacubeking.AutoBuilder.robot.robotinterface.AutonomousContainer;
import com.dacubeking.AutoBuilder.robot.serialization.AbstractAutonomousStep;
import com.dacubeking.AutoBuilder.robot.serialization.Autonomous;
import com.dacubeking.AutoBuilder.robot.serialization.Serializer;
import com.dacubeking.AutoBuilder.robot.serialization.TrajectoryAutonomousStep;
import com.dacubeking.AutoBuilder.robot.serialization.command.CommandExecutionFailedException;
import com.dacubeking.AutoBuilder.robot.serialization.command.SendableScript;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.dacubeking.AutoBuilder.robot.robotinterface.AutonomousContainer.getCommandTranslator;

public class GuiAuto implements Runnable {

    private static final Autonomous DO_NOTHING_AUTONOMOUS = new Autonomous(new ArrayList<>());
    private @NotNull Autonomous autonomous = DO_NOTHING_AUTONOMOUS; // default to do nothing in case of some error
    private @Nullable Pose2d initialPose;

    /**
     * Ensure you are creating the objects for your auto on robot init. The roborio will take multiple seconds to initialize the
     * auto.
     *
     * @param autonomousFile File location of the auto
     */
    public GuiAuto(File autonomousFile) throws IOException {
        autonomous = (Autonomous) Serializer.deserializeFromFile(autonomousFile, Autonomous.class,
                autonomousFile.getName().endsWith(".json"));
        init();
    }

    /**
     * Ensure you are creating the objects for your auto before you run them. The roborio will take multiple seconds to initialize
     * the auto.
     *
     * @param autonomousJson String of the autonomous
     */
    public GuiAuto(String autonomousJson) {
        try {
            autonomous = (Autonomous) Serializer.deserialize(autonomousJson, Autonomous.class, true);
        } catch (IOException e) {
            DriverStation.reportError("Failed to deserialize auto. " + e.getMessage(), e.getStackTrace());
            // The do nothing auto will be used
        }
        init();
    }

    /**
     * Finds and saves the initial pose of the robot.
     */
    private void init() {
        for (AbstractAutonomousStep autonomousStep : autonomous.getAutonomousSteps()) {
            if (autonomousStep instanceof TrajectoryAutonomousStep) {
                TrajectoryAutonomousStep trajectoryAutonomousStep = (TrajectoryAutonomousStep) autonomousStep;
                Trajectory.State initialState = trajectoryAutonomousStep.getTrajectory().getStates().get(0);
                initialPose = new Pose2d(initialState.poseMeters.getTranslation(),
                        trajectoryAutonomousStep.getRotations().get(0).getRotation());
                break;
            }
        }
    }

    /**
     * Runs the autonomous.
     */
    @Override
    public void run() {
        AutonomousContainer.getInstance().isInitialized();

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            DriverStation.reportError("Uncaught exception in auto thread: " + e.getMessage(), e.getStackTrace());
            getCommandTranslator().stopRobot();
        });

        if (autonomous == DO_NOTHING_AUTONOMOUS) {
            DriverStation.reportError("No auto was loaded. Doing nothing.", false);
            return;
        }

        AutonomousContainer.getInstance().printDebug("Started Running: " + Timer.getFPGATimestamp());


        //Set our initial pose in our robot tracker
        if (initialPose != null) {
            getCommandTranslator().setRobotPose(initialPose);
            AutonomousContainer.getInstance().printDebug("Set initial pose: " + initialPose);
        } else {
            AutonomousContainer.getInstance().printDebug("No initial pose set");
        }

        //Loop though all the steps and execute them
        List<SendableScript> scriptsToExecuteByTime = new ArrayList<>();
        List<SendableScript> scriptsToExecuteByPercent = new ArrayList<>();

        for (AbstractAutonomousStep autonomousStep : autonomous.getAutonomousSteps()) {
            AutonomousContainer.getInstance().printDebug("Doing a step: " + Timer.getFPGATimestamp());

            if (Thread.interrupted()) {
                getCommandTranslator().stopRobot();
                AutonomousContainer.getInstance().printDebug("Auto was interrupted " + Timer.getFPGATimestamp());
                return;
            }

            try {
                autonomousStep.execute(scriptsToExecuteByTime, scriptsToExecuteByPercent);
            } catch (InterruptedException e) {
                getCommandTranslator().stopRobot();
                AutonomousContainer.getInstance().printDebug("Auto prematurely stopped at " + Timer.getFPGATimestamp() +
                        ". This is not an error if you disabled your robot.");
                if (AutonomousContainer.getInstance().areDebugPrintsEnabled()) {
                    e.printStackTrace();
                }
                return;
            } catch (CommandExecutionFailedException | ExecutionException e) {
                getCommandTranslator().stopRobot();
                e.printStackTrace(); // We should always print this out since it is a fatal error
                return;
            }
        }

        System.out.println("Finished Autonomous at " + Timer.getFPGATimestamp());
        getCommandTranslator().stopRobot();
    }

    /**
     * Gets the initial pose of the robot.
     *
     * @return The initial pose of the robot.
     */
    public @Nullable Pose2d getInitialPose() {
        return initialPose;
    }

    @Override
    public String toString() {
        return "GuiAuto{" +
                "initialPose=" + initialPose +
                ",autonomous=" + autonomous +
                '}';
    }
}