package com.dacubeking.autobuilder.gui.net;

import com.badlogic.gdx.graphics.Color;
import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.config.gui.FileHandler;
import com.dacubeking.autobuilder.gui.gui.drawable.*;
import com.dacubeking.autobuilder.gui.gui.hud.HudElement;
import com.dacubeking.autobuilder.gui.gui.hud.HudRenderer;
import com.dacubeking.autobuilder.gui.gui.notification.Notification;
import com.dacubeking.autobuilder.gui.gui.notification.NotificationHandler;
import com.dacubeking.autobuilder.gui.gui.path.AbstractGuiItem;
import com.dacubeking.autobuilder.gui.gui.shooter.ShooterConfig;
import com.dacubeking.autobuilder.gui.pathing.RobotPosition;
import com.dacubeking.autobuilder.gui.serialization.path.Autonomous;
import com.dacubeking.autobuilder.gui.serialization.path.GuiSerializer;
import com.dacubeking.autobuilder.gui.serialization.path.NotDeployableException;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public final class NetworkTablesHelper {
    private final List<List<RobotPosition>> robotPositions = Collections.synchronizedList(new ArrayList<>());
    private NetworkTableInstance inst;
    private NetworkTable autoData;
    private NetworkTableEntry autoPath;
    private NetworkTable smartDashboardTable;

    private NetworkTableEntry processingTable;
    private NetworkTableEntry shooterConfigStatusIdEntry;

    private NetworkTableEntry limelightForcedOn;

    private NetworkTableEntry shooterConfigEntry;
    private NetworkTableEntry shooterConfigStatusEntry;

    private NetworkTableEntry hudElementsEntry;
    private NetworkTableEntry drawablesEntry;

    private NetworkTableEntry enabledTable;

    private NetworkTableEntry robotPositionsEntry;
    private NetworkTableEntry distanceEntry;

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    private NetworkTablesHelper() {
    }


    private static NetworkTablesHelper networkTablesInstance;
    private static final ReentrantReadWriteLock networkTablesInstanceLock = new ReentrantReadWriteLock();

    public static NetworkTablesHelper getInstance() {
        networkTablesInstanceLock.readLock().lock();
        try {
            if (networkTablesInstance == null) {
                networkTablesInstanceLock.readLock().unlock();
                networkTablesInstanceLock.writeLock().lock();
                try {
                    if (networkTablesInstance == null) {
                        networkTablesInstance = new NetworkTablesHelper();
                    }
                } finally {
                    networkTablesInstanceLock.writeLock().unlock();
                }
                networkTablesInstanceLock.readLock().lock();
            }
            return networkTablesInstance;
        } finally {
            networkTablesInstanceLock.readLock().unlock();
        }
    }

    public void start(HudRenderer hudRenderer, @NotNull DrawableRenderer drawableRenderer) {
        new Thread(() -> {
            inst = NetworkTableInstance.getDefault();
            autoData = inst.getTable("autodata");
            autoPath = autoData.getEntry("autoPath");
            smartDashboardTable = inst.getTable("SmartDashboard");
            processingTable = autoData.getEntry("processing");
            shooterConfigStatusIdEntry = inst.getTable("limelightgui").getEntry("shooterconfigStatusId");
            limelightForcedOn = inst.getTable("limelightgui").getEntry("forceledon");
            shooterConfigEntry = inst.getTable("limelightgui").getEntry("shooterconfig");
            shooterConfigStatusEntry = inst.getTable("limelightgui").getEntry("shooterconfigStatus");
            hudElementsEntry = NetworkTableInstance.getDefault().getEntry("autodata/hudElements");
            drawablesEntry = NetworkTableInstance.getDefault().getEntry("autodata/drawables");
            enabledTable = autoData.getEntry("enabled");
            robotPositionsEntry = NetworkTableInstance.getDefault().getEntry("autodata/robotPositions");
            distanceEntry = smartDashboardTable.getEntry("Shooter Distance to Target");


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
            }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate | EntryListenerFlags.kLocal);

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
                        if (o1.name().equals("Robot Position")) { // Always put robot position first
                            return -1;
                        } else if (o2.name().equals("Robot Position")) {
                            return 1;
                        } else {
                            return o1.name().compareTo(o2.name());
                        }
                    });
                    robotPositions.add(positionsList);
                    AutoBuilder.requestRendering();
                }
            }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate | EntryListenerFlags.kLocal);

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
            }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate | EntryListenerFlags.kLocal);

            hudElementsEntry.addListener(entryNotification -> {
                @Nullable String hudElementsString = entryNotification.getEntry().getString(null);
                if (hudElementsString != null) {
                    String[] hudElementsStringArray = hudElementsString.split(";");

                    List<HudElement> hudElements = new ArrayList<>(hudElementsStringArray.length);
                    for (String s : hudElementsStringArray) {
                        HudElement hudElement = HudElement.fromString(s);
                        if (hudElement != null) {
                            hudElements.add(hudElement);
                        }
                    }

                    hudRenderer.setHudElements(hudElements);
                }
            }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate | EntryListenerFlags.kLocal);

            drawablesEntry.addListener(entryNotification -> {
                @Nullable String[] drawablesString = entryNotification.getEntry().getStringArray(null);
                if (drawablesString != null) {
                    ArrayList<Drawable> drawables = new ArrayList<>(drawablesString.length);
                    for (String s : drawablesString) {
                        switch (s.charAt(0)) {
                            case 'R' -> drawables.add(Rectangle.fromString(s));
                            case 'P' -> drawables.add(Path.fromString(s));
                            case 'C' -> drawables.add(Circle.fromString(s));
                            case 'L' -> drawables.add(Line.fromString(s));
                        }
                    }
                    drawableRenderer.setDrawables(drawables);
                    AutoBuilder.requestRendering();
                }
            }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate | EntryListenerFlags.kLocal);

            if (AutoBuilder.getInstance().shooterGui != null) { // Only add the listeners if the shooter gui is enabled
                smartDashboardTable.getEntry("Shooter Distance to Target").addListener(
                        entryNotification -> AutoBuilder.requestRendering(),
                        EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kImmediate | EntryListenerFlags.kLocal
                );
            }
        }).start();
    }


    private static final Color LIGHT_GREEN = Color.valueOf("8FEC8F");


    public void pushAutoData(List<AbstractGuiItem> guiItemList) {
        FileHandler.saveAuto(true);
        if (inst != null && inst.isConnected()) {
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
        if (limelightForcedOn != null) limelightForcedOn.setBoolean(forcedOn);
    }

    public void setShooterConfig(ShooterConfig shooterConfig) {
        try {
            if (shooterConfigEntry != null) shooterConfigEntry.setString(Serializer.serializeToString(shooterConfig));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getShooterConfigStatusId() {
        if (shooterConfigStatusIdEntry != null) return shooterConfigStatusIdEntry.getDouble(-1);
        return -1;
    }

    public double getShooterConfigStatus() {
        if (shooterConfigStatusEntry != null) return shooterConfigStatusEntry.getDouble(-1);
        return -1;
    }

    /**
     * @return Distance from the limelight to the target in cm
     */
    public double getDistance() {
        if (distanceEntry != null) return distanceEntry.getDouble(-1);
        return -1;
    }

    /**
     * It is imperative that the user manually synchronize on the returned list when traversing it via {@link Iterator},
     * {@link Spliterator} or {@link Stream}:
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
        if (inst == null) return false;
        return inst.isConnected();
    }
}