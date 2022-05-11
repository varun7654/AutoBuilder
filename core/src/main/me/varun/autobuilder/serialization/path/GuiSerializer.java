package me.varun.autobuilder.serialization.path;

import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.path.ScriptItem;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.pathing.TimedRotation;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator.ControlVectorList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GuiSerializer {
    public static Autonomous serializeAutonomousForDeployment(List<AbstractGuiItem> mutableGuiItemList) throws NotDeployableException {
        List<AbstractAutonomousStep> autonomousSteps = new ArrayList<>();
        List<AbstractGuiItem> guiItemList = new ArrayList<>(mutableGuiItemList);
        for (AbstractGuiItem abstractGuiItem : guiItemList) {
            if (abstractGuiItem instanceof ScriptItem) {
                ScriptItem scriptItem = (ScriptItem) abstractGuiItem;
                autonomousSteps.add(new ScriptAutonomousStep(scriptItem.getText(), scriptItem.isClosed(), scriptItem.isValid(), scriptItem.getSendableScript()));
            }

            if (abstractGuiItem instanceof TrajectoryItem) {
                TrajectoryItem trajectoryItem = (TrajectoryItem) abstractGuiItem;

                try {
                    autonomousSteps.add(new TrajectoryAutonomousStep(
                            trajectoryItem.getPathRenderer().getNotNullTrajectory().getStates(),
                            null,
                            trajectoryItem.getPathRenderer().getRotationsAndTimes(),
                            trajectoryItem.getPathRenderer().isReversed(),
                            0,
                            trajectoryItem.isClosed(),
                            trajectoryItem.getPathRenderer().getVelocityStart(),
                            trajectoryItem.getPathRenderer().getVelocityEnd(),
                            trajectoryItem.getPathRenderer().getConstraints()));
                } catch (ExecutionException e) {
                    throw new NotDeployableException("Trajectory is not deployable");
                }
            }
        }
        for (AbstractAutonomousStep autonomousStep : autonomousSteps) {
            if (autonomousStep instanceof ScriptAutonomousStep) {
                ScriptAutonomousStep scriptAutonomousStep = (ScriptAutonomousStep) autonomousStep;
                if (!scriptAutonomousStep.getSendableScript().isDeployable()) {
                    throw new NotDeployableException("Script is not deployable");
                }
            }
        }
        return new Autonomous(autonomousSteps);
    }

    public static Autonomous serializeAutonomousForUndoHistory(List<AbstractGuiItem> mutableGuiItemList) {
        List<AbstractAutonomousStep> autonomousSteps = new ArrayList<>();
        List<AbstractGuiItem> guiItemList = new ArrayList<>(mutableGuiItemList);
        for (AbstractGuiItem abstractGuiItem : guiItemList) {
            if (abstractGuiItem instanceof ScriptItem) {
                ScriptItem scriptItem = (ScriptItem) abstractGuiItem;
                autonomousSteps.add(new ScriptAutonomousStep(
                        scriptItem.getText(),
                        scriptItem.isClosed(),
                        scriptItem.isValid(),
                        scriptItem.getSendableScript()));
            }

            if (abstractGuiItem instanceof TrajectoryItem) {
                TrajectoryItem trajectoryItem = (TrajectoryItem) abstractGuiItem;
                float[] color = new float[3];
                trajectoryItem.getPathRenderer().getColor().toHsv(color);
                autonomousSteps.add(new TrajectoryAutonomousStep(
                        null,
                        new ControlVectorList(trajectoryItem.getPathRenderer().getControlVectors()),
                        trajectoryItem.getPathRenderer().getRotations().stream().map(TimedRotation::new)
                                .collect(Collectors.toList()), // Add fake times as they are not used in undo history
                        trajectoryItem.getPathRenderer().isReversed(),
                        color[0],
                        trajectoryItem.isClosed(),
                        trajectoryItem.getPathRenderer().getVelocityStart(),
                        trajectoryItem.getPathRenderer().getVelocityEnd(),
                        trajectoryItem.getPathRenderer().getConstraints()));
            }
        }
        return new Autonomous(autonomousSteps);
    }

    public static Autonomous serializeAutonomous(List<AbstractGuiItem> mutableGuiItemList) {
        boolean deployable = true;
        List<AbstractAutonomousStep> autonomousSteps = new ArrayList<>();
        List<AbstractGuiItem> guiItemList = new ArrayList<>(mutableGuiItemList);
        for (AbstractGuiItem abstractGuiItem : guiItemList) {
            if (abstractGuiItem instanceof ScriptItem) {
                ScriptItem scriptItem = (ScriptItem) abstractGuiItem;
                autonomousSteps.add(new ScriptAutonomousStep(
                        scriptItem.getText(),
                        scriptItem.isClosed(),
                        scriptItem.isValid(),
                        scriptItem.getSendableScript()));
            }

            if (abstractGuiItem instanceof TrajectoryItem) {
                TrajectoryItem trajectoryItem = (TrajectoryItem) abstractGuiItem;
                float[] color = new float[3];
                trajectoryItem.getPathRenderer().getColor().toHsv(color);

                List<Trajectory.State> states = null;
                try {
                    states = trajectoryItem.getPathRenderer().getNotNullTrajectory().getStates();
                } catch (ExecutionException e) {
                    deployable = false;
                }

                autonomousSteps.add(new TrajectoryAutonomousStep(
                        states,
                        trajectoryItem.getPathRenderer().getControlVectors(),
                        deployable ? trajectoryItem.getPathRenderer().getRotationsAndTimes() :
                                trajectoryItem.getPathRenderer().getRotations().stream().map(TimedRotation::new)
                                        .collect(Collectors.toList()), // Add fake times as they are not used in undo history,
                        trajectoryItem.getPathRenderer().isReversed(),
                        color[0],
                        trajectoryItem.isClosed(),
                        trajectoryItem.getPathRenderer().getVelocityStart(),
                        trajectoryItem.getPathRenderer().getVelocityEnd(),
                        trajectoryItem.getPathRenderer().getConstraints()));
            }
        }
        Autonomous autonomous = new Autonomous(autonomousSteps);

        for (AbstractAutonomousStep autonomousStep : autonomous.getAutonomousSteps()) {
            if (autonomousStep instanceof ScriptAutonomousStep) {
                ScriptAutonomousStep scriptAutonomousStep = (ScriptAutonomousStep) autonomousStep;
                if (!scriptAutonomousStep.getSendableScript().isDeployable()) deployable = false;
            }
        }

        autonomous.deployable = deployable;
        return autonomous;
    }
}
