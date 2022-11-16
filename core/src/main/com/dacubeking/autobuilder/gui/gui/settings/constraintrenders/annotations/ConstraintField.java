package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field in a constraint as a field that should be rendered in the settings menu. Any object can be marked
 * with this annotation, but the only fields that will be rendered as the final node are doubles.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.TYPE_USE})
public @interface ConstraintField {
    /**
     * The name of the field to be displayed in the settings menu.
     */
    String name();

    /**
     * The description of the field to be displayed in the settings menu. The description will be displayed in a tooltip when the
     * user hovers over the field. If no description is provided, no tooltip will be displayed.
     */
    String description() default "";
}
