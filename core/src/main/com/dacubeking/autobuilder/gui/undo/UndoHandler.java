package com.dacubeking.autobuilder.gui.undo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.CameraHandler;
import com.dacubeking.autobuilder.gui.config.Config;
import com.dacubeking.autobuilder.gui.config.gui.FileHandler;
import com.dacubeking.autobuilder.gui.gui.path.AbstractGuiItem;
import com.dacubeking.autobuilder.gui.gui.path.PathGui;
import com.dacubeking.autobuilder.gui.gui.path.ScriptItem;
import com.dacubeking.autobuilder.gui.gui.path.TrajectoryItem;
import com.dacubeking.autobuilder.gui.serialization.path.*;
import com.dacubeking.autobuilder.gui.wpi.math.trajectory.TrajectoryGenerator.ControlVectorList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class UndoHandler {
    private static final int MAX_UNDO_HISTORY = 1000;
    private static final UndoHandler undoHandler = new UndoHandler();
    private @NotNull List<UndoState> undoHistory = new ArrayList<>(MAX_UNDO_HISTORY);
    int pointer = 0;
    private boolean somethingChanged = false;

    private long lastUndoSaveTime = 0;

    private static final long UNDO_SAVE_INTERVAL = 1000;

    private UndoHandler() {

    }

    public static UndoHandler getInstance() {
        return undoHandler;
    }

    public synchronized void update(PathGui pathGui, @NotNull CameraHandler cameraHandler) {
        if (somethingChanged && System.currentTimeMillis() - lastUndoSaveTime > UNDO_SAVE_INTERVAL) {
            saveCurrentState(pathGui);
        }

        if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) &&
                Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            if (somethingChanged) {
                saveCurrentState(pathGui);
            }
            if ((Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
                pointer--;
                if (pointer >= 0) {
                    restoreState(undoHistory.get(pointer), pathGui, cameraHandler);
                    //System.out.println("redoing to: " + undoHistory.get(pointer));
                } else {
                    pointer = 0;
                }
            } else {
                //Undoing
                //System.out.println(undoHistory);
                pointer++;
                if (pointer < undoHistory.size()) {
                    restoreState(undoHistory.get(pointer), pathGui, cameraHandler);
                    //System.out.println("undoing to: " + undoHistory.get(pointer));
                } else {
                    pointer = undoHistory.size() - 1;
                }
            }

            FileHandler.saveAuto(true);
        }
    }

    private synchronized void saveCurrentState(PathGui pathGui) {
        Autonomous newAutonomousState = GuiSerializer.serializeAutonomousForUndoHistory(pathGui.guiItems);
        while (pointer > 0) {
            undoHistory.remove(0);
            pointer--;
        }
        UndoState newState = new UndoState(newAutonomousState, new Config(AutoBuilder.getConfig()));
        undoHistory.add(0, newState);
        if (undoHistory.size() > MAX_UNDO_HISTORY) {
            undoHistory.remove(undoHistory.size() - 1);
        }
        // System.out.println("adding: " + newState);
        somethingChanged = false;
        lastUndoSaveTime = System.currentTimeMillis();
        FileHandler.saveAuto(true);
    }

    public synchronized void restoreState(UndoState undoState, PathGui pathGui,
                                          @NotNull CameraHandler cameraHandler) {
        List<AbstractGuiItem> guiItemList = new ArrayList<>();
        for (AbstractAutonomousStep autonomousStep : undoState.autonomous().getAutonomousSteps()) {
            if (autonomousStep instanceof TrajectoryAutonomousStep trajectoryAutonomousStep) {
                Color color = new Color().fromHsv(trajectoryAutonomousStep.getColor(), 1, 1);
                color.set(color.r, color.g, color.b, 1);
                TrajectoryItem trajectoryItem = new TrajectoryItem(pathGui, cameraHandler,
                        new ControlVectorList(trajectoryAutonomousStep.getControlVectors()),
                        trajectoryAutonomousStep.getRotations(),
                        trajectoryAutonomousStep.isReversed(), color, trajectoryAutonomousStep.isClosed(),
                        trajectoryAutonomousStep.getVelocityStart(), trajectoryAutonomousStep.getVelocityEnd(),
                        trajectoryAutonomousStep.getConstraints());
                guiItemList.add(trajectoryItem);
            } else if (autonomousStep instanceof ScriptAutonomousStep scriptAutonomousStep) {
                ScriptItem scriptItem = new ScriptItem(scriptAutonomousStep.getScript(),
                        scriptAutonomousStep.isClosed(), scriptAutonomousStep.isValid());
                guiItemList.add(scriptItem);
            }
        }
        for (AbstractGuiItem guiItem : pathGui.guiItems) {
            guiItem.dispose();
        }
        pathGui.guiItems = guiItemList;
        AutoBuilder.getConfig().setConfig(undoState.config());
        AutoBuilder.getInstance().settingsGui.updateValues();
    }

    public synchronized void somethingChanged() {
        somethingChanged = true;
    }

    public synchronized void forceCreateUndoState() {
        saveCurrentState(AutoBuilder.getInstance().pathGui);
    }

    public synchronized void clearUndoHistory() {
        undoHistory.clear();
        pointer = 0;
    }

    public void reloadState() {
        forceCreateUndoState();
        restoreState(undoHistory.get(pointer), AutoBuilder.getInstance().pathGui, AutoBuilder.getInstance().cameraHandler);
    }
}
