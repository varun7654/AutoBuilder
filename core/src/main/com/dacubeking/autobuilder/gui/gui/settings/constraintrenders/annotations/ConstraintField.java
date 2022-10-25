package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.TYPE_USE})
public @interface ConstraintField {
    String name();

    String description();
}
