package me.varun.autobuilder.serialization.path;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.varun.autobuilder.wpi.math.geometry.Rotation2d;
import me.varun.autobuilder.wpi.math.spline.Spline.ControlVector;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrajectoryAutonomousStep extends AbstractAutonomousStep {
    private final List<Trajectory.State> states;
    private final List<ControlVector> controlVectors;
    private final boolean reversed;
    private final float color;
    private final float velocityStart;
    private final float velocityEnd;
    private final List<Rotation2d> rotations;

    @JsonCreator
    public TrajectoryAutonomousStep(@JsonProperty(required = true, value = "states") @Nullable List<Trajectory.State> m_states,
                                    @JsonProperty(required = true, value = "pointList") @Nullable List<ControlVector> controlVectors,
                                    @JsonProperty(required = true, value = "rotations") List<Rotation2d> rotations,
                                    @JsonProperty(required = true, value = "reversed") boolean reversed,
                                    @JsonProperty(required = true, value = "color") float color,
                                    @JsonProperty(required = true, value = "closed") boolean closed,
                                    @JsonProperty(defaultValue = "0", value = "velocityStart") float velocityStart,
                                    @JsonProperty(defaultValue = "0", value = "velocityEnd") float velocityEnd) {
        super(closed);
        this.reversed = reversed;
        this.color = color;
        this.controlVectors = controlVectors;
        this.rotations = rotations;

        this.states = m_states;
        this.velocityStart = velocityStart;
        this.velocityEnd = velocityEnd;
    }

    public Trajectory getTrajectory() {
        return new Trajectory(states);
    }

    @JsonProperty("pointList")
    public List<ControlVector> getControlVectors() {
        return controlVectors;
    }

    @JsonProperty
    public boolean isReversed() {
        return reversed;
    }


    @Override
    public void execute() {

    }

    @Override
    public String toString() {
        return "TrajectoryAutonomousStep{" +
                "m_states=" + states +
                ", controlVectors=" + controlVectors +
                ", reversed=" + reversed +
                ", color=" + color +
                '}';
    }

    @JsonProperty
    public float getColor() {
        return color;
    }

    @JsonProperty("states")
    public List<Trajectory.State> getStates() {
        return states;
    }

    @JsonProperty
    public float getVelocityStart() {
        return velocityStart;
    }

    @JsonProperty
    public float getVelocityEnd() {
        return velocityEnd;
    }

    @JsonProperty
    public List<Rotation2d> getRotations() {
        return rotations;
    }
}
