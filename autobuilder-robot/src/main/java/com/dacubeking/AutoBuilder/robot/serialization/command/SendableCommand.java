package com.dacubeking.AutoBuilder.robot.serialization.command;

import com.dacubeking.AutoBuilder.robot.annotations.AutoBuilderRobotSide;
import com.dacubeking.AutoBuilder.robot.annotations.RequireWait;
import com.dacubeking.AutoBuilder.robot.robotinterface.AutonomousContainer;
import com.dacubeking.AutoBuilder.robot.utility.Utils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SendableCommand {

    @JsonProperty("methodName")
    @NotNull
    protected final String methodName;

    @JsonProperty("args") public final String @NotNull [] args;

    @JsonProperty("argTypes") public final String[] argTypes;

    @JsonProperty("reflection") public final boolean reflection;

    @JsonProperty("command") private boolean command;

    @AutoBuilderRobotSide
    private boolean shouldWait;
	
    @AutoBuilderRobotSide
    private boolean shouldCancelCommand;

    private static final @NotNull Map<String, Function<String, Object>> INFERABLE_TYPES_PARSER;

    static {
        INFERABLE_TYPES_PARSER = new HashMap<>();
        INFERABLE_TYPES_PARSER.put(int.class.getName(), Integer::parseInt);
        INFERABLE_TYPES_PARSER.put(double.class.getName(), Double::parseDouble);
        INFERABLE_TYPES_PARSER.put(float.class.getName(), Float::parseFloat);
        INFERABLE_TYPES_PARSER.put(long.class.getName(), Long::parseLong);
        INFERABLE_TYPES_PARSER.put(short.class.getName(), Short::parseShort);
        INFERABLE_TYPES_PARSER.put(byte.class.getName(), Byte::parseByte);
        INFERABLE_TYPES_PARSER.put(char.class.getName(), s -> s.charAt(0));
        INFERABLE_TYPES_PARSER.put(boolean.class.getName(), Boolean::parseBoolean);
        INFERABLE_TYPES_PARSER.put(String.class.getName(), s -> s);
        INFERABLE_TYPES_PARSER.put(Integer.class.getName(), Integer::valueOf);
        INFERABLE_TYPES_PARSER.put(Double.class.getName(), Double::valueOf);
        INFERABLE_TYPES_PARSER.put(Float.class.getName(), Float::valueOf);
        INFERABLE_TYPES_PARSER.put(Long.class.getName(), Long::valueOf);
        INFERABLE_TYPES_PARSER.put(Short.class.getName(), Short::valueOf);
        INFERABLE_TYPES_PARSER.put(Byte.class.getName(), Byte::valueOf);
        INFERABLE_TYPES_PARSER.put(Character.class.getName(), s -> Character.valueOf(s.charAt(0)));
        INFERABLE_TYPES_PARSER.put(Boolean.class.getName(), Boolean::valueOf);
    }

    @JsonIgnoreProperties
    private static final List<String> primitiveTypes = Arrays.asList(
            int.class.getName(),
            double.class.getName(),
            float.class.getName(),
            long.class.getName(),
            short.class.getName(),
            byte.class.getName(),
            char.class.getName(),
            boolean.class.getName()
    );

    public SendableCommand(@JsonProperty("methodName") String methodName,
                           @JsonProperty("args") String[] args,
                           @JsonProperty("argTypes") String[] argTypes,
                           @JsonProperty("reflection") boolean reflection) {
        this(methodName, args, argTypes, reflection, false);
    }

    @JsonCreator
    public SendableCommand(@JsonProperty("methodName") @NotNull String methodName,
                           @JsonProperty("args") String @NotNull [] args,
                           @JsonProperty("argTypes") String[] argTypes,
                           @JsonProperty("reflection") boolean reflection,
                           @JsonProperty("command") boolean command) {
        Method methodToCall = null;
        Object instance = null;
        boolean shouldWait;

        this.methodName = methodName;
        this.args = args;
        this.argTypes = argTypes;
        this.reflection = reflection;
        this.command = command;

        objArgs = new Object[args.length];

        if (Utils.isOnRobot()) {
            init();
        }
    }

    @AutoBuilderRobotSide
    private void init() {
        if (command) {
            // If we're a command, the command name is the method name
            if (AutonomousContainer.getInstance().getAccessibleInstances().containsKey(methodName)) {
                instance = AutonomousContainer.getInstance().getAccessibleInstances().get(methodName);
            } else {
                throwIllegalArgumentException(
                        "Command " + methodName + " not found. Make sure it's annotated with @AutoBuilderAccessible", null);
            }
            try {
                Command castedCommand = (Command) instance; // Check that it's actually a command to prevent errors from
                // occurring when we try to run it
            } catch (ClassCastException e) {
                throwIllegalArgumentException(
                        "Command " + methodName + " is not a Command. Make sure it's annotated with @AutoBuilderAccessible" +
                                "Try rebuilding your robotCodeData.json by rerunning your robot code in simulation.", e);
            }


            shouldCancelCommand = args.length > 0 && Boolean.parseBoolean(args[0]);

            assert instance != null;
            shouldWait = instance.getClass().isAnnotationPresent(RequireWait.class);
        } else {
            shouldCancelCommand = false;
            for (int i = 0; i < args.length; i++) {
                try {
                    // Parse the arguments into the correct types
                    if (INFERABLE_TYPES_PARSER.containsKey(argTypes[i])) {
                        // Convert the string to the correct type
                        objArgs[i] = INFERABLE_TYPES_PARSER.get(argTypes[i]).apply(args[i]);
                    } else {
                        // Convert the string to the correct enum if it's not a primitive type
                        objArgs[i] = Enum.valueOf(Class.forName(argTypes[i]).asSubclass(Enum.class), args[i]);
                    }
                } catch (ClassNotFoundException e) {
                    throwIllegalArgumentException("We couldn't find the class " + argTypes[i] +
                            ". Try rebuilding your robotCodeData.json by rerunning your robot code in simulation.", e);
                } catch (ClassCastException e) {
                    throwIllegalArgumentException("We couldn't cast the argument " + args[i] + " to the type " + argTypes[i] +
                            ". Try rebuilding your robotCodeData.json by rerunning your robot code in simulation.", e);
                } catch (NumberFormatException e) {
                    throwIllegalArgumentException(
                            "We couldn't parse number with the type " + args[i] + " to the type " + argTypes[i] +
                                    ". Try rebuilding your robotCodeData.json by rerunning your robot code in simulation.", e);
                }
            }

            if (reflection) {
                String[] splitMethod = methodName.split("\\.");

                String[] classNameArray = new String[splitMethod.length - 1];
                System.arraycopy(splitMethod, 0, classNameArray, 0, classNameArray.length);
                String className = String.join(".", classNameArray); // Get the class name that the method is in
                try {
                    Class<?> cls = Class.forName(className); // Get the class that the method is in

                    // Get an array of the class types to find the correct method
                    Class<?>[] typeArray = new Class[argTypes.length];
                    for (int i = 0; i < objArgs.length; i++) {
                        if (primitiveTypes.contains(argTypes[i])) {
                            typeArray[i] = getPrimitiveClass(objArgs[i].getClass());
                        } else {
                            typeArray[i] = objArgs[i].getClass();
                        }
                    }

                    if (typeArray.length == 0) {
                        // If there are no arguments, just get the method with no arguments
                        methodToCall = cls.getDeclaredMethod(splitMethod[splitMethod.length - 1]);
                    } else {
                        // Get the method with the correct arguments
                        methodToCall = cls.getDeclaredMethod(splitMethod[splitMethod.length - 1], typeArray);
                    }
                    methodToCall.setAccessible(true); // Make the method accessible so that we can call it if it's private

                    if (AutonomousContainer.getInstance().getAccessibleInstances().containsKey(className)) {
                        // The user has specified an instance to use for this class, so use it
                        instance = AutonomousContainer.getInstance().getAccessibleInstances().get(className);
                    } else {
                        if (!Modifier.isStatic(methodToCall.getModifiers())) {
                            // If the method isn't static, we need to get an instance of the class
                            Method getInstance = cls.getDeclaredMethod("getInstance"); // Get the getInstance method
                            getInstance.setAccessible(true);
                            instance = getInstance.invoke(null); // Invoke the getInstance method to get an instance of the class
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throwIllegalArgumentException("Class not found: " + className + ". " + e.getMessage()
                            + ". Try rebuilding your robotCodeData.json by rerunning your robot code in simulation.", e);
                } catch (NoSuchMethodException e) {
                    throwIllegalArgumentException("Could not find method : " + splitMethod[splitMethod.length - 1] + " in class "
                            + className + ". " + e.getMessage()
                            + ". Try rebuilding your robotCodeData.json by rerunning your robot code in simulation.", e);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throwIllegalArgumentException("Could not get singleton reference in class " + className + " for method: " +
                            splitMethod[splitMethod.length - 1] + ". " + e.getMessage(), e);
                }
            }
            shouldWait = methodToCall != null && methodToCall.isAnnotationPresent(RequireWait.class);
        }

        shouldWait = shouldWait || AutonomousContainer.getInstance().getRequireWaitObjects().contains(instance);

        if (!reflection && methodName.equals("sleep")) {
            // We no longer implement sleep as an actual sleep so convert it to a wait command
            command = true;
            instance = new WaitCommand(Double.parseDouble(args[0]));
            shouldWait = true;
        }
    }

    @AutoBuilderRobotSide
    private static void throwIllegalArgumentException(@NotNull String errorMessage, @Nullable Exception e) {
        DriverStation.reportError(errorMessage, false);
        throw new IllegalArgumentException(errorMessage, e);
    }

    @JsonIgnoreProperties
    @Nullable
    @AutoBuilderRobotSide
    private Object instance;

    @JsonIgnoreProperties
    @Nullable
    @AutoBuilderRobotSide
    private Method methodToCall;

    @JsonIgnoreProperties private final Object @NotNull [] objArgs;

    private static Class<?> getPrimitiveClass(Class<?> clazz) {
        if (clazz.equals(Integer.class)) {
            return double.class;
        } else if (clazz.equals(Double.class)) {
            return double.class;
        } else if (clazz.equals(Boolean.class)) {
            return boolean.class;
        } else if (clazz.equals(char.class)) {
            return char.class;
        } else if (clazz.equals(Byte.class)) {
            return byte.class;
        } else if (clazz.equals(Short.class)) {
            return short.class;
        } else if (clazz.equals(Long.class)) {
            return long.class;
        } else if (clazz.equals(Float.class)) {
            return float.class;
        } else if (clazz.equals(String.class)) {
            return String.class;
        } else {
            return clazz;
        }
    }


    @AutoBuilderRobotSide
    public void setFirstRun() {
        firstRun = true;
    }


    @AutoBuilderRobotSide
    public boolean firstRun;


    /**
     * @return true if the command is finished, false otherwise
     * @throws CommandExecutionFailedException If the command fails to execute for any other reason
     * @throws ExecutionException              Should never happen (for some reason the future was cancelled or threw and
     *                                         exception)
     */
    @AutoBuilderRobotSide
    protected boolean execute() throws CommandExecutionFailedException, ExecutionException {
        if (!command && methodToCall == null && reflection) {
            throw new CommandExecutionFailedException("Method to call is null");
        }

        if (command && instance == null) {
            throw new CommandExecutionFailedException("Instance is null when calling a command");
        }

        if (command) {
            var wpiCommand = (Command) instance;
            if (shouldCancelCommand) {
                wpiCommand.cancel();
            } else {
                if (firstRun) {
                    wpiCommand.initialize();
                    wpiCommand.schedule();
                    CommandScheduler.getInstance().schedule(wpiCommand);
                    firstRun = false;
                }

                if (shouldWait) {
                    return wpiCommand.isFinished();
                }
            }
        } else {
            if (shouldCancelCommand) {
                throw new CommandExecutionFailedException("Cannot cancel a method call");
            }
            var out = invokeMethod();

            // If the method returns true & is a boolean, stop executing it. If
            if (shouldWait && out instanceof Boolean) {
                return (Boolean) out;
            }
        }
        return true;
    }

    private @Nullable Object invokeMethod() throws CommandExecutionFailedException {
        assert !command;
        try {
            if (reflection) {
                assert methodToCall != null;
                return methodToCall.invoke(instance, objArgs); // If the method returns true & is a boolean, stop executing it
            } else {
                switch (methodName) {
                    case "print" -> System.out.println(objArgs[0]);
                    case "sleep" -> throw new UnsupportedOperationException(
                            "The sleep method should have been converted to a command");
                }
                return null;
            }
        } catch (Exception e) {
            throw new CommandExecutionFailedException("Could not invoke method " + methodName + " due to: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "SendableCommand{" +
                "methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", argTypes=" + Arrays.toString(argTypes) +
                ", reflection=" + reflection +
                ", command=" + command +
                ", shouldWait=" + shouldWait +
                ", shouldCancelCommand=" + shouldCancelCommand +
                ", instance=" + instance +
                ", methodToCall=" + methodToCall +
                ", objArgs=" + Arrays.toString(objArgs) +
                '}';
    }
}
