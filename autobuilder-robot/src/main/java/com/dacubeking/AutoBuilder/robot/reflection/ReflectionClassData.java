package com.dacubeking.AutoBuilder.robot.reflection;

import com.dacubeking.AutoBuilder.robot.robotinterface.AutonomousContainer;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map.Entry;

final class ReflectionClassData {
    @JsonProperty
    @NotNull
    private final String fullName;
    @JsonProperty private final String @NotNull [] fieldNames;
    @JsonProperty private final String @NotNull [] fieldTypes;
    @JsonProperty private final ReflectionMethodData @NotNull [] methods;

    @JsonProperty
    private final @NotNull String superClass;

    @JsonProperty private final String @NotNull [] interfaces;
    @JsonProperty private final int modifiers;
    @JsonProperty private final boolean isEnum;
    @JsonProperty private final boolean isCommand;
    @JsonProperty private final boolean isAnnotatedAsAccessible;
    @JsonProperty private final String alias;

    ReflectionClassData(@NotNull Object instance) {
        this(instance.getClass(), instance);
    }

    ReflectionClassData(@NotNull Class<?> clazz) {
        this(clazz, null);
    }


    ReflectionClassData(@NotNull Class<?> clazz, @Nullable Object instance) {
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

        @Nullable Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            this.superClass = superClass.getName();
        } else {
            this.superClass = "";
        }

        this.interfaces = Arrays.stream(clazz.getInterfaces()).map(Class::getName).toArray(String[]::new);
        this.modifiers = clazz.getModifiers();
        this.isEnum = clazz.isEnum();
        isCommand = isCommand(clazz);

        if (clazz.isAnonymousClass()) {
            this.isAnnotatedAsAccessible = AutonomousContainer.getInstance().getAccessibleInstances().entrySet().stream()
                    .anyMatch(entry -> entry.getValue() == instance);
        } else {
            this.isAnnotatedAsAccessible = AutonomousContainer.getInstance().getAccessibleInstances().entrySet().stream()
                    .anyMatch(entry -> entry.getValue().getClass().equals(clazz));
        }
        if (isAnnotatedAsAccessible) {
            this.alias = AutonomousContainer.getInstance().getAccessibleInstances().entrySet().stream()
                    .filter(entry -> entry.getValue() == instance)
                    .map(Entry::getKey)
                    .filter(s -> !s.equals(fullName))
                    .findFirst()
                    .orElse("");
        } else {
            this.alias = "";
        }

        if (this.alias.equals("") && clazz.isAnonymousClass()) {
            DriverStation.reportWarning(
                    "Anonymous class found without an alias. This should be impossible. Please report this: " + clazz.getName(),
                    false);
        }
    }

    private boolean isCommand(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        if (clazz.getName().equals(Command.class.getName())) {
            return true;
        }

        for (Class<?> anInterface : clazz.getInterfaces()) {
            if (isCommand(anInterface)) {
                return true;
            }
        }

        return isCommand(clazz.getSuperclass());
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
}
