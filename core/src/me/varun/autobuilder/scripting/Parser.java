package me.varun.autobuilder.scripting;

import java.util.List;

public class Parser {
    public static boolean execute(String string, List<String> methods){
        String[] commands = string.split("\n"); //Start by splitting everything by lines (commands)
        for (String command : commands) {
            if(command.startsWith("#")) continue; //Ignore comments
            String[] parts = command.split(" "); //Split by spaces
            if(parts.length == 0) continue; //Ignore empty lines
            String method = parts[0]; //Get the method name
            if(!methods.contains(method)) return false; //Exit early if the method doesn't exist
            String[] args = new String[parts.length - 1]; //Initialize the argument array
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
            try {
                switch (method) {
                    case "print":
                        if(args.length == 0) return false;
                        break;
                    case "sleep":
                        Long.parseLong(args[0]);
                        break;
                    //Add more cases for other robot functions
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
