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
import org.jetbrains.annotations.NotNull;
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

    private static final Color DARK_TEAL = Color.valueOf("00627a");
    private static final Color BLUE = Color.valueOf("005ad9");

    public static final @NotNull Map<String, Function<@NotNull String, @NotNull Boolean>> inferableTypesVerification;

    static {
        final Pattern intPattern = Pattern.compile("^-?[0-9]+$");
        final Pattern decimalPattern =
                Pattern.compile("^[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)$");

        final Function<String, Boolean> isInt = s -> checkNumberParseable(s, intPattern, Integer::parseInt);
        final Function<String, Boolean> isDouble = s -> checkNumberParseable(s, decimalPattern, Double::parseDouble);
        final Function<String, Boolean> isFloat = s -> checkNumberParseable(s, decimalPattern, Float::parseFloat);
        final Function<String, Boolean> isLong = s -> checkNumberParseable(s, intPattern, Long::parseLong);
        final Function<String, Boolean> isShort = s -> checkNumberParseable(s, intPattern, Short::parseShort);
        final Function<String, Boolean> isByte = s -> checkNumberParseable(s, intPattern, Byte::parseByte);
        final Function<String, Boolean> isChar = s -> s.length() == 1;
        final Function<String, Boolean> isBoolean = s -> s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");


        inferableTypesVerification = new Hashtable<>();
        inferableTypesVerification.put(int.class.getName(), isInt);
        inferableTypesVerification.put(double.class.getName(), isDouble);
        inferableTypesVerification.put(float.class.getName(), isFloat);
        inferableTypesVerification.put(long.class.getName(), isLong);
        inferableTypesVerification.put(short.class.getName(), isShort);
        inferableTypesVerification.put(byte.class.getName(), isByte);
        inferableTypesVerification.put(char.class.getName(), isChar);
        inferableTypesVerification.put(boolean.class.getName(), isBoolean);

        inferableTypesVerification.put(String.class.getName(), s -> true);
        inferableTypesVerification.put(Integer.class.getName(), isInt);
        inferableTypesVerification.put(Double.class.getName(), isDouble);
        inferableTypesVerification.put(Float.class.getName(), isFloat);
        inferableTypesVerification.put(Long.class.getName(), isLong);
        inferableTypesVerification.put(Short.class.getName(), isShort);
        inferableTypesVerification.put(Byte.class.getName(), isByte);
        inferableTypesVerification.put(Character.class.getName(), isChar);
        inferableTypesVerification.put(Boolean.class.getName(), isBoolean);
    }

    /**
     * Checks if a string is parseable as a number
     *
     * @param s             The string to check
     * @param pattern       The pattern to check against
     * @param parseFunction The function to parse the string with
     * @return True if the string is parseable as a number
     */
    private static boolean checkNumberParseable(@NotNull String s, @NotNull Pattern pattern,
                                                @NotNull Function<@NotNull String, ?> parseFunction) {
        if (pattern.asMatchPredicate().test(s)) {
            return false;
        }
        try {
            parseFunction.apply(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
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


        if (classAndMethod.length == 1 /* no arguments */ && hasInstance && reflectionClassData.isCommand
                && !classMethod.string().endsWith(".")) {
            classTextComponents.add(
                    new TextComponent("\n\nThis class is a command it will be executed by the command scheduler"));
            classTextComponents.add(new TextComponent("This will be run asynchronously by the scheduler unless the method has " +
                    "been annotated with the "));
            classTextComponents.add(new TextComponent("@RequireWait").setItalic(true).setColor(Color.ORANGE));
            classTextComponents.add(new TextComponent(" annotation."));
            classTextComponents.add(new TextComponent("\n\nIf the command has been annotated with the annotation sequential " +
                    "commands will not be run until this command stop executing."));
            sendableCommands.add(new SendableCommand(reflectionClassData.fullName,
                    new String[]{}, new String[]{}, true, true));
            createLintingPos(lintingPositions, classMethod.index(), false, DARK_TEAL, classTextComponents);
            return true;
        } else if (classAndMethod.length <= 1) {
            classTextComponents.add(new TextComponent("\n\nExpected a method after class or for the class to be a command"));
            error = true;
            createLintingPos(lintingPositions, classMethod.index(), error, classTextComponents);
        } else {
            createLintingPos(lintingPositions, classMethod.index(), error, BLUE, classTextComponents);
        }


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

    private static void createLintingPos(@NotNull List<LintingPos> lintingPositions, int index, boolean error,
                                         @NotNull List<TextComponent> textComponents) {
        createLintingPos(lintingPositions, index, error, textComponents.toArray(TextComponent[]::new));
    }

    private static void createLintingPos(@NotNull List<LintingPos> lintingPositions, int index, boolean error, Color textColor,
                                         @NotNull List<TextComponent> textComponents) {
        createLintingPos(lintingPositions, index, error, textColor, textComponents.toArray(TextComponent[]::new));
    }

    private static void createLintingPos(@NotNull List<LintingPos> lintingPositions, int index, boolean error,
                                         @NotNull TextComponent... textComponents) {
        createLintingPos(lintingPositions, index, error, Color.BLACK, textComponents);
    }

    private static void createLintingPos(@NotNull List<LintingPos> lintingPositions, int index, boolean error, Color textColor,
                                         @NotNull TextComponent... textComponents) {
        lintingPositions.add(new LintingPos(index, error ? Color.RED : Color.CLEAR, textColor,
                new TextBlock(Fonts.ROBOTO, 14, 300, textComponents)));
    }
}
