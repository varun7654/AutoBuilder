package me.varun.autobuilder.serialization.path;

public class NotDeployableException extends Exception {

    public NotDeployableException() {
        super();
    }

    public NotDeployableException(String message) {
        super(message);
    }

    public NotDeployableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotDeployableException(Throwable cause) {
        super(cause);
    }
}