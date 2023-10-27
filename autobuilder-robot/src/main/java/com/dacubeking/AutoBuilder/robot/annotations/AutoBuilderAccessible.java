package com.dacubeking.AutoBuilder.robot.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Represents an instance of a class that Autos should have access to.
 * <p>
 * The code will automatically find singleton instances that have been a @code{getInstance} method without the use of this
 * annotation. For other instances that are not singletons, the code will automatically find the instance by searching for this
 * annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface AutoBuilderAccessible {

    /**
     * The name that should be used on the GUI to access this instance.
     * <p>
     * The allowed characters are: a-z, A-Z, 0-9, and _
     */
    String alias() default "";
}
