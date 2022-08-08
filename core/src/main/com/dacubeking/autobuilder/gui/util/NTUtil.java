package com.dacubeking.autobuilder.gui.util;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableValue;

import java.text.DecimalFormat;

public final class NTUtil {
    public static String getEntryAsString(NetworkTableEntry entry, DecimalFormat decimalFormat) {
        NetworkTableValue value = entry.getValue();
        return switch (value.getType()) {
            case kBoolean -> entry.getBoolean(false) ? "true" : "false";
            case kDouble -> String.valueOf(decimalFormat.format(entry.getDouble(0)));
            case kString -> entry.getString("");
            case kUnassigned -> "Unassigned";
            default -> "Invalid type";
        };
    }
}
