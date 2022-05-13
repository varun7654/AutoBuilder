package com.dacubeking.autobuilder.gui.config.gui;

import com.badlogic.gdx.graphics.Color;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.config.Config;
import com.dacubeking.autobuilder.gui.gui.notification.Notification;
import com.dacubeking.autobuilder.gui.gui.notification.NotificationHandler;
import com.dacubeking.autobuilder.gui.net.Serializer;
import com.dacubeking.autobuilder.gui.pathing.PathRenderer;
import com.dacubeking.autobuilder.gui.serialization.path.Autonomous;
import com.dacubeking.autobuilder.gui.serialization.path.GuiSerializer;

import java.io.File;
import java.io.IOException;

public class FileHandler {
    private static boolean supressNextAutoReload = true;

    public static void handleFile(File file) {
        if (file.getName().equalsIgnoreCase("config.json")) {
            try {
                Config config = (Config) Serializer.deserializeFromFile(file, Config.class);
                String auto = AutoBuilder.getConfig().getSelectedAuto();
                AutoBuilder.getConfig().setConfig(config);
                AutoBuilder.getConfig().setAuto(auto);
                save();
                NotificationHandler.addNotification(new Notification(Color.GREEN, "Loaded Config File: " + file.getAbsolutePath(),
                        3000));
            } catch (IOException e) {
                e.printStackTrace();
                NotificationHandler.addNotification(new Notification(Color.RED, "Failed to load config file: " + file.getName(),
                        3000));
            }
        } else {
            try {
                save();

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

                AutoBuilder.getInstance().restoreState(autonomous);
                save();

                NotificationHandler.addNotification(new Notification(Color.GREEN, "Loaded Autonomous: " + file.getName(),
                        3000));
            } catch (IOException e) {
                e.printStackTrace();
                NotificationHandler.addNotification(
                        new Notification(Color.RED, "Failed to load autonomous file: " + file.getName(),
                                3000));
            }
        }
    }

    public static void reloadAuto() {
        if (supressNextAutoReload) {
            supressNextAutoReload = false;
        } else {
            System.out.println("Reloading autonomous");
            if (loadAuto()) {
                NotificationHandler.addNotification(new Notification(Color.GREEN, "Reloaded Autonomous", 3000));
            }
        }
    }

    public static boolean loadAuto() {
        File pathFile = AutoBuilder.getConfig().getAutoPath();
        try {
            Autonomous autonomous = Serializer.deserializeAutoFromFile(pathFile);
            AutoBuilder.getInstance().restoreState(autonomous, false);
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
        }
    }

    public static long lastSaveTime = -1;

    public static void save() {
        saveConfig();
        saveAuto();
        supressNextAutoReload = true;
    }

    public static void saveAuto() {
        Autonomous autonomous = GuiSerializer.serializeAutonomous(AutoBuilder.getInstance().pathGui.guiItems);
        File autoFile;
        autoFile = new File(
                PathRenderer.config.getAutoPath().getParentFile().getAbsolutePath() + "/" +
                        (autonomous.deployable ? "" : "NOTDEPLOYABLE") + new File(
                        PathRenderer.config.getSelectedAuto()).getName());
        autoFile.getParentFile().mkdirs();

        try {
            Serializer.serializeToFile(autonomous, autoFile);

            if (autonomous.deployable) {
                File fileToDelete = new File(
                        PathRenderer.config.getAutoPath().getParentFile().getAbsolutePath() + "/NOTDEPLOYABLE" +
                                new File(PathRenderer.config.getSelectedAuto()).getName());
                fileToDelete.delete();
            } else {
                File fileToDelete = new File(PathRenderer.config.getSelectedAuto());
                fileToDelete.delete();
            }
            supressNextAutoReload = true;
            lastSaveTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        File configFile = new File(AutoBuilder.USER_DIRECTORY + "/config.json");
        File shooterConfig = PathRenderer.config.getShooterConfigPath();

        configFile.getParentFile().mkdirs();
        shooterConfig.getParentFile().mkdirs();
        try {
            configFile.createNewFile();
            Serializer.serializeToFile(PathRenderer.config, configFile);

            if (AutoBuilder.getInstance().shooterGui != null) {
                shooterConfig.createNewFile();
                Serializer.serializeToFile(AutoBuilder.getInstance().shooterGui.getShooterConfig(), shooterConfig);
            }
            supressNextConfigReload = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean supressNextConfigReload = false;

    public static void reloadConfig() {
        if (supressNextConfigReload) {
            System.out.println("Suppressed reloading config");
            supressNextConfigReload = false;
        } else {
            try {
                Config config = (Config) Serializer.deserializeFromFile(new File(AutoBuilder.USER_DIRECTORY + "/config.json"),
                        Config.class);
                String auto = AutoBuilder.getConfig().getSelectedAuto();
                AutoBuilder.getConfig().setConfig(config);
                if (!auto.equals(config.getSelectedAuto())) {
                    AutoBuilder.getConfig().setAuto(auto);
                    save();
                }
                loadAuto();
                saveAuto();
                NotificationHandler.addNotification(new Notification(Color.GREEN, "Loaded Config", 3000));
            } catch (IOException e) {
                e.printStackTrace();
                NotificationHandler.addNotification(new Notification(Color.RED, "Failed to reload config file", 3000));
            }
        }
    }
}
