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


    private boolean saveWanted = false;
    private final Object saveWantedLock = new Object();

    private UndoHandler() {

    }

    public static UndoHandler getInstance() {
        return undoHandler;
    }

    public synchronized void update(PathGui pathGui, @NotNull CameraHandler cameraHandler) {
        if (somethingChanged && System.currentTimeMillis() - lastUndoSaveTime > UNDO_SAVE_INTERVAL) {
            saveCurrentState(pathGui);
        } else if (!somethingChanged) {
            lastUndoSaveTime = System.currentTimeMillis();
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
                    //System.out.println("redoing to: " + pointer);
                } else {
                    pointer = 0;
                }
            } else {
                //Undoing
                //System.out.println(undoHistory);
                pointer++;
                if (pointer < undoHistory.size()) {
                    restoreState(undoHistory.get(pointer), pathGui, cameraHandler);
                    //System.out.println("undoing to: " + pointer);
                } else {
                    pointer = undoHistory.size() - 1;
                }
            }

            FileHandler.saveAuto(true);
            synchronized (saveWantedLock) {
                saveWanted = false;
            }
        }
        boolean saveWanted = false;
        synchronized (saveWantedLock) {
            saveWanted = this.saveWanted;
        }
        if (saveWanted) {
            saveCurrentState(pathGui);
            synchronized (saveWantedLock) {
                this.saveWanted = false;
            }
        }
    }

    /**
     * @return The current state of the application as an UndoState
     */
    private @NotNull UndoState getCurrentState(@NotNull PathGui pathGui) {
        Autonomous newAutonomousState = GuiSerializer.serializeAutonomousForUndoHistory(pathGui.guiItems);
        return new UndoState(newAutonomousState, new Config(AutoBuilder.getConfig()));
    }

    private synchronized void saveCurrentState(PathGui pathGui) {
        while (pointer > 0) {
            undoHistory.remove(0);
            pointer--;
        }

        undoHistory.add(0, getCurrentState(pathGui));
        if (undoHistory.size() > MAX_UNDO_HISTORY) {
            undoHistory.remove(undoHistory.size() - 1);
        }
        // System.out.println("adding: " + newState);
        somethingChanged = false;
        lastUndoSaveTime = System.currentTimeMillis();
        FileHandler.saveAuto(true);
    }

    public synchronized void restoreState(@NotNull UndoState undoState, PathGui pathGui,
                                          @NotNull CameraHandler cameraHandler) {
        AutoBuilder.getConfig().setConfig(undoState.config());
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

    /**
     * Reloads the state of the Config and PathGui from the current state. Useful to have the app recalculate changes that may
     * have been made
     */
    public synchronized void reloadState() {
        PathGui pathGui = AutoBuilder.getInstance().pathGui;
        restoreState(getCurrentState(pathGui), pathGui, AutoBuilder.getInstance().cameraHandler);
    }

    /**
     * Reloads the autonomous. Used when paths need to be recalculated.
     */
    public synchronized void reloadPaths() {
        Autonomous newAutonomousState = GuiSerializer.serializeAutonomousForUndoHistory(
                AutoBuilder.getInstance().pathGui.guiItems);

        for (AbstractGuiItem guiItem : AutoBuilder.getInstance().pathGui.guiItems) {
            guiItem.dispose();
        }
        AutoBuilder.getInstance().pathGui.guiItems = new ArrayList<>();
        for (AbstractAutonomousStep autonomousStep : newAutonomousState.getAutonomousSteps()) {
            if (autonomousStep instanceof TrajectoryAutonomousStep trajectoryAutonomousStep) {
                Color color = new Color().fromHsv(trajectoryAutonomousStep.getColor(), 1, 1);
                color.set(color.r, color.g, color.b, 1);
                TrajectoryItem trajectoryItem = new TrajectoryItem(AutoBuilder.getInstance().pathGui,
                        AutoBuilder.getInstance().cameraHandler,
                        new ControlVectorList(trajectoryAutonomousStep.getControlVectors()),
                        trajectoryAutonomousStep.getRotations(),
                        trajectoryAutonomousStep.isReversed(), color, trajectoryAutonomousStep.isClosed(),
                        trajectoryAutonomousStep.getVelocityStart(), trajectoryAutonomousStep.getVelocityEnd(),
                        trajectoryAutonomousStep.getConstraints());
                AutoBuilder.getInstance().pathGui.guiItems.add(trajectoryItem);
            } else if (autonomousStep instanceof ScriptAutonomousStep scriptAutonomousStep) {
                ScriptItem scriptItem = new ScriptItem(scriptAutonomousStep.getScript(),
                        scriptAutonomousStep.isClosed(), scriptAutonomousStep.isValid());
                AutoBuilder.getInstance().pathGui.guiItems.add(scriptItem);
            }
        }
    }

    public synchronized void flushChanges() {
        lastUndoSaveTime = -1000000;
    }

    public void triggerSave() {
        synchronized (saveWantedLock) {
            saveWanted = true;
        }
    }
}
