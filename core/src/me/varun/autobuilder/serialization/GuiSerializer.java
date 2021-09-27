package me.varun.autobuilder.serialization;

import me.varun.autobuilder.gui.ScriptItem;
import me.varun.autobuilder.gui.TrajectoryItem;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;

import java.util.List;

public class GuiSerializer {
    public static Autonomous serializeAutonomous(List<AbstractGuiItem> guiItemList){
        Autonomous autonomous = new Autonomous();
        for (AbstractGuiItem abstractGuiItem : guiItemList) {
            if(abstractGuiItem instanceof ScriptItem){
                ScriptItem scriptItem = (ScriptItem) abstractGuiItem;
                autonomous.autonomousSteps.add(new ScriptAutonomousStep(scriptItem.getText()));
            }

            if(abstractGuiItem instanceof TrajectoryItem){
                TrajectoryItem trajectoryItem = (TrajectoryItem) abstractGuiItem;
                autonomous.autonomousSteps.add(new TrajectoryAutonomousStep(trajectoryItem.getPathRenderer().getNotNullTrajectory().getStates(), trajectoryItem.getPathRenderer().getPoint2DList()));
            }
        }
        return autonomous;
    }
}
