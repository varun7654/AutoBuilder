package com.dacubeking.autobuilder.gui.config.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.config.Config;
import com.dacubeking.autobuilder.gui.gui.notification.Notification;
import com.dacubeking.autobuilder.gui.gui.notification.NotificationHandler;
import com.dacubeking.autobuilder.gui.net.Serializer;
import com.dacubeking.autobuilder.gui.serialization.path.Autonomous;
import com.dacubeking.autobuilder.gui.serialization.path.GuiSerializer;
import com.dacubeking.autobuilder.gui.undo.UndoHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileHandler {
    public static void handleFile(@NotNull File file) {
        synchronized (saveLock) {
            try {
                if (!file.getAbsolutePath().equals(AutoBuilder.getConfig().getAutoPath().getAbsolutePath())) {
                    // If the user requests to load the auto we already have loaded, don't save the current auto state to allow
                    // the user to perform a "reload from disk"
                    saveAuto(false);
                }

                System.out.println("Loading file: " + file.getPath());
                Autonomous autonomous = (Autonomous) Serializer.deserializeFromFile(file, Autonomous.class);

                AutoBuilder.getConfig().setAuto(getSavableFileName(file));
                AutoBuilder.getInstance().restoreState(autonomous, false);
                String autoFilePath = AutoBuilder.getConfig().getSelectedAuto();

                saveConfig();
                saveAuto(false);

                if (autoFilePath.endsWith(".json")) {
                    // Migrate the json to the msgpack format
                    new File(autoFilePath).delete();
                    AutoBuilder.getConfig().setAuto(autoFilePath.substring(0, autoFilePath.length() - 5) + ".auto");

                    saveConfig();
                    saveAuto(false);

                    NotificationHandler.addNotification(new Notification(Color.GREEN,
                            "Migrated & Loaded Autonomous: " + AutoBuilder.getConfig().getAutoPath().getName(),
                            3000));
                    System.out.println("Migrated auto to: " + AutoBuilder.getConfig().getAutoPath().getAbsolutePath());
                } else {
                    NotificationHandler.addNotification(new Notification(Color.GREEN,
                            "Loaded Autonomous: " + AutoBuilder.getConfig().getAutoPath().getName(),
                            3000));
                }

                Gdx.graphics.setTitle("Auto Builder - " + autoFilePath);
            } catch (IOException e) {
                //Maybe it's a config file?
                try {
                    Config config = (Config) Serializer.deserializeFromFile(file, Config.class);
                    String auto = AutoBuilder.getConfig().getSelectedAuto();
                    AutoBuilder.getConfig().setConfig(config);
                    AutoBuilder.getConfig().setAuto(auto);
                    saveConfig();
                    NotificationHandler.addNotification(
                            new Notification(Color.GREEN, "Loaded Config File: " + file.getAbsolutePath(),
                                    3000));
                    UndoHandler.getInstance().forceCreateUndoState();
                } catch (IOException exception) {
                    exception.printStackTrace();
                    NotificationHandler.addNotification(
                            new Notification(Color.RED, "Failed to load config/Autonomous file: " + file.getName(),
                                    3000));
                    e.printStackTrace();
                }
            }
        }
    }

    public static void createNewAuto(@NotNull File file) {
        saveAuto(false);
        if (!file.getName().endsWith(".auto")) {
            file = new File(file.getAbsolutePath() + ".auto");
        }

        Autonomous autonomous = new Autonomous(new ArrayList<>());
        AutoBuilder.getConfig().setAuto(getSavableFileName(file));
        AutoBuilder.getInstance().restoreState(autonomous);
        saveConfig();
        saveAuto(false);
        Gdx.graphics.setTitle("Auto Builder - " + file.getAbsolutePath());
        NotificationHandler.addNotification(new Notification(Color.GREEN, "Created Autonomous: " + file.getName(),
                3000));
    }

    public static void loadConfig() {
        File configFile = new File(AutoBuilder.USER_DIRECTORY + "/config.json");
        configFile.getParentFile().mkdirs();
        try {
            AutoBuilder.getConfig().setConfig((Config) Serializer.deserializeFromFile(configFile, Config.class));
        } catch (IOException e) {
            e.printStackTrace();
            NotificationHandler.addNotification(
                    new Notification(Color.RED, "Failed to load config file: " + configFile.getName(), 10000));
        }
    }

    public volatile static long lastSaveTime = -1;
    public volatile static boolean saving = false;
    public volatile static boolean savingError = false;

    private static final Object autonomousToSaveLock = new Object();

    private static final Object saveLock = new Object();
    static final Thread saveThread;
    private static volatile boolean requestSave = false;

    @Nullable static Autonomous autonomousToSave = null;

    static {
        saveThread = new Thread(() -> {
            main:
            while (true) {
                while (!requestSave) {
                    try {
                        synchronized (autonomousToSaveLock) {
                            autonomousToSaveLock.wait();
                        }
                    } catch (InterruptedException ignored) {
                        System.out.println("Save thread interrupted");
                        break main;
                    }
                }

                synchronized (saveLock) {
                    requestSave = false;
                }
                try {
                    Autonomous autonomous = autonomousToSave;
                    if (autonomous == null) {
                        continue;
                    }
                    synchronized (saveLock) {
                        saving = true;
                        boolean error = !saveAuto(autonomous);
                        error = !saveConfig() || error;
                        savingError = error;
                        saving = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    synchronized (saveLock) {
                        saving = false;
                        savingError = true;
                    }
                }


                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
            System.out.println("Save thread exited");
        });
    }

    public static void startAutoSaveThread() {
        saveThread.start();
    }


    /**
     * This method must be called from the main thread
     *
     * @param async whether to save asynchronously
     */
    public static void saveAuto(boolean async) {
        Autonomous autonomous = GuiSerializer.serializeAutonomous(AutoBuilder.getInstance().pathGui.guiItems, async);

        synchronized (saveLock) {
            autonomousToSave = autonomous;
            if (async) {
                requestSave = true;
                synchronized (autonomousToSaveLock) {
                    autonomousToSaveLock.notify();
                }
            } else {
                requestSave = false;
                saveAuto(autonomous);
                saveConfig();
            }
        }
    }


    private static boolean saveAuto(@NotNull Autonomous autonomous) {
        synchronized (saveLock) {
            File autoFile;
            autoFile = new File(
                    AutoBuilder.getConfig().getAutoPath().getParentFile().getAbsolutePath() + "/" +
                            (autonomous.deployable ? "" : "NOTDEPLOYABLE") + new File(
                            AutoBuilder.getConfig().getSelectedAuto()).getName());
            autoFile.getParentFile().mkdirs();

            try {
                Serializer.serializeToFile(autonomous, autoFile, AutoBuilder.getConfig().getSelectedAuto().endsWith(".json"));

                if (autonomous.deployable) {
                    File fileToDelete = new File(
                            AutoBuilder.getConfig().getAutoPath().getParentFile().getAbsolutePath() + "/NOTDEPLOYABLE" +
                                    new File(AutoBuilder.getConfig().getSelectedAuto()).getName());
                    fileToDelete.delete();
                } else {
                    File fileToDelete = new File(AutoBuilder.getConfig().getSelectedAuto());
                    fileToDelete.delete();
                }
                lastSaveTime = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private static boolean saveConfig() {
        synchronized (saveLock) {
            File configFile = new File(AutoBuilder.USER_DIRECTORY + "/config.json");
            File shooterConfig = AutoBuilder.getConfig().getShooterConfigPath();

            configFile.getParentFile().mkdirs();
            shooterConfig.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
                Serializer.serializeToFile(AutoBuilder.getConfig(), configFile, true);

                if (AutoBuilder.getInstance().shooterGui != null) {
                    shooterConfig.createNewFile();
                    Serializer.serializeToFile(AutoBuilder.getInstance().shooterGui.getShooterConfig(), shooterConfig, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @NotNull
    private static String getSavableFileName(@NotNull File file) {
        String fileName;
        if (file.getName().startsWith("NOTDEPLOYABLE")) {
            // Skip the NOTDEPLOYABLE part of the name. (It will be automatically added later if needed)
            fileName = file.getName().substring(13);
        } else {
            fileName = file.getName();
        }

        File parentOfSelectedFile = file.getParentFile();
        if (!(new File(AutoBuilder.USER_DIRECTORY).equals(parentOfSelectedFile))) {
            fileName = parentOfSelectedFile.getAbsolutePath() + "/" + fileName;
        } // else if the auto is inside the user directory we can instead a store it as a relative path
        return fileName;
    }
}
