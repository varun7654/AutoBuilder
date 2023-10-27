package com.dacubeking.AutoBuilder.robot.serialization.command;

public class CommandExecutionFailedException extends Exception {

    public CommandExecutionFailedException() {
        super();
    }

    public CommandExecutionFailedException(String message) {
        super(message);
    }

    public CommandExecutionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandExecutionFailedException(Throwable cause) {
        super(cause);
    }

    protected CommandExecutionFailedException(String message, Throwable cause, boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
