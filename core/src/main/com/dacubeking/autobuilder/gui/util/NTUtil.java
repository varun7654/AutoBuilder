package com.dacubeking.autobuilder.gui.util;

import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.Topic;

import java.text.DecimalFormat;

public final class NTUtil {
    public static String getEntryAsString(Topic entry, DecimalFormat decimalFormat) {
        NetworkTableValue value = entry.getGenericEntry().get();
        return switch (value.getType()) {
            case kBoolean -> value.getBoolean() ? "true" : "false";
            case kDouble -> String.valueOf(decimalFormat.format(value.getDouble()));
            case kString -> value.getString();
            case kUnassigned -> "Unassigned";
            default -> "Invalid type";
        };
    }
}
