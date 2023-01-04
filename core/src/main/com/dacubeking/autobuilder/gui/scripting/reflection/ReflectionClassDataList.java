package com.dacubeking.autobuilder.gui.scripting.reflection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ReflectionClassDataList {
    @JsonProperty public
    ArrayList<ReflectionClassData> reflectionClassData = new ArrayList<>();


    protected ReflectionClassDataList() {
    }

    @JsonCreator
    ReflectionClassDataList(ArrayList<ReflectionClassData> reflectionClassData) {
        this.reflectionClassData = reflectionClassData;
    }
}
