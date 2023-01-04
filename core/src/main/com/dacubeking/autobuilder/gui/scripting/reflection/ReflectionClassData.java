package com.dacubeking.autobuilder.gui.scripting.reflection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public final class ReflectionClassData {

    public Hashtable<String, ArrayList<ReflectionMethodData>> methodMap = new Hashtable<>();
    @JsonProperty
    @NotNull
    public final String fullName;
    @JsonProperty public final String @NotNull [] fieldNames;
    @JsonProperty public final String @NotNull [] fieldTypes;
    @JsonProperty public final ReflectionMethodData @NotNull [] methods;

    @JsonProperty
    public final @NotNull String superClass;

    @JsonProperty public final String @NotNull [] interfaces;
    @JsonProperty public final int modifiers;
    @JsonProperty public final boolean isEnum;
    @JsonProperty public final boolean isCommand;
    @JsonProperty public final boolean isAnnotatedAsAccessible;
    @JsonProperty public final String alias;

    @JsonIgnoreProperties public final String name;

    @JsonCreator
    public ReflectionClassData(@JsonProperty("fullName") @NotNull String fullName,
                               @JsonProperty("fieldNames") String @NotNull [] fieldNames,
                               @JsonProperty("fieldTypes") String @NotNull [] fieldTypes,
                               @JsonProperty("methods") ReflectionMethodData @NotNull [] methods,
                               @JsonProperty("superClass") @NotNull String superClass,
                               @JsonProperty("interfaces") String @NotNull [] interfaces,
                               @JsonProperty("modifiers") int modifiers,
                               @JsonProperty("isEnum") boolean isEnum,
                               @JsonProperty("isCommand") boolean isCommand,
                               @JsonProperty("isAnnotatedAsAccessible") boolean isAnnotatedAsAccessible,
                               @JsonProperty("alias") String alias) {
        this.fullName = fullName;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.methods = methods;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.modifiers = modifiers;
        this.isEnum = isEnum;
        this.isCommand = isCommand;
        this.isAnnotatedAsAccessible = isAnnotatedAsAccessible;
        this.alias = alias;

        if (alias.isEmpty()) {
            this.name = fullName;
        } else {
            this.name = alias;
        }
    }


    @Override
    public String toString() {
        return "ReflectionClassData{" +
                "fullName='" + fullName + '\'' +
                ", fieldNames=" + Arrays.toString(fieldNames) +
                ", fieldTypes=" + Arrays.toString(fieldTypes) +
                ", methods=" + Arrays.toString(methods) +
                ", superClass='" + superClass + '\'' +
                ", interfaces=" + Arrays.toString(interfaces) +
                ", modifiers=" + modifiers +
                ", isEnum=" + isEnum +
                ", isCommand=" + isCommand +
                ", isAnnotatedAsAccessible=" + isAnnotatedAsAccessible +
                ", alias='" + alias + '\'' +
                '}';
    }

    public void initMap() {
        for (ReflectionMethodData method : methods) {
            if (!methodMap.containsKey(method.methodName)) {
                methodMap.put(method.methodName, new ArrayList<>());
            }
            methodMap.get(method.methodName).add(method);
        }
    }
}
