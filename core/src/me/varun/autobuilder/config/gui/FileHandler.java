package me.varun.autobuilder.config.gui;

import com.badlogic.gdx.graphics.Color;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.config.Config;
import me.varun.autobuilder.gui.notification.Notification;
import me.varun.autobuilder.gui.notification.NotificationHandler;
import me.varun.autobuilder.net.Serializer;
import me.varun.autobuilder.serialization.path.Autonomous;
import me.varun.autobuilder.serialization.path.GuiSerializer;

import java.io.File;
import java.io.IOException;

import static me.varun.autobuilder.AutoBuilder.USER_DIRECTORY;
import static me.varun.autobuilder.pathing.PathRenderer.config;

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
                    if (new File(USER_DIRECTORY).equals(file.getParentFile())) {
                        AutoBuilder.getConfig().setAuto(file.getName().substring(13));
                    } else {
                        AutoBuilder.getConfig().setAuto(
                                file.getParentFile().getAbsolutePath() + "/" + file.getName().substring(13));
                    }
                } else {
                    if (new File(USER_DIRECTORY).equals(file.getParentFile())) {
                        AutoBuilder.getConfig().setAuto(file.getName());
                    } else {
                        AutoBuilder.getConfig().setAuto(file.getAbsolutePath());
                    }
                }

                AutoBuilder.getInstance().restoreState(autonomous);
                saveConfig();

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
        File configFile = new File(USER_DIRECTORY + "/config.json");
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
        File autoFile = new File(
                config.getAutoPath().getParentFile().getAbsolutePath() + "/" +
                        (autonomous.deployable ? "" : "NOTDEPLOYABLE") + new File(config.getSelectedAuto()).getName());
        autoFile.getParentFile().mkdirs();


        try {
            Serializer.serializeToFile(autonomous, autoFile);

            if (autonomous.deployable) {
                File fileToDelete = new File(USER_DIRECTORY + "/NOTDEPLOYABLE" + config.getSelectedAuto());
                fileToDelete.delete();
            } else {
                File fileToDelete = new File(USER_DIRECTORY + "/" + config.getSelectedAuto());
                fileToDelete.delete();
            }
            supressNextAutoReload = true;
            lastSaveTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        File configFile = new File(USER_DIRECTORY + "/config.json");
        File shooterConfig = new File(USER_DIRECTORY + "/" + config.getSelectedShooterConfig());

        configFile.getParentFile().mkdirs();
        shooterConfig.getParentFile().mkdirs();
        System.out.println("Saving config");
        try {
            configFile.createNewFile();
            Serializer.serializeToFile(config, configFile);

            shooterConfig.createNewFile();
            Serializer.serializeToFile(AutoBuilder.getInstance().shooterGui.getShooterConfig(), shooterConfig);
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
                Config config = (Config) Serializer.deserializeFromFile(new File(USER_DIRECTORY + "/config.json"), Config.class);
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
