package com.dacubeking.AutoBuilder.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class NetworkAuto extends GuiAuto {

    static final @NotNull NetworkTableInstance instance = NetworkTableInstance.getDefault();
    static final @NotNull NetworkTable table = instance.getTable("autodata");
    static final @NotNull NetworkTableEntry autoPath = table.getEntry("autoPath");

    /**
     * Deserializes an auto form a NT entry.
     */
    public NetworkAuto() throws IOException {
        super(autoPath.getString(null));
        loadAutonomous();
    }
}