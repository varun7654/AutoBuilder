package com.dacubeking.autobuilder.gui.events.movablepoint;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface MovablePointEventHandler {
    void onPointMove(@NotNull PointMoveEvent event);
}
