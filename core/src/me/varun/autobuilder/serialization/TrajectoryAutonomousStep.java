package me.varun.autobuilder.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrajectoryAutonomousStep extends AbstractAutonomousStep {
    private final List<Trajectory.State> states;
    private final List<Pose2d> pose2DList;
    private final boolean reversed;
    private final float color;
    private final float velocityStart;
    private final float velocityEnd;

    @JsonCreator
    public TrajectoryAutonomousStep(@JsonProperty(required = true, value = "states") @Nullable List<Trajectory.State> m_states,
                                    @JsonProperty(required = true, value = "pointList") @Nullable List<Pose2d> point2DList,
                                    @JsonProperty(required = true, value = "reversed") boolean reversed,
                                    @JsonProperty(required = true, value = "color") float color,
                                    @JsonProperty(required = true, value = "closed") boolean closed,
                                    @JsonProperty(defaultValue = "0", value = "velocityStart")float velocityStart,
                                    @JsonProperty(defaultValue = "0", value = "velocityEnd")float velocityEnd) {
        super(closed);
        this.reversed = reversed;
        this.color = color;
        this.pose2DList = point2DList;

        this.states = m_states;
        this.velocityStart = velocityStart;
        this.velocityEnd = velocityEnd;
    }

    public Trajectory getTrajectory(){
        return new Trajectory(states);
    }

    @JsonProperty("pointList")
    public List<Pose2d> getPose2DList(){

        return pose2DList;
    }

    @JsonProperty
    public boolean isReversed(){
        return reversed;
    }


    @Override
    public void execute() {

    }

    @Override
    public String toString() {
        return "TrajectoryAutonomousStep{" +
                "m_states=" + states +
                ", pose2DList=" + pose2DList +
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
}
