package me.varun.autobuilder.serialization;

import me.varun.autobuilder.wpi.math.geometry.Pose2d;
import me.varun.autobuilder.wpi.math.trajectory.Trajectory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryAutonomousStep extends AbstractAutonomousStep {
    private final List<SerializableTrajectoryState> m_states;
    private final List<SerializablePose2d> pose2DList;

    public TrajectoryAutonomousStep(List<Trajectory.State> m_states, @NotNull List<Pose2d> point2DList) {
        this.m_states = new ArrayList<>();
        for (Trajectory.State m_state : m_states) {
            this.m_states.add(new SerializableTrajectoryState(m_state));
        }

        this.pose2DList = new ArrayList<>();
        for (Pose2d pose2d : point2DList) {
            this.pose2DList.add(new SerializablePose2d(pose2d));
        }
    }

    public Trajectory getTrajectory(){
        List<Trajectory.State> states = new ArrayList<>();
        for (SerializableTrajectoryState m_state : m_states) {
            states.add(m_state.getState());
        }
        return new Trajectory(states);
    }

    public List<Pose2d> getPose2DList(){
        List<Pose2d> pose2dList = new ArrayList<>();
        for (SerializablePose2d serializablePose2d : pose2DList) {
            pose2dList.add(serializablePose2d.getPose2d());
        }
        return pose2dList;
    }



    @Override
    public void execute() {

    }

    @Override
    public String toString() {
        return "TrajectoryAutonomousStep{" +
                "m_states=" + m_states +
                '}';
    }
}
