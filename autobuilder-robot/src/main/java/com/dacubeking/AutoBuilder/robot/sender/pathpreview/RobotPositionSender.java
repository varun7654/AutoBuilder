package com.dacubeking.AutoBuilder.robot.sender.pathpreview;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class RobotPositionSender {
    private static final NetworkTableEntry robotPositionsTable = NetworkTableInstance.getDefault()
            .getEntry("autodata/robotPositions");
    private final static Map<String, RobotState> robotStatesHashMap = new HashMap<>(4);

    private static final Predicate<RobotState> removeCondition = state -> state.timeCreated < Timer.getFPGATimestamp() - 0.2;

    public synchronized static void addRobotPosition(RobotState robotState) {
        robotStatesHashMap.put(robotState.name, robotState);
        if (robotState.name.equals("Robot Position")) {
            StringBuilder sb = new StringBuilder();

            robotStatesHashMap.values().removeIf(removeCondition); // remove values older than 0.2 seconds
            for (RobotState state : robotStatesHashMap.values()) {
                sb.append(state.toString()).append(";");
            }
            robotPositionsTable.setString(sb.toString());
        }
    }
}
