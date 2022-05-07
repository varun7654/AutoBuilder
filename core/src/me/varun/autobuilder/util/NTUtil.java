package me.varun.autobuilder.util;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableValue;

import java.text.DecimalFormat;

public final class NTUtil {
    public static String getEntryAsString(NetworkTableEntry entry, DecimalFormat decimalFormat) {
        NetworkTableValue value = entry.getValue();
        switch (value.getType()) {
            case kBoolean:
                return entry.getBoolean(false) ? "true" : "false";
            case kDouble:
                return String.valueOf(entry.getDouble(0));
            case kString:
                return entry.getString("");
            default:
                return "invalid type";
        }
    }
}
