package me.varun.autobuilder.serialization.path;

import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.path.ScriptItem;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.wpi.math.trajectory.TrajectoryGenerator.ControlVectorList;

import java.util.ArrayList;
import java.util.List;

public class GuiSerializer {
    public static Autonomous serializeAutonomousForDeployment(List<AbstractGuiItem> mutableGuiItemList) {
        List<AbstractAutonomousStep> autonomousSteps = new ArrayList<>();
        List<AbstractGuiItem> guiItemList = new ArrayList<>(mutableGuiItemList);
        for (AbstractGuiItem abstractGuiItem : guiItemList) {
            if (abstractGuiItem instanceof ScriptItem) {
                ScriptItem scriptItem = (ScriptItem) abstractGuiItem;
                autonomousSteps.add(new ScriptAutonomousStep(scriptItem.getText(), scriptItem.isClosed(), scriptItem.isValid(), scriptItem.getSendableScript()));
            }

            if (abstractGuiItem instanceof TrajectoryItem) {
                TrajectoryItem trajectoryItem = (TrajectoryItem) abstractGuiItem;
                autonomousSteps.add(new TrajectoryAutonomousStep(
                        trajectoryItem.getPathRenderer().getNotNullTrajectory().getStates(),
                        null,
                        trajectoryItem.getPathRenderer().getRotations(),
                        trajectoryItem.getPathRenderer().isReversed(),
                        0,
                        trajectoryItem.isClosed(),
                        trajectoryItem.getPathRenderer().getVelocityStart(),
                        trajectoryItem.getPathRenderer().getVelocityEnd()));
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
                        new ArrayList<>(trajectoryItem.getPathRenderer().getRotations()),
                        trajectoryItem.getPathRenderer().isReversed(),
                        color[0],
                        trajectoryItem.isClosed(),
                        trajectoryItem.getPathRenderer().getVelocityStart(),
                        trajectoryItem.getPathRenderer().getVelocityEnd()));
            }
        }
        return new Autonomous(autonomousSteps);
    }

    public static Autonomous serializeAutonomous(List<AbstractGuiItem> mutableGuiItemList) {
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
                        trajectoryItem.getPathRenderer().getNotNullTrajectory().getStates(),
                        trajectoryItem.getPathRenderer().getControlVectors(),
                        trajectoryItem.getPathRenderer().getRotations(),
                        trajectoryItem.getPathRenderer().isReversed(),
                        color[0],
                        trajectoryItem.isClosed(),
                        trajectoryItem.getPathRenderer().getVelocityStart(),
                        trajectoryItem.getPathRenderer().getVelocityEnd()));
            }
        }
        return new Autonomous(autonomousSteps);
    }
}
