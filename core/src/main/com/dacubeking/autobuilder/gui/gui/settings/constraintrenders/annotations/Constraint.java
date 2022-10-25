package com.dacubeking.autobuilder.gui.gui.settings.constraintrenders.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {
    String name();

    String description();
}
