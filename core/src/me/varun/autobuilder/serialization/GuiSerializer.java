package me.varun.autobuilder.serialization;

import me.varun.autobuilder.gui.path.ScriptItem;
import me.varun.autobuilder.gui.path.TrajectoryItem;
import me.varun.autobuilder.gui.path.AbstractGuiItem;

import java.util.ArrayList;
import java.util.List;

public class GuiSerializer {
    public static Autonomous serializeAutonomousForDeployment(List<AbstractGuiItem> mutableGuiItemList) {
        List<AbstractAutonomousStep> autonomousSteps = new ArrayList<>();
        List<AbstractGuiItem> guiItemList = new ArrayList<>(mutableGuiItemList);
        for (AbstractGuiItem abstractGuiItem : guiItemList) {
            if (abstractGuiItem instanceof ScriptItem) {
                ScriptItem scriptItem = (ScriptItem) abstractGuiItem;
                autonomousSteps.add(new ScriptAutonomousStep(scriptItem.getText(), scriptItem.isClosed(), scriptItem.isValid()));
            }

            if (abstractGuiItem instanceof TrajectoryItem) {
                TrajectoryItem trajectoryItem = (TrajectoryItem) abstractGuiItem;
                autonomousSteps.add(new TrajectoryAutonomousStep(trajectoryItem.getPathRenderer().getNotNullTrajectory().getStates(),
                        null, trajectoryItem.getPathRenderer().isReversed(), 0, trajectoryItem.isClosed(),
                        trajectoryItem.getPathRenderer().getVelocityStart(), trajectoryItem.getPathRenderer().getVelocityEnd()));
            }
        }
        return new Autonomous(autonomousSteps);
    }

    public static Autonomous serializeAutonomousForSaving(List<AbstractGuiItem> mutableGuiItemList) {
        List<AbstractAutonomousStep> autonomousSteps = new ArrayList<>();
        List<AbstractGuiItem> guiItemList = new ArrayList<>(mutableGuiItemList);
        for (AbstractGuiItem abstractGuiItem : guiItemList) {
            if (abstractGuiItem instanceof ScriptItem) {
                ScriptItem scriptItem = (ScriptItem) abstractGuiItem;
                autonomousSteps.add(new ScriptAutonomousStep(scriptItem.getText(), scriptItem.isClosed(), scriptItem.isValid()));
            }

            if (abstractGuiItem instanceof TrajectoryItem) {
                TrajectoryItem trajectoryItem = (TrajectoryItem) abstractGuiItem;
                float[] color = new float[3];
                trajectoryItem.getPathRenderer().getColor().toHsv(color);
                autonomousSteps.add(new TrajectoryAutonomousStep(null, new ArrayList<>(trajectoryItem.getPathRenderer().getPoint2DList()),
                        trajectoryItem.getPathRenderer().isReversed(), color[0], trajectoryItem.isClosed(),
                        trajectoryItem.getPathRenderer().getVelocityStart(), trajectoryItem.getPathRenderer().getVelocityEnd()));
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
                autonomousSteps.add(new ScriptAutonomousStep(scriptItem.getText(), scriptItem.isClosed(), scriptItem.isValid()));
            }

            if (abstractGuiItem instanceof TrajectoryItem) {
                TrajectoryItem trajectoryItem = (TrajectoryItem) abstractGuiItem;
                float[] color = new float[3];
                trajectoryItem.getPathRenderer().getColor().toHsv(color);
                autonomousSteps.add(new TrajectoryAutonomousStep(trajectoryItem.getPathRenderer().getNotNullTrajectory().getStates(),
                        trajectoryItem.getPathRenderer().getPoint2DList(), trajectoryItem.getPathRenderer().isReversed(), color[0],
                        trajectoryItem.isClosed(), trajectoryItem.getPathRenderer().getVelocityStart(),
                        trajectoryItem.getPathRenderer().getVelocityEnd()));
            }
        }
        return new Autonomous(autonomousSteps);
    }
}
