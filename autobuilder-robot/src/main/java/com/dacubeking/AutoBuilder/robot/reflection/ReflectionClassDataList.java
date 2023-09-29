package com.dacubeking.AutoBuilder.robot.reflection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

class ReflectionClassDataList {
    @JsonProperty
    ArrayList<ReflectionClassData> reflectionClassData = new ArrayList<>();


    protected ReflectionClassDataList() {
    }

    @JsonCreator
    ReflectionClassDataList(ArrayList<ReflectionClassData> reflectionClassData) {
        this.reflectionClassData = reflectionClassData;
    }
}
