package com.dacubeking.autobuilder.gui.scripting.sendable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class SendableCommand {

    @JsonProperty("methodName") public final String methodName;

    @JsonProperty("args") public final String[] args;

    @JsonProperty("argTypes") public final String[] argTypes;

    @JsonProperty("reflection") public final boolean reflection;

    @JsonCreator
    public SendableCommand(@JsonProperty("methodName") String methodName,
                           @JsonProperty("args") String[] args,
                           @JsonProperty("argTypes") String[] argTypes,
                           @JsonProperty("reflection") boolean reflection) {
        this.methodName = methodName;
        this.args = args;
        this.argTypes = argTypes;
        this.reflection = reflection;
    }

    @Override
    public String toString() {
        return "SendableCommand{" +
                "methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", argTypes=" + Arrays.toString(argTypes) +
                ", reflection=" + reflection +
                '}';
    }
}
