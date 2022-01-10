package me.varun.autobuilder.scripting;

import com.badlogic.gdx.graphics.Color;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextBlock;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.scripting.reflection.ReflectionClassData;
import me.varun.autobuilder.scripting.reflection.ReflectionMethodData;
import me.varun.autobuilder.scripting.sendable.SendableCommand;
import me.varun.autobuilder.scripting.util.LintingPos;
import me.varun.autobuilder.scripting.util.StringIndex;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.varun.autobuilder.AutoBuilder.USER_DIRECTORY;
import static me.varun.autobuilder.AutoBuilder.getConfig;
import static me.varun.autobuilder.scripting.util.StringIndex.splitWithIndex;

public class RobotCodeData {

    public static Map<String, Function<String, Boolean>> inferableTypesVerification;

    
    static {
        inferableTypesVerification = new Hashtable<>();
        inferableTypesVerification.put(int.class.getName(), s -> s.matches("[0-9]+"));
        inferableTypesVerification.put(double.class.getName(), s -> s.matches("[0-9]+\\.?[0-9]*"));
        inferableTypesVerification.put(float.class.getName(), s -> s.matches("[0-9]+\\.?[0-9]*"));
        inferableTypesVerification.put(long.class.getName(), s -> s.matches("[0-9]+"));
        inferableTypesVerification.put(short.class.getName(), s -> s.matches("[0-9]+"));
        inferableTypesVerification.put(byte.class.getName(), s -> s.matches("[0-9]+"));
        inferableTypesVerification.put(char.class.getName(), s -> s.matches("[a-zA-Z]+"));
        inferableTypesVerification.put(boolean.class.getName(), s -> s.matches("true|false"));
        inferableTypesVerification.put(String.class.getName(), s -> true);
        inferableTypesVerification.put(Integer.class.getName(), s -> s.matches("[0-9]+"));
        inferableTypesVerification.put(Double.class.getName(), s -> s.matches("[0-9]+\\.?[0-9]*"));
        inferableTypesVerification.put(Float.class.getName(), s -> s.matches("[0-9]+\\.?[0-9]*"));
        inferableTypesVerification.put(Long.class.getName(), s -> s.matches("[0-9]+"));
        inferableTypesVerification.put(Short.class.getName(), s -> s.matches("[0-9]+"));
        inferableTypesVerification.put(Byte.class.getName(), s -> s.matches("[0-9]+"));
        inferableTypesVerification.put(Character.class.getName(), s -> s.matches("[a-zA-Z]+"));
        inferableTypesVerification.put(Boolean.class.getName(), s -> s.matches("true|false"));
    }


    public static List<ReflectionClassData> robotClasses = new ArrayList<>();

    public static Hashtable<String, ReflectionClassData> robotClassesMap = new Hashtable<>();
    public static Hashtable<String, ReflectionClassData> robotFullNameClassesMap = new Hashtable<>();

    public static void initData() {
        try {
            File file = new File(USER_DIRECTORY + "/" + getConfig().getRobotCodeDataFile());
            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            fileInputStream.close();
            String string = new String(bytes);

            robotClasses = new ObjectMapper().readValue(string, new TypeReference<ArrayList<ReflectionClassData>>() {
            });
            
            for (ReflectionClassData robotClass : robotClasses) {
                String[] splitFullName = robotClass.fullName.split("\\.");
                robotClassesMap.put(splitFullName[splitFullName.length - 1], robotClass);
                robotFullNameClassesMap.put(robotClass.fullName, robotClass);
                robotClass.initMap();
            }

            System.out.println("Loaded " + robotClassesMap.size() + " robot classes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Pattern periodPattern = Pattern.compile("\\.");
    private static final TextComponent executingUsingReflectionComponent =
            new TextComponent("Executing this method using reflection\n").setBold(true);

    public static boolean validateMethod(StringIndex classMethod, StringIndex[] args, List<LintingPos> lintingPositions, ArrayList<SendableCommand> sendableCommands) {
        StringIndex[] classAndMethod = splitWithIndex(classMethod.string, periodPattern, classMethod.index);

        boolean error = false;
        List<TextComponent> classTextComponents = new ArrayList<>();
        classTextComponents.add(executingUsingReflectionComponent);

        // Even if there are errors still try and find the class
        ReflectionClassData reflectionClassData = null;
        boolean singleton = false;
        if (classAndMethod.length > 0) {
            reflectionClassData = robotClassesMap.get(classAndMethod[0].string);
            if (reflectionClassData == null) {
                classTextComponents.add(new TextComponent("\nCould not find class: "));
                classTextComponents.add(new TextComponent(classAndMethod[0].string).setItalic(true));
                error = true;
            } else {
                if (reflectionClassData.methodMap.containsKey("getInstance")) {
                    ReflectionMethodData singletonMethod = reflectionClassData.methodMap.get("getInstance").stream()
                            .filter(methodData -> methodData.parameterTypes.length == 0).findFirst().orElse(null);
                    singleton = isStatic(singletonMethod) && singletonMethod.returnType.equals(reflectionClassData.fullName);
                }

                classTextComponents.add(new TextComponent("Using " + (singleton ? "singleton " : "") + "class "));
                classTextComponents.add(new TextComponent(reflectionClassData.fullName).setItalic(true));
            }
        }

        if (classAndMethod.length < 2) {
            classTextComponents.add(new TextComponent("\n\nExpected a method after class"));
            error = true;
        } else if (classAndMethod.length > 2) {
            classTextComponents.add(new TextComponent("\n\nFully qualified classes are not supported yet!"));
            error = true;
        }


        lintingPositions.add(new LintingPos(classMethod.index, error ? Color.RED : Color.CLEAR,
                new TextBlock(Fonts.ROBOTO, 14, 300, classTextComponents.toArray(new TextComponent[0]))));

        if (error) {
            return false;
        }

        if (reflectionClassData.methodMap.containsKey(classAndMethod[1].string)) {

            List<ReflectionMethodData> callableMethods = reflectionClassData.methodMap.get(classAndMethod[1].string);
            if (!singleton) {
                callableMethods = callableMethods.stream().filter(RobotCodeData::isStatic).collect(Collectors.toList());
            }

            if (callableMethods.size() == 0) {
                lintingPositions.add(new LintingPos(classAndMethod[1].index, Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                        executingUsingReflectionComponent, new TextComponent("\nFound method(s) with the name "),
                        new TextComponent(classAndMethod[1].string).setItalic(true),
                        new TextComponent(" but none are static or have a singleton instance\n\n" +
                                "Possible Fixes:\n" +
                                "   Make the method static\n" +
                                "   Make the class a singleton"))));
                return false;
            }

            callableMethods = callableMethods.stream().filter(methodData -> Modifier.isPublic(methodData.modifiers))
                    .collect(Collectors.toList());

            if (callableMethods.size() == 0) {
                lintingPositions.add(new LintingPos(classAndMethod[1].index, Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                        executingUsingReflectionComponent, new TextComponent("\nFound method(s) with the name "),
                        new TextComponent(classAndMethod[1].string).setItalic(true),
                        new TextComponent(" but none are public\n\n" +
                                "Possible Fix:\n" +
                                "   Make the method public"))));
                return false;
            }

            List<ReflectionMethodData> possibleMethodsBySize = reflectionClassData.methodMap.get(classAndMethod[1].string)
                    .stream()
                    .filter(methodData -> methodData.parameterTypes.length == args.length).collect(Collectors.toList());

            if (possibleMethodsBySize.isEmpty()) {
                lintingPositions.add(new LintingPos(classAndMethod[1].index, Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                        executingUsingReflectionComponent,
                        new TextComponent("Method "),
                        new TextComponent(classAndMethod[1].string).setItalic(true),
                        new TextComponent(" is not applicable for arguments n = "),
                        new TextComponent(Integer.toString(args.length)).setItalic(true),
                        new TextComponent("\n\nPossible arguments are:\n"),
                        new TextComponent(reflectionClassData.methodMap.get(classAndMethod[1].string).stream()
                                .map(methodData -> Arrays.stream(methodData.parameterTypes) // Map a method into a string of its argument types
                                        .map(parameterName -> { // Remove the package name from the argument name. Also add the enum fields names if applicable.
                                            String[] splitFullName = parameterName.split("\\.");
                                            if (inferableTypesVerification.containsKey(parameterName)) {
                                                return splitFullName[splitFullName.length - 1];
                                            } else { // Maybe it's an enum
                                                ReflectionClassData classData = robotFullNameClassesMap.get(parameterName);
                                                if (classData != null && classData.isEnum) {
                                                    splitFullName = splitFullName[splitFullName.length - 1].split("\\$");
                                                    String availableEnums = Arrays.stream(classData.fieldNames)
                                                            .filter(n -> !n.equals("$VALUES"))
                                                            .collect(Collectors.joining(", "));
                                                    return splitFullName[splitFullName.length - 1] + " (enum: " + availableEnums + ")";
                                                }
                                                return splitFullName[splitFullName.length - 1] + " (can't infer)";
                                            }
                                        }).collect(Collectors.joining(", ")))
                                .map(s -> s.equals("") ? "<no arguments>" : s)
                                .collect(Collectors.joining("\n\n"))) //Separate each method by a newline
                                .setItalic(true))));
                return false;
            }

            possibleMethodsBySize = possibleMethodsBySize.stream().filter(methodData -> {
                for (int i = 0; i < args.length; i++) {
                    String parameterType = methodData.parameterTypes[i];
                    if (inferableTypesVerification.containsKey(parameterType)) {
                        if (!inferableTypesVerification.get(parameterType).apply(args[i].string)) return false;
                    } else {
                        // Maybe it's an enum
                        if (robotFullNameClassesMap.containsKey(parameterType)) {
                            ReflectionClassData enumData = robotFullNameClassesMap.get(parameterType);
                            int finalI = i;
                            if (Arrays.stream(enumData.fieldNames).noneMatch(
                                    enumField -> !enumField.equals("$VALUES") && enumField.equals(args[finalI].string))) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
                return true;
            }).collect(Collectors.toList());

            if (possibleMethodsBySize.isEmpty()) {
                lintingPositions.add(new LintingPos(classAndMethod[1].index, Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                        executingUsingReflectionComponent,
                        new TextComponent("Method "),
                        new TextComponent(classAndMethod[1].string).setItalic(true),
                        new TextComponent(" is not applicable for arguments "),
                        new TextComponent(
                                Arrays.stream(args).map(arg -> arg.string).collect(Collectors.joining(", "))).setItalic(
                                true),
                        new TextComponent("\n\nPossible arguments are:\n"),
                        new TextComponent(reflectionClassData.methodMap.get(classAndMethod[1].string).stream()
                                .map(methodData -> Arrays.stream(methodData.parameterTypes)
                                        .map(parameterName -> {
                                            String[] splitFullName = parameterName.split("\\.");
                                            if (inferableTypesVerification.containsKey(parameterName)) {
                                                return splitFullName[splitFullName.length - 1];
                                            } else {
                                                // Maybe it's an enum
                                                ReflectionClassData classData = robotFullNameClassesMap.get(parameterName);
                                                if (classData != null && classData.isEnum) {
                                                    splitFullName = splitFullName[splitFullName.length - 1].split("\\$");
                                                    String availableEnums = Arrays.stream(classData.fieldNames)
                                                            .filter(n -> !n.equals("$VALUES"))
                                                            .collect(Collectors.joining(", "));
                                                    return splitFullName[splitFullName.length - 1] + " (enum: " + availableEnums + ")";
                                                }
                                                return splitFullName[splitFullName.length - 1] + " (can't infer)";
                                            }

                                        }).collect(Collectors.joining(", ")))
                                .map(s -> s.equals("") ? "<no arguments>" : s)
                                .collect(Collectors.joining("\n\n"))) //Separate each method by a newline
                                .setItalic(true))));
                return false;
            }

            ReflectionMethodData methodToUse = possibleMethodsBySize.get(0);
            lintingPositions.add(new LintingPos(classAndMethod[1].index, Color.CLEAR, new TextBlock(Fonts.ROBOTO, 14, 300,
                    executingUsingReflectionComponent,
                    new TextComponent("Will execute "),
                    new TextComponent(methodToUse.methodName + Arrays.stream(args)
                            .map(StringIndex::toString).collect(Collectors.joining(", ", "(", ")"))).setItalic(true),
                    new TextComponent(" on the robot"))));

            List<String> argumentTypes = Arrays.stream(methodToUse.parameterTypes).collect(Collectors.toList());

            for (int i = 0; i < args.length; i++) {
                lintingPositions.add(new LintingPos(args[i].index, Color.CLEAR, new TextBlock(Fonts.ROBOTO, 14, 300,
                        executingUsingReflectionComponent,
                        new TextComponent("Inferred type: "),
                        new TextComponent(argumentTypes.get(i)).setItalic(true))));
            }


            sendableCommands.add(new SendableCommand(reflectionClassData.fullName + "." + methodToUse.methodName,
                    Arrays.stream(args).map(StringIndex::toString).toArray(String[]::new),
                    argumentTypes.toArray(String[]::new),
                    true));

            return true;
        } else {
            lintingPositions.add(new LintingPos(classAndMethod[1].index, Color.RED,
                    new TextBlock(Fonts.ROBOTO, 14, 300,
                            executingUsingReflectionComponent,
                            new TextComponent("Could not find method: "),
                            new TextComponent(classAndMethod[1].string).setItalic(true))));
            return false;
        }
    }


    public static boolean isStatic(@Nullable ReflectionMethodData methodData) {
        if (methodData == null) return false;
        return Modifier.isStatic(methodData.modifiers);
    }
}
