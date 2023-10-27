package com.dacubeking.AutoBuilder.robot.reflection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * Contains data about a method to be able to error checking on it.
 */
final class ReflectionMethodData {

    @JsonProperty
    @NotNull
    private final String methodName;

    @JsonProperty private final String @NotNull [] parameterTypes;

    @JsonProperty
    @NotNull
    private final String returnType;

    @JsonProperty private final int modifiers;

    ReflectionMethodData(@NotNull Method method) {
        this.methodName = method.getName();
        this.parameterTypes = getParameterTypes(method);
        this.returnType = method.getReturnType().getTypeName();
        this.modifiers = method.getModifiers();
    }

    @JsonIgnore
    private static String @NotNull [] getParameterTypes(@NotNull Method method) {
        String[] parameterTypes = new String[method.getParameterCount()];
        for (int i = 0; i < method.getParameterCount(); i++) {
            parameterTypes[i] = method.getParameterTypes()[i].getName();
        }
        return parameterTypes;
    }

    @Override
    public @NotNull String toString() {
        return "ReflectionMethodData{" +
                "methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", returnType='" + returnType + '\'' +
                ", modifiers=" + modifiers +
                '}';
    }
}
