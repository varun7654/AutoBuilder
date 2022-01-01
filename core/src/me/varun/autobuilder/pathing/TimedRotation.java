package me.varun.autobuilder.pathing;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TimedRotation {
    @JsonProperty("time")
    public double time;

    @JsonProperty("rotation")
    public Rotation2d rotation;

    public TimedRotation() {
        this(0, new Rotation2d());
    }

    public TimedRotation(Rotation2d rotation) {
        this(0, rotation);
    }

    public TimedRotation(double time, Rotation2d rotation) {
        this.time = time;
        this.rotation = rotation;
    }

    public TimedRotation(@NotNull TimedRotation other) {
        this(other.time, other.rotation);
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull TimedRotation copy() {
        return new TimedRotation(this);
    }

    public Rotation2d getRotation() {
        return rotation;
    }
}
