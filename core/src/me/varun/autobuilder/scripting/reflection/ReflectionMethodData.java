package me.varun.autobuilder.scripting.reflection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Method;
import java.util.Arrays;


//Contains data about a method to be able  to error checking on it.
public final class ReflectionMethodData {

    public final String methodName;
    public final String[] parameterTypes;
    public final String returnType;
    public final int modifiers;

    public ReflectionMethodData(Method method) {
        this.methodName = method.getName();
        this.parameterTypes = ReflectionUtils.getParameterTypes(method);
        this.returnType = method.getReturnType().getTypeName();
        this.modifiers = method.getModifiers();
    }

    @JsonCreator
    public ReflectionMethodData(@JsonProperty("methodName") String methodName,
                                @JsonProperty("parameterTypes") String[] parameterTypes,
                                @JsonProperty("returnType") String returnType,
                                @JsonProperty("modifiers") int modifiers) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.modifiers = modifiers;
    }

    @Override
    public String toString() {
        return "ReflectionMethodData{" +
                "methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", returnType='" + returnType + '\'' +
                '}';
    }
}
