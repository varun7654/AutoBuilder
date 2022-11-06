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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileHandler {
    public static void handleFile(File file) {
        try {
            if (!file.getAbsolutePath().equals(AutoBuilder.getConfig().getAutoPath().getAbsolutePath())) {
                saveAuto(false);
            }

            System.out.println("Loading file: " + file.getPath());
            Autonomous autonomous = (Autonomous) Serializer.deserializeFromFile(file, Autonomous.class);
            if (file.getName().startsWith("NOTDEPLOYABLE")) {
                if (new File(AutoBuilder.USER_DIRECTORY).equals(file.getParentFile())) {
                    AutoBuilder.getConfig().setAuto(file.getName().substring(13));
                } else {
                    AutoBuilder.getConfig().setAuto(
                            file.getParentFile().getAbsolutePath() + "/" + file.getName().substring(13));
                }
            } else {
                if (new File(AutoBuilder.USER_DIRECTORY).equals(file.getParentFile())) {
                    AutoBuilder.getConfig().setAuto(file.getName());
                } else {
                    AutoBuilder.getConfig().setAuto(file.getAbsolutePath());
                }
            }

            AutoBuilder.getInstance().restoreState(autonomous, false);
            saveConfig();
            saveAuto(false);

            NotificationHandler.addNotification(new Notification(Color.GREEN, "Loaded Autonomous: " + file.getName(),
                    3000));
            Gdx.graphics.setTitle("Auto Builder - " + file.getAbsolutePath());
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

    public static void createNewAuto(File file) {
        saveAuto(false);
        if (!file.getName().endsWith(".json")) {
            file = new File(file.getAbsolutePath() + ".json");
        }
        Autonomous autonomous = new Autonomous(new ArrayList<>());
        if (file.getName().startsWith("NOTDEPLOYABLE")) {
            if (new File(AutoBuilder.USER_DIRECTORY).equals(file.getParentFile())) {
                AutoBuilder.getConfig().setAuto(file.getName().substring(13));
            } else {
                AutoBuilder.getConfig().setAuto(
                        file.getParentFile().getAbsolutePath() + "/" + file.getName().substring(13));
            }
        } else {
            if (new File(AutoBuilder.USER_DIRECTORY).equals(file.getParentFile())) {
                AutoBuilder.getConfig().setAuto(file.getName());
            } else {
                AutoBuilder.getConfig().setAuto(file.getAbsolutePath());
            }
        }
        AutoBuilder.getInstance().restoreState(autonomous);
        saveConfig();
        saveAuto(false);
        Gdx.graphics.setTitle("Auto Builder - " + file.getAbsolutePath());
        NotificationHandler.addNotification(new Notification(Color.GREEN, "Created Autonomous: " + file.getName(),
                3000));
    }

    public static void reloadAuto() {
        System.out.println("Reloading autonomous");
        if (loadAuto()) {
            NotificationHandler.addNotification(new Notification(Color.GREEN, "Reloaded Autonomous", 3000));
        }
    }

    public static boolean loadAuto() {
        File pathFile = AutoBuilder.getConfig().getAutoPath();
        try {
            Autonomous autonomous = Serializer.deserializeAutoFromFile(pathFile);
            AutoBuilder.getInstance().restoreState(autonomous, false);
            Gdx.graphics.setTitle("Auto Builder - " + pathFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            NotificationHandler.addNotification(
                    new Notification(Color.RED, "Failed to load autonomous file: " + pathFile.getName(), 3000));
            return false;
        }
        return true;
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

    private static final Object autoSaveNotificationObject = new Object();

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
                        break main;
                    }
                }

                synchronized (autonomousToSaveLock) {
                    requestSave = false;
                }
                try {
                    Autonomous autonomous = GuiSerializer.serializeAutonomous(AutoBuilder.getInstance().pathGui.guiItems, true);
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
        saveThread.start();
    }

    public static void saveAuto(boolean async) {
        if (async) {
            synchronized (autonomousToSaveLock) {
                requestSave = true;
                autonomousToSaveLock.notifyAll();
            }
        } else {
            synchronized (saveLock) {
                Autonomous autonomous = GuiSerializer.serializeAutonomous(AutoBuilder.getInstance().pathGui.guiItems, async);
                saveAuto(autonomous);
                saveConfig();
            }
        }
    }


    private static boolean saveAuto(Autonomous autonomous) {
        File autoFile;
        autoFile = new File(
                AutoBuilder.getConfig().getAutoPath().getParentFile().getAbsolutePath() + "/" +
                        (autonomous.deployable ? "" : "NOTDEPLOYABLE") + new File(
                        AutoBuilder.getConfig().getSelectedAuto()).getName());
        autoFile.getParentFile().mkdirs();

        try {
            Serializer.serializeToFile(autonomous, autoFile, false);

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

    private static boolean saveConfig() {
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

    public static void reloadConfig() {
        try {
            Config config = (Config) Serializer.deserializeFromFile(new File(AutoBuilder.USER_DIRECTORY + "/config.json"),
                    Config.class);
            String auto = AutoBuilder.getConfig().getSelectedAuto();
            AutoBuilder.getConfig().setConfig(config);
            if (!auto.equals(config.getSelectedAuto())) {
                AutoBuilder.getConfig().setAuto(auto);
            }
            loadAuto();
            saveConfig();
            saveAuto(false);
            NotificationHandler.addNotification(new Notification(Color.GREEN, "Loaded Config", 3000));
        } catch (IOException e) {
            e.printStackTrace();
            NotificationHandler.addNotification(new Notification(Color.RED, "Failed to reload config file", 3000));
        }
    }
}
