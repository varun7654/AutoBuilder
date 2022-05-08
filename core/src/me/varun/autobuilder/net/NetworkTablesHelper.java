package me.varun.autobuilder.net;

import com.badlogic.gdx.graphics.Color;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import me.varun.autobuilder.AutoBuilder;
import me.varun.autobuilder.config.gui.FileHandler;
import me.varun.autobuilder.gui.hud.HudElement;
import me.varun.autobuilder.gui.hud.HudRenderer;
import me.varun.autobuilder.gui.notification.Notification;
import me.varun.autobuilder.gui.notification.NotificationHandler;
import me.varun.autobuilder.gui.path.AbstractGuiItem;
import me.varun.autobuilder.gui.shooter.ShooterConfig;
import me.varun.autobuilder.pathing.RobotPosition;
import me.varun.autobuilder.serialization.path.Autonomous;
import me.varun.autobuilder.serialization.path.GuiSerializer;
import me.varun.autobuilder.serialization.path.NotDeployableException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public final class NetworkTablesHelper {

    private static final NetworkTablesHelper networkTablesInstance = new NetworkTablesHelper();
    private final List<List<RobotPosition>> robotPositions = Collections.synchronizedList(new ArrayList<>());
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable autoData = inst.getTable("autodata");
    NetworkTableEntry autoPath = autoData.getEntry("autoPath");
    NetworkTable smartDashboardTable = inst.getTable("SmartDashboard");

    NetworkTableEntry processingTable = autoData.getEntry("processing");
    NetworkTableEntry shooterConfigStatusIdEntry = inst.getTable("limelightgui").getEntry("shooterconfigStatusId");

    NetworkTableEntry limelightForcedOn = inst.getTable("limelightgui").getEntry("forceledon");

    NetworkTableEntry shooterConfigEntry = inst.getTable("limelightgui").getEntry("shooterconfig");
    NetworkTableEntry shooterConfigStatusEntry = inst.getTable("limelightgui").getEntry("shooterconfigStatus");

    NetworkTableEntry hudElementsEntry = autoData.getEntry("hudElements");

    NetworkTableEntry enabledTable = autoData.getEntry("enabled");

    NetworkTableEntry robotPositionsEntry = autoData.getEntry("robotPositions");

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    private NetworkTablesHelper() {

    }

    public static NetworkTablesHelper getInstance() {
        return networkTablesInstance;
    }

    public void start(HudRenderer hudRenderer) {
        inst.startClientTeam(AutoBuilder.getConfig().getTeamNumber());
        enabledTable.addListener(entryNotification -> {
            if (entryNotification.getEntry().getBoolean(false)) {
                if (!enabled) {
                    enabled = true;
                    robotPositions.clear();
                }
            } else {
                enabled = false;
            }
        }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate);

        robotPositionsEntry.addListener(entryNotification -> {
            @Nullable String positions = robotPositionsEntry.getString(null);
            if (positions != null) {
                String[] positionsArray = positions.split(";");
                List<RobotPosition> positionsList = new ArrayList<>(positionsArray.length);
                for (String s : positionsArray) {
                    RobotPosition robotPosition = RobotPosition.fromString(s);
                    if (robotPosition != null) {
                        positionsList.add(robotPosition);
                    }
                }
                positionsList.sort((o1, o2) -> {
                    if (o1.name.equals("Robot Position")) { // Always put robot position first
                        return -1;
                    } else if (o2.name.equals("Robot Position")) {
                        return 1;
                    } else {
                        return o1.name.compareTo(o2.name);
                    }
                });
                robotPositions.add(positionsList);
            }
        }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate);

        processingTable.addListener(entryNotification -> {
            double processingId = entryNotification.getEntry().getDouble(0);
            if (processingId == 1) {
                NotificationHandler.addNotification(
                        new Notification(Color.CORAL, "The Roborio has started deserializing the auto", 1500));
            } else if (processingId == 2) {
                NotificationHandler.addNotification(
                        new Notification(LIGHT_GREEN, "The Roborio has finished deserializing the auto", 1500));
            } else {
                NotificationHandler.addNotification(
                        new Notification(LIGHT_GREEN, "The Roborio has set: " + processingId, 1500));
            }
        }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate);

        hudElementsEntry.addListener(entryNotification -> {
            @Nullable String hudElementsString = entryNotification.getEntry().getString(null);
            if (hudElementsString != null) {
                String[] hudElementsStringArray = hudElementsString.split(";");

                List<HudElement> hudElements = new ArrayList<>(hudElementsStringArray.length);
                for (String s : hudElementsStringArray) {
                    HudElement hudElement = HudElement.fromString(s);
                    hudElements.add(hudElement);
                }

                hudRenderer.setHudElements(hudElements);
            }
        }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate);
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


    public void setLimelightForcedOn(boolean forcedOn) {
        limelightForcedOn.setBoolean(forcedOn);
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
     * @return Distance from the limelight to the target in cm
     * @see <a href="https://docs.limelightvision.io/en/latest/cs_estimating_distance.html">...</a>
     */
    public double getDistance() {
        return smartDashboardTable.getEntry("Shooter Distance to Target").getDouble(-1);
    }

    /**
     * It is imperative that the user manually synchronize on the returned list when traversing it via {@link Iterator}, {@link
     * Spliterator} or {@link Stream}:
     * <pre>
     *  List list = Collections.synchronizedList(new ArrayList());
     *      ...
     *  synchronized (list) {
     *      Iterator i = list.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * @return A list of robot positions
     */
    public List<List<RobotPosition>> getRobotPositions() {
        return robotPositions;
    }

    public boolean isConnected() {
        return inst.isConnected();
    }
}