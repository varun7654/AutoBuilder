package com.dacubeking.autobuilder.gui.gui.shooter;

import com.dacubeking.autobuilder.gui.serialization.shooter.ShooterPreset;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.List;

public class ShooterConfig {
    private List<ShooterPreset> shooterConfigs;

    @JsonCreator
    public ShooterConfig() {
        shooterConfigs = new ArrayList<>();
    }

    public ShooterConfig(ArrayList<ShooterPreset> shooterConfigs) {
        this.shooterConfigs = shooterConfigs;
    }

    public List<ShooterPreset> getShooterConfigs() {
        return shooterConfigs;
    }
}
