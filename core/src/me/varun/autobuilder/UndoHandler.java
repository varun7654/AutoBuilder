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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UndoHandler {
    private static final int MAX_UNDO_HISTORY = 100;
    private static final UndoHandler undoHandler = new UndoHandler();
    @NotNull List<Autonomous> undoHistory = new ArrayList<>();
    int pointer;
    private boolean somethingChanged = false;

    private UndoHandler() {

    }

    public static UndoHandler getInstance() {
        return undoHandler;
    }

    public void update(Gui gui, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font, @NotNull InputEventThrower inputEventThrower, @NotNull CameraHandler cameraHandler) {
        if (somethingChanged) {
            Autonomous newState = GuiSerializer.serializeAutonomousForSaving(gui.guiItems);
            while (pointer > 0) {
                undoHistory.remove(0);
                pointer--;
            }
            undoHistory.add(0, newState);
            if (undoHistory.size() > MAX_UNDO_HISTORY) {
                undoHistory.remove(undoHistory.size() - 1);
            }
            //System.out.println("adding: " + newState);
            somethingChanged = false;
        }

        if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) &&
                Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            if ((Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
                pointer--;
                if (pointer >= 0) {
                    restoreState(undoHistory.get(pointer), gui, fontShader, font, inputEventThrower, cameraHandler);
                    //System.out.println("redoing to: " + undoHistory.get(pointer));
                } else pointer = 0;
            } else {
                //Undoing
                System.out.println(undoHistory);
                pointer++;
                if (pointer < undoHistory.size()) {
                    restoreState(undoHistory.get(pointer), gui, fontShader, font, inputEventThrower, cameraHandler);
                    //System.out.println("undoing to: " + undoHistory.get(pointer));
                } else pointer = undoHistory.size() - 1;

            }


        }

    }

    public void restoreState(Autonomous autonomous, Gui gui, @NotNull ShaderProgram fontShader, @NotNull BitmapFont font,
                             @NotNull InputEventThrower inputEventThrower, @NotNull CameraHandler cameraHandler) {
        List<AbstractGuiItem> guiItemList = new ArrayList<>();
        for (AbstractAutonomousStep autonomousStep : autonomous.getAutonomousSteps()) {
            if (autonomousStep instanceof TrajectoryAutonomousStep) {
                TrajectoryAutonomousStep trajectoryAutonomousStep = (TrajectoryAutonomousStep) autonomousStep;
                Color color = new Color().fromHsv(trajectoryAutonomousStep.getColor(), 1, 1);
                color.set(color.r, color.g, color.b, 1);
                TrajectoryItem trajectoryItem = new TrajectoryItem(gui, fontShader, font, inputEventThrower, cameraHandler,
                        new ArrayList<>(trajectoryAutonomousStep.getPose2DList()), trajectoryAutonomousStep.isReversed(),
                        color, trajectoryAutonomousStep.isClosed(), trajectoryAutonomousStep.getVelocityStart(),
                        trajectoryAutonomousStep.getVelocityEnd());
                guiItemList.add(trajectoryItem);


            } else if (autonomousStep instanceof ScriptAutonomousStep) {
                ScriptAutonomousStep scriptAutonomousStep = (ScriptAutonomousStep) autonomousStep;
                ScriptItem scriptItem = new ScriptItem(fontShader, font, inputEventThrower, scriptAutonomousStep.getScript(),
                        scriptAutonomousStep.isClosed(), scriptAutonomousStep.isValid());
                guiItemList.add(scriptItem);
            }
        }
        for (AbstractGuiItem guiItem : gui.guiItems) {
            guiItem.dispose();
        }
        gui.guiItems = guiItemList;

    }

    public void somethingChanged() {
        somethingChanged = true;
    }

}
