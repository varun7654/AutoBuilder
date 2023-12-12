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
import edu.wpi.first.wpilibj2.command.CommandBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class GuiAuto extends CommandBase {


    @Override
    public void execute() {
        if (isFirstRun) {
            if (initialPose != null) {
                AutonomousContainer.getInstance().getInitialPoseSetter().accept(initialPose);
                AutonomousContainer.getInstance().printDebug("Set initial pose: " + initialPose);
            } else {
                AutonomousContainer.getInstance().printDebug("No initial pose set");
            }

            if (!abstractAutonomousSteps.isEmpty()) {
                abstractAutonomousSteps.peek().initialize();
            }

            isFirstRun = false;
        }

        if (abstractAutonomousSteps.isEmpty()) {
            return;
        }

        AbstractAutonomousStep autonomousStep = abstractAutonomousSteps.peek();

        try {
            if (autonomousStep.execute(scriptsToExecuteByTime, scriptsToExecuteByPercent)) {
                autonomousStep.end();
                abstractAutonomousSteps.remove();
                if (!abstractAutonomousSteps.isEmpty()) {
                    abstractAutonomousSteps.peek().initialize();
                }

                //Sort the lists to make sure they are sorted by time
                Collections.sort(scriptsToExecuteByTime);
                Collections.sort(scriptsToExecuteByPercent);
            }
        } catch (CommandExecutionFailedException | ExecutionException e) {
            DriverStation.reportError("Failed to execute autonomous step. " + e.getMessage(), e.getStackTrace());
            abstractAutonomousSteps.clear(); // Will end the auto
        }
    }

    @Override
    public void end(boolean interrupted) {
        if (!abstractAutonomousSteps.isEmpty()) {
            abstractAutonomousSteps.peek().end();
        }

        abstractAutonomousSteps.clear();
    }

    @Override
    public boolean isFinished() {
        return abstractAutonomousSteps.isEmpty();
    }

    private static final Autonomous DO_NOTHING_AUTONOMOUS = new Autonomous(new ArrayList<>());
    private @NotNull Autonomous autonomous = DO_NOTHING_AUTONOMOUS; // default to do nothing in case of some error
    private @Nullable Pose2d initialPose;

    private Callable<Autonomous> autonomousSupplier;

    /**
     * Ensure you are creating the objects for your auto on robot init. The roborio will take multiple seconds to initialize the
     * auto.
     *
     * @param autonomousFile File location of the auto
     */
    public GuiAuto(File autonomousFile) {
        autonomousSupplier = () -> (Autonomous) Serializer.deserializeFromFile(autonomousFile, Autonomous.class,
                autonomousFile.getName().endsWith(".json"));
        this.setName(autonomousFile.getPath() + "GuiAuto");
    }

    /**
     * Ensure you are creating the objects for your auto before you run them. The roborio will take multiple seconds to initialize
     * the auto.
     *
     * @param autonomousJson String of the autonomous
     */
    public GuiAuto(String autonomousJson) {
        autonomousSupplier = () -> (Autonomous) Serializer.deserialize(autonomousJson, Autonomous.class, true);
        this.setName("JsonGuiAuto");
    }


    /**
     * Loads the autonomous and saves the initial pose.
     */
    public void loadAutonomous() throws IOException {
        try {
            if (autonomousSupplier == null) {
                throw new IllegalStateException("Autonomous supplier is null");
            }

            if (autonomous.equals(DO_NOTHING_AUTONOMOUS)) {
                AutonomousContainer.getInstance().printDebug("Loading autonomous: " + this.getName());
                autonomous = autonomousSupplier.call();
                AutonomousContainer.getInstance().printDebug("Loaded autonomous: " + this.getName());
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        for (AbstractAutonomousStep autonomousStep : autonomous.getAutonomousSteps()) {
            if (autonomousStep instanceof TrajectoryAutonomousStep trajectoryAutonomousStep) {
                Trajectory.State initialState = trajectoryAutonomousStep.getTrajectory().getStates().get(0);
                initialPose = new Pose2d(initialState.poseMeters.getTranslation(),
                        trajectoryAutonomousStep.getRotations().get(0).getRotation());
                break;
            }
        }
    }

    @Override
    public void initialize() {
        scriptsToExecuteByPercent = new ArrayList<>();
        scriptsToExecuteByTime = new ArrayList<>();
        abstractAutonomousSteps = new LinkedList<>(autonomous.getAutonomousSteps());
        isFirstRun = true;
    }

    private List<SendableScript> scriptsToExecuteByTime;
    private List<SendableScript> scriptsToExecuteByPercent;
    private LinkedList<AbstractAutonomousStep> abstractAutonomousSteps;

    private boolean isFirstRun = true;

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