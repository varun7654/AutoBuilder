package com.dacubeking.autobuilder.gui.undo;

import com.dacubeking.autobuilder.gui.config.Config;
import com.dacubeking.autobuilder.gui.serialization.path.Autonomous;

public record UndoState(Autonomous autonomous, Config config) {
}
