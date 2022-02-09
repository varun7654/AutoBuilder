package me.varun.autobuilder.config.gui;

import com.badlogic.gdx.graphics.Color;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.config.Config;
import me.varun.autobuilder.gui.notification.Notification;
import me.varun.autobuilder.gui.notification.NotificationHandler;
import me.varun.autobuilder.net.Serializer;
import me.varun.autobuilder.serialization.path.Autonomous;

import java.io.File;
import java.io.IOException;

public class FileHandler {
    public static void handleFile(File file) {
        if (file.getName().equalsIgnoreCase("config.json")) {
            NotificationHandler.addNotification(new Notification(Color.RED, "Currently not supported", 3000));
            try {
                Config config = (Config) Serializer.deserializeFromFile(file, Config.class);
                String auto = config.getSelectedAuto();
                AutoBuilder.getConfig().setConfig(config);
                AutoBuilder.getConfig().setAuto(auto);
            } catch (IOException e) {
                NotificationHandler.addNotification(new Notification(Color.RED, "Failed to load config file: " + file.getName(),
                        3000));
            }
        }
        try {
            AutoBuilder.getInstance().save();

            System.out.println("Loading file: " + file.getPath());
            Autonomous autonomous = (Autonomous) Serializer.deserializeFromFile(file, Autonomous.class);
            if (file.getName().startsWith("NOTDEPLOYABLE")) {
                if (new File(AutoBuilder.USER_DIRECTORY).equals(file.getParentFile())) {
                    AutoBuilder.getConfig().setAuto(file.getName().substring(13));
                } else {
                    AutoBuilder.getConfig().setAuto(file.getParentFile().getAbsolutePath() + "/" + file.getName().substring(13));
                }
            } else {
                if (new File(AutoBuilder.USER_DIRECTORY).equals(file.getParentFile())) {
                    AutoBuilder.getConfig().setAuto(file.getName());
                } else {
                    AutoBuilder.getConfig().setAuto(file.getAbsolutePath());
                }
            }

            AutoBuilder.getInstance().restoreState(autonomous);

            System.out.println("Loaded file: " + autonomous);
        } catch (IOException e) {
            NotificationHandler.addNotification(new Notification(Color.RED, "Failed to load autonomous file: " + file.getName(),
                    3000));
        }
    }
}
