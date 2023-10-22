package com.dacubeking.autobuilder.gui.serialization.shooter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShooterPreset implements Comparable<ShooterPreset> {
    private double hoodEjectAngle;
    private double flywheelSpeed;
    private double distance;

    @JsonCreator
    public ShooterPreset(@JsonProperty(required = true, value = "hoodEjectAngle") double hoodEjectAngle,
                         @JsonProperty(required = true, value = "flywheelSpeed") double flywheelSpeed,
                         @JsonProperty(required = true, value = "distance") double distance) {
        this.hoodEjectAngle = hoodEjectAngle;
        this.flywheelSpeed = flywheelSpeed;
        this.distance = distance;
    }

    @JsonProperty
    public double getHoodEjectAngle() {
        return hoodEjectAngle;
    }

    @JsonProperty
    public double getFlywheelSpeed() {
        return flywheelSpeed;
    }

    @JsonProperty
    public double getDistance() {
        return distance;
    }

    public void setHoodEjectAngle(double hoodEjectAngle) {
        this.hoodEjectAngle = hoodEjectAngle;
    }

    public void setFlywheelSpeed(double flywheelSpeed) {
        this.flywheelSpeed = flywheelSpeed;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(ShooterPreset a) {
        return Double.compare(this.getDistance(), a.getDistance());
    }
}