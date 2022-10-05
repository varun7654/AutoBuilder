package com.dacubeking.autobuilder.gui.gui.settings;

import com.dacubeking.autobuilder.gui.gui.elements.ScrollableGui;

public class SettingsGui extends ScrollableGui {

    public SettingsGui() {
        super(new SettingsGuiOpenIcon(), null);
    }

    public boolean update() {
        super.update(100);
        return panelOpen;
    }
}
