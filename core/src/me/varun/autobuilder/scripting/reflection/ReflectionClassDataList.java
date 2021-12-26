package me.varun.autobuilder.scripting.reflection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReflectionClassDataList {
    @JsonProperty
    public ReflectionClassData[] reflectionClassData;

    @JsonCreator
    public ReflectionClassDataList(@JsonProperty ReflectionClassData[] reflectionClassData) {
        this.reflectionClassData = reflectionClassData;
    }
}