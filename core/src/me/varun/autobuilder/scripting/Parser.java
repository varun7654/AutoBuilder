package me.varun.autobuilder.scripting;

import com.badlogic.gdx.graphics.Color;
import me.varun.autobuilder.gui.textrendering.Fonts;
import me.varun.autobuilder.gui.textrendering.TextBlock;
import me.varun.autobuilder.gui.textrendering.TextComponent;
import me.varun.autobuilder.scripting.util.ErrorPos;
import me.varun.autobuilder.scripting.util.StringIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static me.varun.autobuilder.scripting.util.StringIndex.splitWithIndex;

public class Parser {
    private static final ArrayList<Function<String, Object>> TRY_PARSE_LIST;
    static {
        TRY_PARSE_LIST = new ArrayList<>();
        TRY_PARSE_LIST.add((Integer::parseInt));
        TRY_PARSE_LIST.add(Double::parseDouble);
        TRY_PARSE_LIST.add(s -> {
            if (s.equalsIgnoreCase("true")) {
                return true;
            } else if (s.equalsIgnoreCase("false")) return false;
            throw new IllegalArgumentException();
        });
        TRY_PARSE_LIST.add(String::valueOf);
    }

    private static final Pattern pattern = Pattern.compile(" ");

    public static boolean execute(String string, List<ErrorPos> errorPositions) {
        String[] commands = string.split("\n"); //Start by splitting everything by lines (commands)
        boolean error = false;
        int prevIndex = 0;
        for (String command : commands) {
            if (command.startsWith("#")) continue; //Ignore comments
            StringIndex[] parts = splitWithIndex(command, pattern, prevIndex); //Split by spaces
            if (parts.length == 0) continue; //Ignore empty lines
            StringIndex method = parts[0]; //Get the method name
            //if(!methods.contains(method)) return false; //Exit early if the method doesn't exist
            StringIndex[] args = new StringIndex[parts.length - 1]; //Initialize the argument array
            System.arraycopy(parts, 1, args, 0, parts.length - 1);

            switch (method.string) {
                case "print":
                    if (args.length == 0) {
                        error = true;
                        errorPositions.add(new ErrorPos(method.index, Color.RED,
                                new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Usage: print <text>\n").setBold(true),
                                        new TextComponent("Expected 1 or more argument(s) after print"))));
                    } else {
                        StringBuilder aggregate = new StringBuilder();
                        for (StringIndex arg : args) {
                            aggregate.append(arg.string).append(" ");
                        }

                        errorPositions.add(new ErrorPos(method.index, Color.CLEAR,
                                new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Built in method\n").setBold(true),
                                        new TextComponent("Will print \""),
                                        new TextComponent(aggregate.toString()).setItalic(true),
                                        new TextComponent("\" to the console"))));
                    }
                    break;
                case "sleep":
                    if (args.length != 1) {
                        error = true;
                        errorPositions.add(new ErrorPos(method.index, Color.RED,
                                new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Usage: sleep <time>\n").setBold(true),
                                        new TextComponent("Expected a long after sleep"))));
                        break;
                    }

                    try {
                        long duration = Long.parseLong(args[0].string);

                        errorPositions.add(new ErrorPos(method.index, Color.CLEAR,
                                new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Built in method\n").setBold(true),
                                        new TextComponent("Will sleep for " + duration + "ms").setBold(false))));
                    } catch (NumberFormatException e) {
                        error = true;
                        errorPositions.add(new ErrorPos(args[0].index, Color.RED,
                                new TextBlock(Fonts.ROBOTO, 14, 300,
                                        new TextComponent("Usage: sleep <time>\n").setBold(true),
                                        new TextComponent("Expected a long after sleep"))));
                    }

                    break;
                default:
                    //if (AutoBuilder.getConfig().getScriptMethods().contains(method)) break; //If the method exists, continue
                    //Try using reflection
                    if (!RobotCodeData.validateMethod(method, args, errorPositions)) error = true;
                    break;
            }

            prevIndex += command.length() + 1; // The +1 is for the newline character
        }
        return error;
    }

    private static Class<?> getPrimitiveClass(Class<?> cls) {
        if (cls.equals(Integer.class)) return int.class;
        if (cls.equals(Double.class)) return double.class;
        return cls;
    }
}
