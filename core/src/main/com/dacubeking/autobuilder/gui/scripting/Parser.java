package com.dacubeking.autobuilder.gui.scripting;

import com.badlogic.gdx.graphics.Color;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.gui.textrendering.Fonts;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextBlock;
import com.dacubeking.autobuilder.gui.gui.textrendering.TextComponent;
import com.dacubeking.autobuilder.gui.scripting.sendable.SendableCommand;
import com.dacubeking.autobuilder.gui.scripting.sendable.SendableScript;
import com.dacubeking.autobuilder.gui.scripting.sendable.SendableScript.DelayType;
import com.dacubeking.autobuilder.gui.scripting.util.LintingPos;
import com.dacubeking.autobuilder.gui.scripting.util.StringIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.dacubeking.autobuilder.gui.scripting.util.StringIndex.splitWithIndex;

public class Parser {
    private static final Pattern spacePattern = Pattern.compile(" ");

    private final static Color DARK_GREEN = Color.valueOf("007400");
    private final static Color ORANGE = Color.valueOf("#FF9A00");
    private final static Color PINK = Color.valueOf("#a811aa");

    /**
     * Parses a string into a list of {@link SendableCommand}s
     *
     * @param string           The string to parse
     * @param lintingPositions List of {@link LintingPos}s to add syntax errors to
     * @param sendableScript   List of {@link SendableCommand}s to add parsed scripts to
     * @return true if the string was parsed successfully, false otherwise
     */
    public static boolean execute(@NotNull String string, @NotNull List<LintingPos> lintingPositions,
                                  @NotNull SendableScript sendableScript) {
        String[] commands = string.split("\n"); //Start by splitting everything by lines (commands)
        boolean error = false;
        int prevIndex = 0;
        ArrayList<SendableCommand> sendableCommands = sendableScript.getCommands();
        sendableCommands.clear();
        for (String command : commands) {
            if (command.startsWith("#")) {
                lintingPositions.add(new LintingPos(prevIndex, Color.CLEAR, DARK_GREEN, null));
                prevIndex += command.length() + 1; //Add 1 for the newline
                continue; //Ignore comments
            }

            if (command.isEmpty()) { //Ignore empty lines
                lintingPositions.add(new LintingPos(prevIndex, Color.CLEAR, Color.BLACK, null));
                prevIndex++; //Add 1 for the newline
                continue;
            }

            StringIndex[] parts = splitWithIndex(command, spacePattern, prevIndex); //Split by spaces
            if (parts.length == 0) {
                lintingPositions.add(new LintingPos(prevIndex, Color.CLEAR, Color.BLACK, null));
                prevIndex += command.length() + 1; //Add 1 for the newline
                continue;
            }
            StringIndex method = parts[0]; //Get the method name
            //if(!methods.contains(method)) return false; //Exit early if the method doesn't exist
            StringIndex[] args = new StringIndex[parts.length - 1]; //Initialize the argument array
            System.arraycopy(parts, 1, args, 0, parts.length - 1);

            switch (method.string()) {
                case "print":
                    if (args.length == 0) {
                        error = true;
                        lintingPositions.add(new LintingPos(method.index(), Color.RED, PINK, new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Usage: print <text>\n").setBold(true),
                                new TextComponent("Expected 1 or more argument(s) after print"))));
                    } else {
                        StringBuilder aggregate = new StringBuilder();
                        for (StringIndex arg : args) {
                            aggregate.append(arg.string()).append(" ");
                        }

                        var message = new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Built in method\n").setBold(true),
                                new TextComponent("Will print \""),
                                new TextComponent(aggregate.toString()).setItalic(true),
                                new TextComponent("\" to the console"));

                        lintingPositions.add(new LintingPos(method.index(), Color.CLEAR, PINK, message));
                        lintingPositions.add(new LintingPos(args[0].index(), Color.CLEAR, Color.BLACK, message));
                        sendableCommands.add(new SendableCommand(method.string(), new String[]{aggregate.toString()},
                                new String[]{String.class.getName()}, false));
                    }
                    break;
                case "sleep":
                    if (args.length != 1) {
                        error = true;

                        var message = new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Usage: sleep <time>\n").setBold(true),
                                new TextComponent("Expected a long after sleep"));

                        lintingPositions.add(new LintingPos(method.index(), Color.RED, PINK, message));

                        if (args.length > 1) {
                            lintingPositions.add(new LintingPos(args[1].index(), Color.RED, Color.BLACK, message));
                        }
                        break;
                    }

                    try {
                        long duration = Long.parseLong(args[0].string());

                        var message = new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Built in method\n").setBold(true),
                                new TextComponent("Will sleep for " + duration + "ms").setBold(false));

                        lintingPositions.add(new LintingPos(method.index(), Color.CLEAR, PINK, message));
                        lintingPositions.add(new LintingPos(args[0].index(), Color.CLEAR, message));

                        sendableCommands.add(new SendableCommand(method.string(), new String[]{args[0].string()},
                                new String[]{long.class.getName()}, false));
                    } catch (NumberFormatException e) {
                        error = true;
                        lintingPositions.add(new LintingPos(args[0].index(), Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
                                new TextComponent("Usage: sleep <time>\n").setBold(true),
                                new TextComponent("Expected a long after sleep"))));
                    }

                    break;
                default:
                    if (method.string().contains("@")) {
                        if (sendableScript.getDelayType() != DelayType.NONE) {
                            error = true;
                            lintingPositions.add(new LintingPos(method.index(), Color.RED, new TextBlock(Fonts.ROBOTO, 14, 350,
                                    new TextComponent("Cannot have multiple delays in a single script.\n"),
                                    new TextComponent("If you need multiple delays, create a new script block."))));
                            break;
                        }
                        if (method.string().contains("@t")) {
                            if (args.length < 1) {
                                error = true;
                                lintingPositions.add(
                                        new LintingPos(method.index(), Color.RED, ORANGE, new TextBlock(Fonts.ROBOTO, 14, 300,
                                                new TextComponent("Usage: @time <delay amount in seconds>\n").setBold(true),
                                                new TextComponent("Expected a delay amount in seconds (double)"))));
                            } else if (args.length > 1) {
                                error = true;
                                lintingPositions.add(
                                        new LintingPos(method.index(), Color.RED, ORANGE, new TextBlock(Fonts.ROBOTO, 14, 300,
                                                new TextComponent("Usage: @time <delay amount in seconds>\n").setBold(true),
                                                new TextComponent("Too many arguments"))));
                            } else {
                                if (args[0].string().matches("[0-9]+\\.?[0-9]*")) {
                                    sendableScript.setDelay(Double.parseDouble(args[0].string()));
                                    sendableScript.setDelayType(DelayType.TIME);
                                    lintingPositions.add(new LintingPos(method.index(), Color.CLEAR, ORANGE,
                                            new TextBlock(Fonts.ROBOTO, 14, 300,
                                                    new TextComponent("This script will be delayed by "),
                                                    new TextComponent(sendableScript.getDelay() + "").setItalic(true),
                                                    new TextComponent(" seconds"))));
                                } else {
                                    error = true;
                                    lintingPositions.add(new LintingPos(args[0].index(), Color.RED, ORANGE,
                                            new TextBlock(Fonts.ROBOTO, 14, 300,
                                                    new TextComponent("Usage: @time <delay amount in seconds>\n").setBold(true),
                                                    new TextComponent("Expected a delay amount in seconds (double)"))));
                                }
                            }
                        } else if (method.string().contains("@%") || method.string().contains("@p")) {
                            if (args.length < 1) {
                                error = true;
                                lintingPositions.add(
                                        new LintingPos(method.index(), Color.RED, ORANGE, new TextBlock(Fonts.ROBOTO, 14, 300,
                                                new TextComponent("Usage: @percentage <delay amount in percent>\n").setBold(true),
                                                new TextComponent("Expected a delay amount in percent (double)"))));
                            } else if (args.length > 1) {
                                error = true;
                                lintingPositions.add(
                                        new LintingPos(method.index(), Color.RED, ORANGE, new TextBlock(Fonts.ROBOTO, 14, 300,
                                                new TextComponent("Usage: @percentage <delay amount in percent>\n").setBold(true),
                                                new TextComponent("Too many arguments"))));
                            } else {
                                String usableString = args[0].string().replace("%", "");

                                if (usableString.matches("[0-9]+\\.?[0-9]*")) {
                                    sendableScript.setDelay(Double.parseDouble(usableString) / 100d);
                                    sendableScript.setDelayType(DelayType.PERCENT);
                                    lintingPositions.add(new LintingPos(method.index(), Color.CLEAR, ORANGE,
                                            new TextBlock(Fonts.ROBOTO, 14, 300,
                                                    new TextComponent("This script will execute after the path is "),
                                                    new TextComponent(sendableScript.getDelay() * 100 + "%").setItalic(true),
                                                    new TextComponent(" complete"))));
                                } else {
                                    error = true;
                                    lintingPositions.add(new LintingPos(args[0].index(), Color.RED, ORANGE,
                                            new TextBlock(Fonts.ROBOTO, 14, 300,
                                                    new TextComponent("Usage: @percent <delay amount in percent>\n").setBold(
                                                            true),
                                                    new TextComponent("Expected a delay amount in percent (double)"))));
                                }
                            }
                        } else {
                            error = true;
                            lintingPositions.add(
                                    new LintingPos(method.index(), Color.RED, ORANGE, new TextBlock(Fonts.ROBOTO, 14, 400,
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
                        if (!RobotCodeData.validateMethod(method, args, lintingPositions, sendableCommands)) {
                            error = true;
                        }
                    } else {
                        // Use fallback
                        if (AutoBuilder.getConfig().getScriptMethods().contains(
                                method.string())) { //If the method exists, continue
                            lintingPositions.add(new LintingPos(method.index(), Color.CLEAR, new TextBlock(Fonts.ROBOTO, 14, 300,
                                    new TextComponent("This method is in the list of allowed methods").setBold(true))));

                            String[] argTypes = new String[args.length];
                            Arrays.fill(argTypes, String.class.getName());

                            sendableCommands.add(new SendableCommand(method.string(),
                                    (String[]) Arrays.stream(args).map(StringIndex::string).toArray(),
                                    argTypes, false));
                        } else {
                            error = true;
                            lintingPositions.add(new LintingPos(method.index(), Color.RED, new TextBlock(Fonts.ROBOTO, 14, 300,
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
