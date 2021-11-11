package me.varun.autobuilder.net;

import com.badlogic.gdx.graphics.Color;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.notification.Notification;
import me.varun.autobuilder.gui.notification.NotificationHandler;
import me.varun.autobuilder.gui.shooter.ShooterConfig;
import me.varun.autobuilder.serialization.path.Autonomous;
import me.varun.autobuilder.serialization.path.GuiSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class NetworkTablesHelper {

    private static final float INCHES_PER_METER = 39.3700787f;
    static NetworkTablesHelper networkTablesInstance = new NetworkTablesHelper();
    private final ArrayList<Float[]> robotPositions = new ArrayList<>();
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("autodata");
    NetworkTableEntry autoPath = table.getEntry("autoPath");
    NetworkTable position = table.getSubTable("position");
    NetworkTableEntry xPos = position.getEntry("x");
    NetworkTableEntry yPos = position.getEntry("y");
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
    NetworkTable shooterTable = inst.getTable("shooter");


    private boolean enabled = false;
    private double lastProcessingId = 0;

    private NetworkTablesHelper() {

    }

    public static NetworkTablesHelper getInstance() {
        return networkTablesInstance;
    }

    public void start() {
        inst.startClientTeam(3476);  // where TEAM=190, 294, etc, or use inst.startClient("hostname") or similar
        //inst.startDSClient();  // recommended if running on DS computer; this gets the robot IP from the DS
    }


    private static final Color LIGHT_GREEN = Color.valueOf("8FEC8F");


    public void pushData(List<AbstractGuiItem> guiItemList) {

        if (inst.isConnected()) {
            try {
                String autonomousString = Serializer.serializeToString(GuiSerializer.serializeAutonomousForDeployment(guiItemList));
                autoPath.setString(autonomousString);
                Autonomous autonomous = Serializer.deserializeAuto(autoPath.getString(null));
                System.out.println("Sent Data: " + autonomous);

                NotificationHandler.addNotification(new Notification(LIGHT_GREEN, "Auto Uploaded", 2000 ));

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
                NotificationHandler.addNotification(new Notification(Color.RED, "Auto Failed to Upload", 2000 ));
            }
        } else {
            System.out.println("Cannot Send Data; Not Connected");
            NotificationHandler.addNotification(new Notification(Color.RED, "Auto Failed to Upload: NOT CONNECTED", 2000 ));
        }

    }

    public void updateRobotPath() {
        if (inst.isConnected()) {
            if (enabledTable.getBoolean(false)) {
                if (!enabled) {
                    robotPositions.clear();
                    enabled = true;
                }

                float x = (float) xPos.getDouble(0);
                float y = (float) yPos.getDouble(0);
                if (robotPositions.size() < 1 || (robotPositions.get(robotPositions.size() - 1)[0] != x || robotPositions.get(robotPositions.size() - 1)[1] != y)) {
                    robotPositions.add(new Float[]{x / INCHES_PER_METER, y / INCHES_PER_METER});
                }

            } else {
                enabled = false;
            }

            //Check for the roborio processing notification
            if(processingIdTable.getDouble(0) != lastProcessingId){
                lastProcessingId = processingIdTable.getDouble(0);
                if(processingTable.getDouble(0) == 1){
                    NotificationHandler.addNotification(new Notification(Color.CORAL, "The Roborio has started deserializing the auto", 1500));
                } else if (lastProcessingId == 2){
                    NotificationHandler.addNotification(new Notification(LIGHT_GREEN, "The Roborio has finished deserializing the auto", 1500));
                } else {
                    NotificationHandler.addNotification(new Notification(LIGHT_GREEN, "The Roborio has set: " + processingTable.getDouble(0), 1500));
                }
            }
        }
    }


    public void setLimelightForcedOn(boolean forcedOn){
        limelightForcedOn.setBoolean(forcedOn);
    }

    public boolean isTargetVisiable(){
        if(limelightTable.getEntry("tv").getDouble(0) == 1){
            return true;
        } else {
            return false;
        }
    }

    public void setShooterConfig(ShooterConfig shooterConfig){
        try {
            this.shooterConfigEntry.setString(Serializer.serializeToString(shooterConfig));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getShooterConfigStatusId(){
        return shooterConfigStatusIdEntry.getDouble(-1);
    }

    public double getShooterConfigStatus(){
        return shooterConfigStatusEntry.getDouble(-1);
    }

    /**
     * @return Horizontal Offset From Crosshair To Target (LL1: -27 degrees to 27 degrees | LL2: -29.8 to 29.8 degrees)
     */
    public double getLimelightHorizontalOffset(){
        return limelightTable.getEntry("tx").getDouble(0);
    }

    /**
     * @return Vertical Offset From Crosshair To Target (LL1: -20.5 degrees to 20.5 degrees | LL2: -24.85 to 24.85 degrees)
     */
    public double getLimelightVerticalOffset(){
        return limelightTable.getEntry("ty").getDouble(0);
    }

    /**
     * @see <a href="https://docs.limelightvision.io/en/latest/cs_estimating_distance.html">...</a>
     * @return Distance from the limelight to the target in cm
     */
    public double getDistance(){
        if(isTargetVisiable()){
            return  (limelightCameraTargetHeightOffset.getDouble(0)) /
                    Math.tan(Math.toRadians(LimelightCameraYAngle.getDouble(0) + getLimelightVerticalOffset()));
        } else {
            return -1;//(System.currentTimeMillis() /50d) % 300 ;
        }
    }

    public double getShooterRPM(){
        return shooterTable.getEntry("rpm").getDouble(-1);
    }

    public double getHoodAngle(){
        return shooterTable.getEntry("hoodangle").getDouble(-1);
    }


    public ArrayList<Float[]> getRobotPositions() {
        return robotPositions;
    }
}