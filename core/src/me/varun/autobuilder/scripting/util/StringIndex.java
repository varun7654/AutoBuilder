package me.varun.autobuilder.scripting.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class StringIndex {

    public final int index;
    public final String string;

    public StringIndex(int index, String string) {
        this.index = index;
        this.string = string;
    }

    public static @NotNull StringIndex[] splitWithIndex(@NotNull String string, Pattern pattern, int offset) {
        String[] parts = pattern.split(string);
        StringIndex[] stringIndices = new StringIndex[parts.length];
        int pos = offset;
        for (int i = 0; i < parts.length; i++) {
            String tempString = string.substring(pos - offset);
            int index = tempString.indexOf(parts[i]);

            stringIndices[i] = new StringIndex(index + pos, parts[i]);

            pos += index + parts[i].length();
        }
        return stringIndices;
    }

    @Override
    public String toString() {
        return string;
    }

}
