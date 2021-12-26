package me.varun.autobuilder.scripting.reflection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public final class ReflectionClassData {
    public final String fullName;
    public final String[] fieldNames;
    public final String[] fieldTypes;
    public final ReflectionMethodData[] methods;
    public final int modifiers;

    public Hashtable<String, ArrayList<ReflectionMethodData>> methodMap = new Hashtable<>();

    public ReflectionClassData(Class<?> clazz) {
        this.fullName = clazz.getName();
        Method[] methods = clazz.getMethods();
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

        modifiers = clazz.getModifiers();
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
    public ReflectionClassData(@JsonProperty("fullName") String fullName,
                               @JsonProperty("fieldNames") String[] fieldNames,
                               @JsonProperty("fieldTypes") String[] fieldTypes,
                               @JsonProperty("methods") ReflectionMethodData[] methods,
                               @JsonProperty("modifiers") int modifiers) {
        this.fullName = fullName;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.methods = methods;
        this.modifiers = modifiers;
    }

    @Override
    public String toString() {
        return "ReflectionClassData{" +
                "fullName='" + fullName + '\'' +
                ", fieldNames=" + Arrays.toString(fieldNames) +
                ", fieldTypes=" + Arrays.toString(fieldTypes) +
                ", methods=" + Arrays.toString(methods) +
                '}';
    }
}
