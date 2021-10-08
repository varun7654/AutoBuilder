package me.varun.autobuilder.scripting;

import java.util.List;

public class Parser {
    public static boolean execute(String string, List<String> methods){
        String[] commands = string.split("\n");
        for (String command : commands) {
            StringBuilder methodName = null;
            StringBuilder argument = null;
            for (int i = 0; i < command.length(); i++) {
                char character = command.charAt(i);
                if(argument != null){
                    argument.append(character);
                } else if(methodName == null){
                    if(!Character.isWhitespace(character)){
                        methodName = new StringBuilder().append(character);
                    }

                } else if(!Character.isWhitespace(character)){
                    methodName.append(character);

                } else {
                    argument = new StringBuilder();
                }
            }

            System.out.println("Command " + command + " method name: " + methodName + " argument: " + argument);
            if(methodName == null) return false;
            switch (methodName.toString()){
                case "print":
                    if(argument == null) return false;
                    break;
                case "shootBalls":
                case "setShooterSpeed":
                    try{
                        Float.parseFloat(argument.toString());
                    } catch (NumberFormatException | NullPointerException e){
                        return false;
                    }
                    break;
                default:
                    if(!methods.contains(methodName.toString())) return false;
            }
        }
        return true;
    }
}
