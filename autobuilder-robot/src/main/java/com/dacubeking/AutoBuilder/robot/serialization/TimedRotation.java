package com.dacubeking.AutoBuilder.robot.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Rotation2d;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Internal
public final class TimedRotation {
    @JsonProperty("time") double time;

    @JsonProperty("rotation") Rotation2d rotation;

    @JsonIgnore
    private TimedRotation() {
        this(0, new Rotation2d());
    }

    @JsonIgnore
    private TimedRotation(Rotation2d rotation) {
        this(0, rotation);
    }

    @JsonCreator
    private TimedRotation(@JsonProperty("time") double time,
                          @JsonProperty("rotation") Rotation2d rotation) {
        this.time = time;
        this.rotation = rotation;
    }

    @JsonIgnore
    private TimedRotation(@NotNull TimedRotation other) {
        this(other.time, other.rotation);
    }

    @JsonIgnore
    @Contract(value = " -> new", pure = true)
    @NotNull
    TimedRotation copy() {
        return new TimedRotation(this);
    }

    @JsonIgnore
    public Rotation2d getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return "TimedRotation{" +
                "time=" + time +
                ", rotation=" + rotation +
                '}';
    }
}
