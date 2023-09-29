package com.dacubeking.AutoBuilder.robot.sender.hud;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

class HudElementSender {
    private static final @NotNull NetworkTableEntry hudElementsEntry = NetworkTableInstance.getDefault()
            .getEntry("autodata/hudElements");

    private static final ConcurrentHashMap<HudElement, String> hudElements = new ConcurrentHashMap<>();

    /**
     * Adds/updates a hud element on the GUI.
     *
     * @param element The hud element to add/update.
     */
    protected static void send(@NotNull HudElement element) {
        hudElements.put(element, element.toString());
        send();
    }

    /**
     * Sends all hud elements to the GUI.
     */
    protected static void send() {
        StringBuilder sb = new StringBuilder();
        for (String hudElement : hudElements.values()) {
            sb.append(hudElement).append(";");
        }
        hudElementsEntry.setString(sb.toString());
    }
}
