package me.varun.autobuilder.net;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.config.gui.FileHandler;
import me.varun.autobuilder.gui.notification.Notification;
import me.varun.autobuilder.gui.notification.NotificationHandler;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.shooter.ShooterConfig;
import me.varun.autobuilder.pathing.RobotPosition;
import me.varun.autobuilder.serialization.path.Autonomous;
import me.varun.autobuilder.serialization.path.GuiSerializer;
import me.varun.autobuilder.serialization.path.NotDeployableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class NetworkTablesHelper {

    private static final float INCHES_PER_METER = 39.3700787f;
    static NetworkTablesHelper networkTablesInstance = new NetworkTablesHelper();
    private final ArrayList<List<RobotPosition>> robotPositions = new ArrayList<>();
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("autodata");
    NetworkTableEntry autoPath = table.getEntry("autoPath");

    NetworkTable smartDashboardTable = inst.getTable("SmartDashboard");
    NetworkTableEntry last_estimated_robot_pose_x = smartDashboardTable.getEntry("Last Estimated Robot Pose X");
    NetworkTableEntry last_estimated_robot_pose_y = smartDashboardTable.getEntry("Last Estimated Robot Pose Y");
    NetworkTableEntry last_estimated_robot_pose_angle = smartDashboardTable.getEntry("Last Estimated Robot Pose Angle");
    NetworkTableEntry last_estimated_robot_velocity_x = smartDashboardTable.getEntry("Last Estimated Robot Velocity X");
    NetworkTableEntry last_estimated_robot_velocity_y = smartDashboardTable.getEntry("Last Estimated Robot Velocity Y");
    NetworkTableEntry last_estimated_robot_velocity_theta = smartDashboardTable.getEntry("Last Estimated Robot Velocity Theta");
    NetworkTableEntry timestamp = smartDashboardTable.getEntry("Timestamp");

    NetworkTableEntry shooterAngleBias = smartDashboardTable.getEntry("Shooter Angle Bias");


    NetworkTableEntry vision_pose_x = smartDashboardTable.getEntry("Vision Pose X");
    NetworkTableEntry vision_pose_y = smartDashboardTable.getEntry("Vision Pose Y");
    NetworkTableEntry vision_pose_angle = smartDashboardTable.getEntry("Vision Pose Angle");
    NetworkTableEntry vision_pose_time = smartDashboardTable.getEntry("Vision Pose Time");

    NetworkTableEntry predicted_future_pose_x = smartDashboardTable.getEntry("Predicted Future Pose X");
    NetworkTableEntry predicted_future_pose_y = smartDashboardTable.getEntry("Predicted Future Pose Y");
    NetworkTableEntry predicted_future_pose_time = smartDashboardTable.getEntry("Predicted Future Pose Time");


    NetworkTableEntry enabledTable = table.getEntry("enabled");

    NetworkTableEntry processingTable = table.getEntry("processing");
    NetworkTableEntry processingIdTable = table.getEntry("processingid");
    NetworkTableEntry shooterConfigStatusIdEntry = inst.getTable("limelightgui").getEntry("shooterconfigStatusId");

    NetworkTableEntry limelightForcedOn = inst.getTable("limelightgui").getEntry("forceledon");
    NetworkTableEntry limelightCameraTargetHeightOffset = inst.getTable("limelightgui").getEntry("CameraTargetHeightOffset");

    NetworkTableEntry shooterConfigEntry = inst.getTable("limelightgui").getEntry("shooterconfig");
    NetworkTableEntry shooterConfigStatusEntry = inst.getTable("limelightgui").getEntry("shooterconfigStatus");

    NetworkTableEntry LimelightCameraYAngle = inst.getTable("limelightgui").getEntry("CameraYAngle");
    NetworkTable limelightTable = inst.getTable("limelight");

    NetworkTableEntry adjustedGoalPosX = smartDashboardTable.getEntry("Calculated Target X");
    NetworkTableEntry adjustedGoalPosY = smartDashboardTable.getEntry("Calculated Target Y");


    /**
     * get the adjusted gaol pos in meters
     */
    public Vector2 getAdjustedGoalPos() {
        return new Vector2((float) adjustedGoalPosX.getDouble(0), (float) adjustedGoalPosY.getDouble(0));
    }

    public boolean isEnabled() {
        return enabled;
    }

    private boolean enabled = false;
    private double lastProcessingId = 0;

    private NetworkTablesHelper() {

    }

    public static NetworkTablesHelper getInstance() {
        return networkTablesInstance;
    }

    public void start() {
        inst.startClientTeam(
                AutoBuilder.getConfig().getTeamNumber());  // where TEAM=190, 294, etc, or use inst.startClient("hostname") or
        // similar
        //inst.startDSClient();  // recommended if running on DS computer; this gets the robot IP from the DS
    }


    private static final Color LIGHT_GREEN = Color.valueOf("8FEC8F");


    public void pushAutoData(List<AbstractGuiItem> guiItemList) {
        FileHandler.save();
        if (inst.isConnected()) {
            try {
                String autonomousString = Serializer.serializeToString(
                        GuiSerializer.serializeAutonomousForDeployment(guiItemList));
                autoPath.setString(autonomousString);
                Autonomous autonomous = Serializer.deserializeAuto(autoPath.getString(null));
                System.out.println("Sent Data: " + autonomous);

                NotificationHandler.addNotification(new Notification(LIGHT_GREEN, "Auto Uploaded", 2000));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                NotificationHandler.addNotification(new Notification(Color.RED, "Auto Failed to Upload", 2000));
            } catch (NotDeployableException e) {
                NotificationHandler.addNotification(
                        new Notification(Color.RED, "Your autonomous contains errors: Cannot deploy!", 2000));
            }
        } else {
            System.out.println("Cannot Send Data; Not Connected");
            NotificationHandler.addNotification(new Notification(Color.RED, "Auto Failed to Upload: NOT CONNECTED", 2000));
        }
    }

    private double lastVisionPoseTime = 0;
    private double lastPredictedPoseTime = 0;

    public void updateNT() {
        if (inst.isConnected()) {
            double time = timestamp.getDouble(0);
            if (!enabledTable.getBoolean(false)) {
                enabled = false;
            }

            if (enabledTable.getBoolean(false) && !enabled) {
                robotPositions.clear();
                enabled = true;
            }

            if (robotPositions.size() < 1 || time != robotPositions.get(robotPositions.size() - 1).get(0).time) {
                List<RobotPosition> poses = new ArrayList<>(3);
                poses.add(new RobotPosition(
                        last_estimated_robot_pose_x.getDouble(0),
                        last_estimated_robot_pose_y.getDouble(0),
                        last_estimated_robot_pose_angle.getDouble(0),
                        last_estimated_robot_velocity_x.getDouble(0),
                        last_estimated_robot_velocity_y.getDouble(0),
                        last_estimated_robot_velocity_theta.getDouble(0),
                        timestamp.getDouble(0),
                        "Last Estimated Robot Position"
                ));

                if (time - vision_pose_time.getDouble(0) < 0.5) {
                    poses.add(new RobotPosition(
                            vision_pose_x.getDouble(0),
                            vision_pose_y.getDouble(0),
                            (float) vision_pose_angle.getDouble(0),
                            0, 0, 0,
                            vision_pose_time.getDouble(0),
                            "Vision Position"
                    ));
                    lastVisionPoseTime = vision_pose_time.getDouble(0);
                }

                if (time - predicted_future_pose_time.getDouble(0) < 0.5) {
                    poses.add(new RobotPosition(
                            predicted_future_pose_x.getDouble(0),
                            predicted_future_pose_y.getDouble(0),
                            last_estimated_robot_pose_angle.getDouble(0),
                            0, 0, 0,
                            predicted_future_pose_time.getDouble(0),
                            "Predicted Future Position"
                    ));
                    lastPredictedPoseTime = predicted_future_pose_time.getDouble(0);
                }

                robotPositions.add(poses);
            }


            //Check for the roborio processing notification
            if (processingIdTable.getDouble(0) != lastProcessingId) {
                lastProcessingId = processingIdTable.getDouble(0);
                if (processingTable.getDouble(0) == 1) {
                    NotificationHandler.addNotification(
                            new Notification(Color.CORAL, "The Roborio has started deserializing the auto", 1500));
                } else if (lastProcessingId == 2) {
                    NotificationHandler.addNotification(
                            new Notification(LIGHT_GREEN, "The Roborio has finished deserializing the auto", 1500));
                } else {
                    NotificationHandler.addNotification(
                            new Notification(LIGHT_GREEN, "The Roborio has set: " + processingTable.getDouble(0), 1500));
                }
            }
        }
    }


    public void setLimelightForcedOn(boolean forcedOn) {
        limelightForcedOn.setBoolean(forcedOn);
    }

    public boolean isTargetVisible() {
        return limelightTable.getEntry("tv").getDouble(0) == 1;
    }

    public void setShooterConfig(ShooterConfig shooterConfig) {
        try {
            this.shooterConfigEntry.setString(Serializer.serializeToString(shooterConfig));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getShooterConfigStatusId() {
        return shooterConfigStatusIdEntry.getDouble(-1);
    }

    public double getShooterConfigStatus() {
        return shooterConfigStatusEntry.getDouble(-1);
    }

    /**
     * @return Horizontal Offset From Crosshair To Target (LL1: -27 degrees to 27 degrees | LL2: -29.8 to 29.8 degrees)
     */
    public double getLimelightHorizontalOffset() {
        return limelightTable.getEntry("tx").getDouble(0);
    }

    /**
     * @return Vertical Offset From Crosshair To Target (LL1: -20.5 degrees to 20.5 degrees | LL2: -24.85 to 24.85 degrees)
     */
    public double getLimelightVerticalOffset() {
        return limelightTable.getEntry("ty").getDouble(0);
    }


    NetworkTableEntry visionDistanceOffset = NetworkTableInstance.getDefault().getEntry("VisionDistanceOffset");

    /**
     * @return Distance from the limelight to the target in cm
     * @see <a href="https://docs.limelightvision.io/en/latest/cs_estimating_distance.html">...</a>
     */
    public double getDistance() {
        return smartDashboardTable.getEntry("Shooter Distance to Target").getDouble(-1);
    }

    public double getShooterRPM() {
        return smartDashboardTable.getEntry("Shooter Flywheel Speed").getDouble(-1);
    }

    public double getHoodAngle() {
        return smartDashboardTable.getEntry("Hood Angle").getDouble(-1);
    }

    public ArrayList<List<RobotPosition>> getRobotPositions() {
        return robotPositions;
    }

    public boolean isConnected() {
        return inst.isConnected();
    }

    public double getShooterAngleBias() {
        return shooterAngleBias.getDouble(0);
    }
}