package com.dacubeking.autobuilder.gui.scripting.reflection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ReflectionClassDataList {
    @JsonProperty("reflectionClassData")
    public ArrayList<ReflectionClassData> reflectionClassData = new ArrayList<>();

    @JsonProperty("instanceLocations")
    public ArrayList<String> instanceLocations = new ArrayList<>();

    @JsonCreator
    protected ReflectionClassDataList() {
    }


    ReflectionClassDataList(@JsonProperty ArrayList<ReflectionClassData> reflectionClassData,
                            @JsonProperty ArrayList<String> instanceLocations) {
        this.reflectionClassData = reflectionClassData;
        this.instanceLocations = instanceLocations;
    }
}
