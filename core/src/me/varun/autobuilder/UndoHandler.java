package me.varun.autobuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.varun.autobuilder.events.scroll.InputEventThrower;
import me.varun.autobuilder.gui.Gui;
import me.varun.autobuilder.gui.ScriptItem;
import me.varun.autobuilder.gui.TrajectoryItem;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.serialization.*;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UndoHandler {
    @NotNull List<Autonomous> undoHistory = new ArrayList<>();
    @NotNull List<Autonomous> redoHistory = new ArrayList<>();

    private static final int MAX_UNDO_HISTORY = 100;

    private boolean somethingChanged = false;

    private static final UndoHandler undoHandler = new UndoHandler();
    public static UndoHandler getInstance(){
        return undoHandler;
    }
    private UndoHandler(){

    }

    public void update(Gui gui, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font, @NotNull InputEventThrower inputEventThrower, @NotNull CameraHandler cameraHandler){
        if(somethingChanged){
            Autonomous newState = GuiSerializer.serializeAutonomousForSaving(gui.guiItems);
            undoHistory.add(0, newState);
            if(undoHistory.size()> MAX_UNDO_HISTORY){
                undoHistory.remove(undoHistory.size()-1);
            }
            System.out.println("adding: " + newState);
            redoHistory.clear();
            somethingChanged = false;
        }

        if((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) &&
                Gdx.input.isKeyJustPressed(Input.Keys.Z)){
            if((Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))){
                if(redoHistory.size()>=1){
                    Autonomous autonomous = redoHistory.get(0);
                    System.out.println("redoing: "  + autonomous);
                    restoreState(autonomous, gui, fontShader, font, inputEventThrower, cameraHandler);
                    undoHistory.add(0, redoHistory.get(0));
                    redoHistory.remove(0);
                }
            } else{
                if(undoHistory.size()>=2){
                    Autonomous autonomous = undoHistory.get(1);
                    System.out.println(autonomous);
                    restoreState(autonomous, gui, fontShader, font, inputEventThrower, cameraHandler);
                    redoHistory.add(0, undoHistory.get(0));
                    undoHistory.remove(0);
                }
            }


        }

    }

    public void restoreState(Autonomous autonomous, Gui gui, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font, @NotNull InputEventThrower inputEventThrower, @NotNull CameraHandler cameraHandler){
        List<AbstractGuiItem> guiItemList = new ArrayList<>();
        for (AbstractAutonomousStep autonomousStep : autonomous.getAutonomousSteps()) {
            if(autonomousStep instanceof TrajectoryAutonomousStep){
                TrajectoryAutonomousStep trajectoryAutonomousStep = (TrajectoryAutonomousStep) autonomousStep;
                Color color = new Color().fromHsv(trajectoryAutonomousStep.getColor(), 1, 1);
                TrajectoryItem trajectoryItem = new TrajectoryItem(gui, fontShader, font, inputEventThrower, cameraHandler,
                        trajectoryAutonomousStep.getPose2DList(), trajectoryAutonomousStep.isReversed(), color, trajectoryAutonomousStep.isClosed());
                guiItemList.add(trajectoryItem);


            } else if(autonomousStep instanceof ScriptAutonomousStep){
                ScriptAutonomousStep scriptAutonomousStep = (ScriptAutonomousStep) autonomousStep;
                ScriptItem scriptItem = new ScriptItem(fontShader, font, inputEventThrower, scriptAutonomousStep.getScript(), scriptAutonomousStep.isClosed());
                guiItemList.add(scriptItem);
            }
        }
        gui.guiItems = guiItemList;

    }

    public void somethingChanged(){
        somethingChanged = true;
    }

}
