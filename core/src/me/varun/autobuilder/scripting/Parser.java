package me.varun.autobuilder.scripting;

import com.badlogic.gdx.graphics.Color;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextBlock;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.scripting.sendable.SendableCommand;
import me.varun.autobuilder.scripting.sendable.SendableScript;
import me.varun.autobuilder.scripting.sendable.SendableScript.DelayType;
import me.varun.autobuilder.scripting.util.ErrorPos;
import me.varun.autobuilder.scripting.util.StringIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static me.varun.autobuilder.scripting.util.StringIndex.splitWithIndex;

public class Parser {
    private static final Pattern spacePattern = Pattern.compile(" ");

    private final static Color DARK_GREEN = Color.valueOf("007400");

    /**
     * Parses a string into a list of {@link SendableCommand}s
     *
     * @param string         The string to parse
     * @param errorPositions List of {@link ErrorPos}s to add syntax errors to
     * @param sendableScript List of {@link SendableCommand}s to add parsed scripts to
     * @return true if the string was parsed successfully, false otherwise
     */
    public static boolean execute(@NotNull String string, @NotNull List<ErrorPos> errorPositions, @NotNull SendableScript sendableScript) {
        String[] commands = string.split("\n"); //Start by splitting everything by lines (commands)
        boolean error = false;
        int prevIndex = 0;
        ArrayList<SendableCommand> sendableCommands = sendableScript.getCommands();
        sendableCommands.clear();
        for (String command : commands) {
            if (command.startsWith("#")) continue; //Ignore comments
            StringIndex[] parts = splitWithIndex(command, spacePattern, prevIndex); //Split by spaces
            if (parts.length == 0) continue; //Ignore empty lines
            StringIndex method = parts[0]; //Get the method name
            //if(!methods.contains(method)) return false; //Exit early if the method doesn't exist
            StringIndex[] args = new StringIndex[parts.length - 1]; //Initialize the argument array
            System.arraycopy(parts, 1, args, 0, parts.length - 1);

            switch (method.string) {
                case "print":
                    if (args.length == 0) {
                        error = true;
                        errorPositions.add(new ErrorPos(method.index, Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Usage: print <text>\n").setBold(true),
                                new TextComponent("Expected 1 or more argument(s) after print"))));
                    } else {
                        StringBuilder aggregate = new StringBuilder();
                        for (StringIndex arg : args) {
                            aggregate.append(arg.string).append(" ");
                        }

                        errorPositions.add(new ErrorPos(method.index, Color.CLEAR, new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Built in method\n").setBold(true),
                                new TextComponent("Will print \""),
                                new TextComponent(aggregate.toString()).setItalic(true),
                                new TextComponent("\" to the console"))));
                        sendableCommands.add(new SendableCommand(method.string, new String[]{aggregate.toString()},
                                new String[]{String.class.getName()}, false));
                    }
                    break;
                case "sleep":
                    if (args.length != 1) {
                        error = true;
                        errorPositions.add(new ErrorPos(method.index, Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Usage: sleep <time>\n").setBold(true),
                                new TextComponent("Expected a long after sleep"))));
                        break;
                    }

                    try {
                        long duration = Long.parseLong(args[0].string);

                        errorPositions.add(new ErrorPos(method.index, Color.CLEAR, new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Built in method\n").setBold(true),
                                new TextComponent("Will sleep for " + duration + "ms").setBold(false))));

                        sendableCommands.add(new SendableCommand(method.string, new String[]{args[0].string},
                                new String[]{long.class.getName()}, false));

                    } catch (NumberFormatException e) {
                        error = true;
                        errorPositions.add(new ErrorPos(args[0].index, Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Usage: sleep <time>\n").setBold(true),
                                new TextComponent("Expected a long after sleep"))));
                    }

                    break;
                default:
                    if (method.string.contains("@")) {
                        if (method.string.contains("@t")) {
                            if (args.length < 1) {
                                error = true;
                                errorPositions.add(new ErrorPos(method.index, Color.RED, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Usage: @time <delay amount in seconds>\n").setBold(true),
                                        new TextComponent("Expected a delay amount in seconds (double)"))));


                            } else if (args.length > 1) {
                                error = true;
                                errorPositions.add(new ErrorPos(method.index, Color.RED, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Usage: @time <delay amount in seconds>\n").setBold(true),
                                        new TextComponent("Too many arguments"))));
                            } else {
                                if (args[0].string.matches("[0-9]+\\.?[0-9]*")) {
                                    sendableScript.setDelay(Double.parseDouble(args[0].string));
                                    sendableScript.setDelayType(DelayType.TIME);
                                    errorPositions.add(new ErrorPos(method.index, Color.CLEAR, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 300,
                                            new TextComponent("This script will be delayed by "),
                                            new TextComponent(sendableScript.getDelay() + "").setItalic(true),
                                            new TextComponent(" seconds"))));
                                } else {
                                    error = true;
                                    errorPositions.add(new ErrorPos(args[0].index, Color.RED, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 300,
                                            new TextComponent("Usage: @time <delay amount in seconds>\n").setBold(true),
                                            new TextComponent("Expected a delay amount in seconds (double)"))));

                                }
                            }


                        } else if (method.string.contains("@%") || method.string.contains("@p")) {
                            if (args.length < 1) {
                                error = true;
                                errorPositions.add(new ErrorPos(method.index, Color.RED, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Usage: @percentage <delay amount in percent>\n").setBold(true),
                                        new TextComponent("Expected a delay amount in percent (double)"))));
                            } else if (args.length > 1) {
                                error = true;
                                errorPositions.add(new ErrorPos(method.index, Color.RED, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Usage: @percentage <delay amount in percent>\n").setBold(true),
                                        new TextComponent("Too many arguments"))));
                            } else {
                                String usableString = args[0].string.replace("%", "");

                                if (usableString.matches("[0-9]+\\.?[0-9]*")) {
                                    sendableScript.setDelay(Double.parseDouble(usableString));
                                    sendableScript.setDelayType(DelayType.PERCENT);
                                    errorPositions.add(new ErrorPos(method.index, Color.CLEAR, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 300,
                                            new TextComponent("This script will execute after the path is "),
                                            new TextComponent(sendableScript.getDelay() + "%").setItalic(true),
                                            new TextComponent(" complete"))));
                                } else {
                                    error = true;
                                    errorPositions.add(new ErrorPos(args[0].index, Color.RED, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 300,
                                            new TextComponent("Usage: @percent <delay amount in percent>\n").setBold(true),
                                            new TextComponent("Expected a delay amount in percent (double)"))));
                                }
                            }
                        } else {
                            error = true;
                            errorPositions.add(new ErrorPos(method.index, Color.RED, DARK_GREEN, new TextBlock(Fonts.ROBOTO, 14, 400,
                                    new TextComponent("Usage: @<delay type> <delay amount>\n").setBold(true),
                                    new TextComponent("Expected a delay type after @\n\n"),
                                    new TextComponent("Valid delay types:\n"),
                                    new TextComponent("   @time - delay by a fixed time.\n"),
                                    new TextComponent("   @percent - delay by percent of time of the next path.\n"))));
                        }
                        break;
                    }


                    if (AutoBuilder.getConfig().getReflectionEnabled()) {
                        //Try using reflection
                        if (!RobotCodeData.validateMethod(method, args, errorPositions, sendableCommands)) error = true;
                    } else {
                        // Use fallback
                        if (AutoBuilder.getConfig().getScriptMethods().contains(method.string)) { //If the method exists, continue
                            errorPositions.add(new ErrorPos(method.index, Color.CLEAR, new TextBlock(Fonts.ROBOTO, 14, 300,
                                    new TextComponent("This method is in the list of allowed methods").setBold(true))));

                            String[] argTypes = new String[args.length];
                            Arrays.fill(argTypes, String.class.getName());

                            sendableCommands.add(new SendableCommand(method.string,
                                    (String[]) Arrays.stream(args).map(s -> s.string).toArray(),
                                    argTypes, false));
                        } else {
                            error = true;
                            errorPositions.add(new ErrorPos(method.index, Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                                    new TextComponent("This method is not in the list of allowed methods").setBold(true))));
                        }
                    }

                    break;
            }

            prevIndex += command.length() + 1; // The +1 is for the newline character
        }
        sendableScript.setDeployable(!error);
        return error;
    }
}
