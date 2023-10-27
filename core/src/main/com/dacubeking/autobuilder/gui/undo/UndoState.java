package com.dacubeking.autobuilder.gui.undo;

import com.dacubeking.AutoBuilder.robot.serialization.Autonomous;
import com.dacubeking.autobuilder.gui.config.Config;

public record UndoState(Autonomous autonomous, Config config) {
}
