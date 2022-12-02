package com.dacubeking.autobuilder.gui.scripting.reflection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
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
    private final @NotNull String superClass;

    @JsonProperty public final String @NotNull [] interfaces;
    @JsonProperty public final int modifiers;
    @JsonProperty public final boolean isEnum;
    @JsonProperty public final boolean isCommand;

    public ReflectionClassData(@NotNull Class<?> clazz) {
        this.fullName = clazz.getName();
        Method[] methods = clazz.getDeclaredMethods();
        this.methods = new ReflectionMethodData[methods.length];
        for (int i = 0; i < methods.length; i++) {
            this.methods[i] = new ReflectionMethodData(methods[i]);
        }

        this.fieldNames = new String[clazz.getDeclaredFields().length];
        this.fieldTypes = new String[clazz.getDeclaredFields().length];
        for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
            this.fieldNames[i] = clazz.getDeclaredFields()[i].getName();
            this.fieldTypes[i] = clazz.getDeclaredFields()[i].getType().getName();
        }

        this.superClass = clazz.getSuperclass().getName();
        interfaces = Arrays.stream(clazz.getInterfaces()).map(Class::getName).toArray(String[]::new);

        modifiers = clazz.getModifiers();
        this.isEnum = clazz.isEnum();

        isCommand = isCommand(clazz);
    }

    private boolean isCommand(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        if (clazz.getName().equals("edu.wpi.first.wpilibj2.command.Command")) {
            return true;
        }

        for (Class<?> anInterface : clazz.getInterfaces()) {
            if (isCommand(anInterface)) {
                return true;
            }
        }

        return isCommand(clazz.getSuperclass());
    }

    public void initMap() {
        for (ReflectionMethodData method : methods) {
            if (!methodMap.containsKey(method.methodName)) {
                methodMap.put(method.methodName, new ArrayList<>());
            }
            methodMap.get(method.methodName).add(method);
        }
    }

    @JsonCreator
    public ReflectionClassData(@JsonProperty("fullName") @NotNull String fullName,
                               @JsonProperty("fieldNames") String @NotNull [] fieldNames,
                               @JsonProperty("fieldTypes") String @NotNull [] fieldTypes,
                               @JsonProperty("methods") ReflectionMethodData @NotNull [] methods,
                               @JsonProperty("superClass") @NotNull String superClass,
                               @JsonProperty("interfaces") String @NotNull [] interfaces,
                               @JsonProperty("modifiers") int modifiers,
                               @JsonProperty("isEnum") boolean isEnum,
                               @JsonProperty("isCommand") boolean isCommand) {
        this.fullName = fullName;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.methods = methods;
        this.modifiers = modifiers;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.isEnum = isEnum;
        this.isCommand = isCommand;
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
                '}';
    }
}
