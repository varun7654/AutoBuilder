package com.dacubeking.autobuilder.gui.scripting.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public record StringIndex(int index, String string) {

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

    public int length() {
        return string.length();
    }

    public int getEndIndex() {
        return index + string.length();
    }

    public StringIndex getSubStringIndex(int start, int end) {
        return new StringIndex(index + start, string.substring(start, end));
    }

    public StringIndex getSubStringIndex(int start) {
        return new StringIndex(index + start, string.substring(start));
    }


    @Override
    public String toString() {
        return string;
    }
}
