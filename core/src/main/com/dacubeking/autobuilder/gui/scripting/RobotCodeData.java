package com.dacubeking.autobuilder.gui.scripting;

import com.badlogic.gdx.graphics.Color;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.net.Serializer;
import com.dacubeking.autobuilder.gui.scripting.reflection.ReflectionClassData;
import com.dacubeking.autobuilder.gui.scripting.reflection.ReflectionClassDataList;
import com.dacubeking.autobuilder.gui.scripting.reflection.ReflectionMethodData;
import com.dacubeking.autobuilder.gui.scripting.sendable.SendableCommand;
import com.dacubeking.autobuilder.gui.scripting.util.LintingPos;
import com.dacubeking.autobuilder.gui.scripting.util.StringIndex;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dacubeking.autobuilder.gui.scripting.util.StringIndex.splitWithIndex;

public class RobotCodeData {

    public static Map<String, Function<String, Boolean>> inferableTypesVerification;

    static {
        inferableTypesVerification = new Hashtable<>();
        inferableTypesVerification.put(int.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(double.class.getName(), s -> {
            if (!s.matches("^[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)$")) {
                return false;
            }
            try {
                Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(float.class.getName(), s -> {
            if (!s.matches("^[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)$")) {
                return false;
            }
            try {
                Float.parseFloat(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(long.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Long.parseLong(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(short.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Short.parseShort(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(byte.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Byte.parseByte(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(byte.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Byte.parseByte(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(char.class.getName(), s -> s.matches("^.$"));

        inferableTypesVerification.put(boolean.class.getName(), s -> s.matches("^(?i)(true|false)$"));

        inferableTypesVerification.put(String.class.getName(), s -> true);

        inferableTypesVerification.put(Integer.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(Double.class.getName(), s -> {
            if (!s.matches("^[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)$")) {
                return false;
            }
            try {
                Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(Float.class.getName(), s -> {
            if (!s.matches("^[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)$")) {
                return false;
            }
            try {
                Float.parseFloat(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(Long.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Long.parseLong(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(Short.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Short.parseShort(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(Byte.class.getName(), s -> {
            if (!s.matches("^-?[0-9]+$")) {
                return false;
            }
            try {
                Byte.parseByte(s);
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        });

        inferableTypesVerification.put(Character.class.getName(), s -> s.matches("^.$"));

        inferableTypesVerification.put(Boolean.class.getName(), s -> s.matches("^(?i)(true|false)$"));
    }


    public static List<ReflectionClassData> robotClasses = new ArrayList<>();

    public static Hashtable<String, ReflectionClassData> robotClassesMap = new Hashtable<>();
    public static Hashtable<String, ReflectionClassData> robotFullNameClassesMap = new Hashtable<>();
    public static List<String> accessibleClasses;

    public static void initData() {
        try {
            File file = new File(AutoBuilder.USER_DIRECTORY + "/" + AutoBuilder.getConfig().getRobotCodeDataFile());
            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            fileInputStream.close();
            String string = new String(bytes);

            ReflectionClassDataList reflectionClassDataList =
                    (ReflectionClassDataList) Serializer.deserialize(string, ReflectionClassDataList.class);

            robotClasses = reflectionClassDataList.reflectionClassData;
            accessibleClasses = reflectionClassDataList.instanceLocations;

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

    public static boolean validateMethod(StringIndex classMethod, StringIndex[] args, List<LintingPos> lintingPositions,
                                         ArrayList<SendableCommand> sendableCommands) {
        StringIndex[] classAndMethodSplitByPeriod = splitWithIndex(classMethod.string(), periodPattern, classMethod.index());
        if (classAndMethodSplitByPeriod.length == 0) {
            // Nothing was given
            return false;
        }

        List<StringIndex[]> possibleClassAndMethods = new ArrayList<>(); // [0] = class, [1] = method; the last one will be the
        // one displayed if no valid class is found
        if (classAndMethodSplitByPeriod.length > 2) {

            // Combines the elements of the split array into a single string
            String beginningCombined = Arrays.stream(classAndMethodSplitByPeriod)
                    // Don't include the last element
                    .limit(classAndMethodSplitByPeriod.length - 1)
                    .map(StringIndex::string) // Gets the string of the StringIndex
                    .collect(Collectors.joining(".")); // Joins them with periods

            StringIndex lastElement = classAndMethodSplitByPeriod[classAndMethodSplitByPeriod.length - 1];

            // Try the possibility that the last element is part of the class name
            possibleClassAndMethods.add(new StringIndex[]{
                    new StringIndex(classAndMethodSplitByPeriod[0].index(), beginningCombined + "." + lastElement.string()),
            });

            // Try the possibility that the last element is part of the method name
            possibleClassAndMethods.add(new StringIndex[]{
                    new StringIndex(classAndMethodSplitByPeriod[0].index(), beginningCombined),
                    lastElement
            });
        } else {
            possibleClassAndMethods.add(classAndMethodSplitByPeriod);
        }


        boolean error = false;
        List<TextComponent> classTextComponents = new ArrayList<>(); // Add text components that we want to display together
        classTextComponents.add(executingUsingReflectionComponent);

        ReflectionClassData reflectionClassData = null;
        boolean hasInstance;

        StringIndex[] classAndMethod = possibleClassAndMethods.get(0);

        for (StringIndex[] possibleClassAndMethod : possibleClassAndMethods) {
            // Try to find the class from the name
            reflectionClassData = robotClassesMap.get(possibleClassAndMethod[0].string());
            if (reflectionClassData == null) {
                // Try to find the class from the full name
                reflectionClassData = robotFullNameClassesMap.get(possibleClassAndMethod[0].string());
            }

            if (reflectionClassData != null) {
                classAndMethod = possibleClassAndMethod;
                break; // Found the class
            }
        }

        if (reflectionClassData == null) {
            classTextComponents.add(new TextComponent("\nCould not find class: ")); // Add the error message
            classTextComponents.add(new TextComponent(classAndMethod[0].string()).setItalic(true));
            error = true; // Set the error flag, but continue to allow us to complain about not having a method
            hasInstance = false;
        } else {
            if (accessibleClasses.contains(reflectionClassData.fullName)) {
                hasInstance = true;
                classTextComponents.add(new TextComponent("Using annotated instance for class "));
            } else {
                // Try to find a getInstance method

                Optional<ReflectionMethodData> singletonMethod;
                if (reflectionClassData.methodMap.containsKey("getInstance")) {
                    singletonMethod = reflectionClassData.methodMap.get("getInstance").stream()
                            .filter(methodData -> methodData.parameterTypes.length == 0).findFirst();
                } else {
                    singletonMethod = Optional.empty();
                }

                if (singletonMethod.isPresent()) {
                    hasInstance = isStatic(singletonMethod.get()) &&
                            singletonMethod.get().returnType.equals(reflectionClassData.fullName);
                    classTextComponents.add(new TextComponent("Using singleton class "));
                } else {
                    classTextComponents.add(new TextComponent("Using class "));
                    hasInstance = false;
                }
            }
            classTextComponents.add(new TextComponent(reflectionClassData.fullName).setItalic(true));
        }


        if (classAndMethod.length == 1 /* no arguments */ && hasInstance && reflectionClassData.isCommand) {
            classTextComponents.add(new TextComponent("\n\nThis class is a command and the corresponding, initialize, execute, " +
                    "end methods will be called\n\n"));
            classTextComponents.add(new TextComponent("This will not be run by the scheduler, and requirements will not be " +
                    "checked").setBold(true));
            classTextComponents.add(new TextComponent("\nThe isFinished method will be respected and the command will continue " +
                    "to be executed until it returns true. Sequential commands will not be run until this command returns true"));
            sendableCommands.add(new SendableCommand(reflectionClassData.fullName,
                    new String[]{}, new String[]{}, true, true));
            createLintingPos(lintingPositions, classMethod.index(), false, classTextComponents);
            return true;
        } else if (classAndMethod.length <= 1) {
            classTextComponents.add(new TextComponent("\n\nExpected a method after class"));
            error = true;
        }


        createLintingPos(lintingPositions, classMethod.index(), error, classTextComponents);

        if (error) {
            // Don't continue if there was an error
            return false;
        }

        if (!reflectionClassData.methodMap.containsKey(classAndMethod[1].string())) {
            // The method doesn't exist
            createLintingPos(lintingPositions, classAndMethod[1].index(), true,
                    executingUsingReflectionComponent,
                    new TextComponent("Could not find method: "),
                    new TextComponent(classAndMethod[1].string()).setItalic(true));
            return false;
        }

        // A method with the given name exists
        List<ReflectionMethodData> callableMethods = reflectionClassData.methodMap.get(classAndMethod[1].string());
        if (!hasInstance) {
            // Remove all methods that require an instance (filter out non-static methods)
            callableMethods = callableMethods.stream().filter(RobotCodeData::isStatic).collect(Collectors.toList());
        }

        if (callableMethods.size() == 0) {
            // No methods with the given name have a way to be called
            createLintingPos(lintingPositions, classAndMethod[1].index(), true,
                    executingUsingReflectionComponent,
                    new TextComponent("\nFound method(s) with the name "),
                    new TextComponent(classAndMethod[1].string()).setItalic(true),
                    new TextComponent("""
                             but none are static or have a singleton instance
                                                            
                            Possible Fixes:
                               - Make the method static
                               - Make the class a singleton
                               - Make an instance of the class accessible with the"""),
                    new TextComponent(" @AutoBuilderAccessible ").setBold(false).setColor(Color.ORANGE),
                    new TextComponent("annotation"));
            return false;
        }

        // Filter out methods that don't have the correct number of parameters
        List<ReflectionMethodData> possibleMethodsBySize = reflectionClassData.methodMap.get(classAndMethod[1].string())
                .stream()
                .filter(methodData -> methodData.parameterTypes.length == args.length).toList();

        if (possibleMethodsBySize.isEmpty()) {
            createLintingPos(lintingPositions, classAndMethod[1].index(), true,
                    executingUsingReflectionComponent,
                    new TextComponent("Method "),
                    new TextComponent(classAndMethod[1].string()).setItalic(true),
                    new TextComponent(" is not applicable for arguments n = "),
                    new TextComponent(Integer.toString(args.length)).setItalic(true),
                    new TextComponent("\n\nPossible arguments are:\n"),
                    getPossibleArguments(reflectionClassData, classAndMethod)
            );
            return false;
        }

        // Try match to a method with the correct arguments
        Optional<ReflectionMethodData> methodToUse = possibleMethodsBySize.stream().filter(methodData -> {
            for (int i = 0; i < args.length; i++) {
                String parameterType = methodData.parameterTypes[i];
                if (inferableTypesVerification.containsKey(parameterType)) {
                    // check if the string can be converted to the type
                    if (!inferableTypesVerification.get(parameterType).apply(args[i].string())) {
                        return false;
                    }
                } else if (robotFullNameClassesMap.containsKey(parameterType)) { // Maybe it's an enum
                    // Get the class that represents the enum
                    ReflectionClassData enumData = robotFullNameClassesMap.get(parameterType);
                    int finalI = i;
                    if (Arrays.stream(enumData.fieldNames).noneMatch(
                            enumField -> !enumField.equals("$VALUES") && enumField.equals(args[finalI].string()))) {
                        return false; // The enum doesn't have a field with the given name
                    }
                } else {
                    return false; // The type is not inferable
                }
            }
            return true;
        }).findFirst(); // There should only be one method that matches the arguments (otherwise something is wrong)

        if (methodToUse.isEmpty()) {
            // No method was found for the given types
            createLintingPos(lintingPositions, classAndMethod[1].index(), true,
                    executingUsingReflectionComponent,
                    new TextComponent("Method "),
                    new TextComponent(classAndMethod[1].string()).setItalic(true),
                    new TextComponent(" is not applicable for arguments "),
                    new TextComponent(
                            Arrays.stream(args)
                                    .map(StringIndex::string)
                                    .collect(Collectors.joining(", "))) // Join the arguments with commas
                            .setItalic(true),
                    new TextComponent("\n\nPossible arguments are:\n"),
                    getPossibleArguments(reflectionClassData, classAndMethod)
            );
            return false;
        }

        ReflectionMethodData method = methodToUse.get();
        createLintingPos(lintingPositions, classAndMethod[1].index(), false,
                executingUsingReflectionComponent,
                new TextComponent("Will execute "),
                // Make array of {arg1, arg2, arg3} into "(arg1, arg2, arg3)"
                new TextComponent(method.methodName + Arrays.stream(args)
                        .map(StringIndex::toString)
                        .collect(Collectors.joining(", ", "(", ")"))).setItalic(true),
                new TextComponent(" on the robot"));

        List<String> argumentTypes = Arrays.stream(method.parameterTypes).toList();

        for (int i = 0; i < args.length; i++) {
            createLintingPos(lintingPositions, args[i].index(), false,
                    executingUsingReflectionComponent,
                    new TextComponent("Inferred type: "),
                    new TextComponent(argumentTypes.get(i)).setItalic(true));
        }


        sendableCommands.add(new SendableCommand(reflectionClassData.fullName + "." + method.methodName,
                Arrays.stream(args).map(StringIndex::toString).toArray(String[]::new),
                argumentTypes.toArray(String[]::new),
                true));

        return true;
    }

    private static TextComponent getPossibleArguments(ReflectionClassData reflectionClassData, StringIndex[] classAndMethod) {
        return new TextComponent(reflectionClassData.methodMap.get(classAndMethod[1].string()).stream()
                .map(methodData -> Arrays.stream(methodData.parameterTypes) // Map a method into a string of its argument types
                        .map(parameterName -> { // Remove the package name from the argument name. Also add the
                            // enum fields names if applicable.
                            String[] splitFullName = parameterName.split("\\.");
                            if (inferableTypesVerification.containsKey(parameterName)) {
                                return splitFullName[splitFullName.length - 1];
                            } else { // Maybe it's an enum
                                ReflectionClassData enumData = robotFullNameClassesMap.get(parameterName);
                                if (enumData != null && enumData.isEnum) {
                                    splitFullName = splitFullName[splitFullName.length - 1].split("\\$");
                                    String availableEnums = Arrays.stream(enumData.fieldNames)
                                            .filter(n -> !n.equals("$VALUES"))
                                            .collect(Collectors.joining(", "));
                                    return splitFullName[splitFullName.length - 1] + " (enum: " + availableEnums + ")";
                                }
                                return splitFullName[splitFullName.length - 1] + " (can't infer)";
                            }
                        }).collect(Collectors.joining(", ")))
                .map(s -> s.equals("") ? "<no arguments>" : s)
                .collect(Collectors.joining("\n\n"))) //Separate each method by a newline
                .setItalic(true);
    }


    private static boolean isStatic(@Nullable ReflectionMethodData methodData) {
        if (methodData == null) {
            return false;
        }
        return Modifier.isStatic(methodData.modifiers);
    }

    private static void createLintingPos(List<LintingPos> lintingPositions, int index, boolean error,
                                         List<TextComponent> textComponents) {
        createLintingPos(lintingPositions, index, error, textComponents.toArray(TextComponent[]::new));
    }

    private static void createLintingPos(List<LintingPos> lintingPositions, int index, boolean error,
                                         TextComponent... textComponents) {
        lintingPositions.add(new LintingPos(index, error ? Color.RED : Color.CLEAR,
                new TextBlock(Fonts.ROBOTO, 14, 300, textComponents)));
    }
}
