package com.dacubeking.AutoBuilder.robot.robotinterface;


import com.dacubeking.AutoBuilder.robot.GuiAuto;
import com.dacubeking.AutoBuilder.robot.NetworkAuto;
import com.dacubeking.AutoBuilder.robot.annotations.AutoBuilderAccessible;
import com.dacubeking.AutoBuilder.robot.annotations.AutoBuilderRobotSide;
import com.dacubeking.AutoBuilder.robot.annotations.RequireWait;
import com.google.common.base.Preconditions;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent.Kind;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

@AutoBuilderRobotSide
public final class AutonomousContainer {

    private final @NotNull NetworkTableInstance instance = NetworkTableInstance.getDefault();
    private final @NotNull NetworkTable autoDataTable = instance.getTable("autodata");
    private final @NotNull NetworkTableEntry autoPath = autoDataTable.getEntry("autoPath");
    private final @NotNull NetworkTableEntry pathProcessingStatusEntry = autoDataTable.getEntry("processing");

    private final @NotNull Lock networkAutoLock = new ReentrantLock();
    private @Nullable NetworkAuto networkAuto = null;
    private final @NotNull ExecutorService deserializerExecutor = Executors.newSingleThreadExecutor();

    private Function<TrajectoryBuilderInfo, Command> trajectoryFollowerSupplier;
    private Consumer<Pose2d> initialPoseSetter;

    private static final AutonomousContainer autonomousContainer = new AutonomousContainer();

    public static AutonomousContainer getInstance() {
        return autonomousContainer;
    }

    private AutonomousContainer() {

    }

    private boolean isHolonomic;
    private volatile boolean debugPrints = false;

    private final ConcurrentHashMap<File, GuiAuto> autonomousList = new ConcurrentHashMap<>();
    private final @NotNull Hashtable<String, Object> parentObjects = new Hashtable<>();

    private final @NotNull List<Object> requireWaitObjects = Collections.synchronizedList(new ArrayList<>());
    private static final String AUTO_DIRECTORY = Filesystem.getDeployDirectory().getAbsoluteFile() + "/autos/";

    /**
     * @param isHolonomic                Is the robot using a holonomic drivetrain? (ex: swerve or mecanum)
     * @param trajectoryFollowerSupplier A supplier that returns a command that will follow the trajectory from a
     *                                   {@link TrajectoryBuilderInfo} object.
     * @param initialPoseSetter          A consumer that sets the initial pose of the robot.
     * @param parentObjects              Objects that can be used to access other objects annotated with
     *                                   {@link AutoBuilderAccessible}.
     * @throws NullPointerException If either {@code trajectoryFollowerSupplier} or {@code initialPoseSetter} is null.
     */
    @SuppressWarnings("unused")
    public synchronized void initialize(
            boolean isHolonomic,
            @NotNull Function<TrajectoryBuilderInfo, Command> trajectoryFollowerSupplier,
            @NotNull Consumer<Pose2d> initialPoseSetter,
            Object... parentObjects


    ) {
        Preconditions.checkNotNull(trajectoryFollowerSupplier, "Trajectory follower supplier cannot be null");
        Preconditions.checkNotNull(initialPoseSetter, "Initial pose setter cannot be null");


        initializeAccessibleInstances(parentObjects, true);
        printDebug("Initialized Accessible Instances");


        //Create the listener for network autos
        NetworkTableInstance.getDefault()
                .addListener(autoPath, EnumSet.of(Kind.kValueRemote, Kind.kImmediate, Kind.kProperties), event -> {
                    deserializerExecutor.execute(() -> { //Start deserializing on another thread
                        System.out.println("Starting to Parse Network Autonomous");
                        //Set networktable entries for the gui notifications
                        pathProcessingStatusEntry.setDouble(1);
                        networkAutoLock.lock();
                        try {
                            networkAuto = new NetworkAuto(); //Create the auto object which will start deserializing the json
                            // and the auto
                        } finally {
                            networkAutoLock.unlock();
                        }

                        // ready to be run
                        System.out.println("Done Parsing Network Autonomous");
                        //Set networktable entries for the gui notifications
                        pathProcessingStatusEntry.setDouble(2);
                    });
                });

        this.isHolonomic = isHolonomic;
        this.trajectoryFollowerSupplier = trajectoryFollowerSupplier;
        this.initialPoseSetter = initialPoseSetter;

        long startLoadingTime = System.currentTimeMillis();

        findAutosAndLoadAutos(new File(AUTO_DIRECTORY));
        System.out.println("Found " + numAutonomousFiles + " Autos. Waiting for them to load");

        blockedThread = Thread.currentThread();
        // Wait for all autos to be loaded
        while (loadedAutosCount.get() < numAutonomousFiles) {
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }

        System.out.println("Successfully loaded " + autonomousList.size() + " auto"
                + (autonomousList.size() == 1 ? "" : "s") + " with "
                + (loadedAutosCount.get() - successfullyLoadedAutosCount.get()) +
                " failure" + (loadedAutosCount.get() - successfullyLoadedAutosCount.get() == 1 ? "" : "s") + " in "
                + ((double) (System.currentTimeMillis() - startLoadingTime)) / 1000 + "s");
    }


    private static final Pattern VALID_ALIAS_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

    /**
     * Uses the given parent objects to find all fields/methods/constructors annotated with {@link AutoBuilderAccessible} to store
     * them for use in autos.
     *
     * @param parentObjects Objects that can be used to access other objects annotated with {@link AutoBuilderAccessible}.
     */
    private synchronized void initializeAccessibleInstances(Object[] parentObjects, boolean crashOnError) {
        for (Object parentObject : parentObjects) {
            Arrays.stream(parentObject.getClass().getDeclaredMethods())
                    .filter(method -> Arrays.stream(method.getDeclaredAnnotations())
                            .anyMatch(annotation -> annotation.annotationType() == AutoBuilderAccessible.class))
                    .forEach(method -> {
                        try {
                            method.setAccessible(true);
                            Object instance = method.invoke(parentObject);

                            String name;
                            if (method.getAnnotation(AutoBuilderAccessible.class).alias().isEmpty()) {
                                if (instance.getClass().isAnonymousClass()) {
                                    name = method.getName();
                                } else {
                                    name = instance.getClass().getName();
                                }
                            } else {
                                name = method.getAnnotation(AutoBuilderAccessible.class).alias();
                                if (!VALID_ALIAS_PATTERN.matcher(name).matches()) {
                                    throw new IllegalArgumentException("Invalid alias name: " + name +
                                            ". Alias names must only contain letters, numbers, and underscores");
                                }
                            }

                            if (this.parentObjects.contains(name)) {
                                throw new IllegalStateException(
                                        "There are multiple @AutoBuilderAccessible for the same type/alias\n" +
                                                "Remove the @AutoBuilderAccessible annotation from the method " +
                                                "with the name/alias: " + name);
                            }

                            if (method.isAnnotationPresent(RequireWait.class)) {
                                this.requireWaitObjects.add(instance);
                            }
                            this.parentObjects.put(name, instance);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            DriverStation.reportError("Failed to get accessible instance: " + e.getMessage(), e.getStackTrace());
                            if (crashOnError) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            Arrays.stream(parentObject.getClass().getDeclaredFields())
                    .filter(field -> Arrays.stream(field.getDeclaredAnnotations())
                            .anyMatch(annotation -> annotation.annotationType().isAssignableFrom(AutoBuilderAccessible.class)))
                    .forEach(field -> {
                        try {
                            field.setAccessible(true);
                            Object instance = field.get(parentObject);
                            String name;
                            if (field.getAnnotation(AutoBuilderAccessible.class).alias().isEmpty()) {
                                if (instance.getClass().isAnonymousClass()) {
                                    name = field.getName();
                                } else {
                                    name = instance.getClass().getName();
                                }
                            } else {
                                name = field.getAnnotation(AutoBuilderAccessible.class).alias();
                                if (!VALID_ALIAS_PATTERN.matcher(name).matches()) {
                                    throw new IllegalArgumentException("Invalid alias name: " + name +
                                            ". Alias names must only contain letters, numbers, and underscores");
                                }
                            }

                            if (this.parentObjects.contains(name)) {
                                throw new IllegalStateException(
                                        "There are multiple @AutoBuilderAccessible for the same type/alias\n" +
                                                "Remove the @AutoBuilderAccessible annotation from the field " +
                                                "with the name/alias: " + name);
                            }

                            if (field.isAnnotationPresent(RequireWait.class)) {
                                this.requireWaitObjects.add(instance);
                            }
                            this.parentObjects.put(name, instance);
                        } catch (IllegalAccessException e) {
                            DriverStation.reportError("Failed to get accessible instance: " + e.getMessage(), e.getStackTrace());
                            if (crashOnError) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        }
    }


    /**
     * Recursively finds all autonomous files in the given directory and loads them.
     *
     * @param directory The directory to search in.
     */
    private synchronized void findAutosAndLoadAutos(File directory) {
        File[] autos = directory.listFiles();
        if (autos == null) {
            System.out.println("No autos files found");
        } else {
            for (File file : autos) {
                if (file.isDirectory()) {
                    findAutosAndLoadAutos(file);
                    continue;
                }

                String fileName = file.getName();

                if (fileName.endsWith(".json") || fileName.endsWith(".auto")) {
                    if (file.getName().contains("NOTDEPLOYABLE")) {
                        System.out.println("Skipping " + file.getAbsolutePath() + " because it is marked as NOTDEPLOYABLE");
                        continue;
                    }
                    printDebug("Found auto file: " + file.getAbsolutePath());

                    CompletableFuture.runAsync(() -> {
                        try {
                            autonomousList.put(file, new GuiAuto(file));
                            successfullyLoadedAutosCount.incrementAndGet();
                            printDebug("Successfully loaded auto: " + file.getAbsolutePath());
                        } catch (IOException e) {
                            DriverStation.reportError("Failed to deserialize auto: " + e.getLocalizedMessage(),
                                    e.getStackTrace());
                        }
                    }).thenRun(this::incrementLoadedAutos);

                    numAutonomousFiles++;
                }
            }
        }
    }

    private int numAutonomousFiles = 0;
    private final AtomicInteger successfullyLoadedAutosCount = new AtomicInteger(0);
    private final AtomicInteger loadedAutosCount = new AtomicInteger(0);
    @Nullable private Thread blockedThread = null;

    private void incrementLoadedAutos() {
        printDebug("Loaded Autos: " + loadedAutosCount.incrementAndGet() + "/" + numAutonomousFiles);
        if (blockedThread != null) {
            blockedThread.interrupt();
        }
    }

    @Internal
    public synchronized void isInitialized() {
        Preconditions.checkNotNull(initialPoseSetter, "The Autonomous Container must be initialized before any " +
                "autonomous can be run. (Call initialize(...) first)");
    }

    @Internal
    public synchronized boolean isHolonomic() {
        return isHolonomic;
    }

    @Internal
    public synchronized boolean areDebugPrintsEnabled() {
        return debugPrints;
    }

    @Internal
    public void printDebug(String message) {
        if (debugPrints) {
            System.out.println(message);
        }
    }

    @Internal
    public @NotNull Map<String, Object> getAccessibleInstances() {
        return parentObjects;
    }

    @Internal
    public @NotNull List<Object> getRequireWaitObjects() {
        return requireWaitObjects;
    }

    @SuppressWarnings("unused")
    public void setDebugPrints(boolean debugPrints) {
        this.debugPrints = debugPrints;
    }

    @Internal
    public @NotNull Function<TrajectoryBuilderInfo, Command> getTrajectoryFollowerSupplier() {
        return trajectoryFollowerSupplier;
    }

    @Internal
    public @NotNull Consumer<Pose2d> getInitialPoseSetter() {
        return initialPoseSetter;
    }


    /**
     * @return A list of the names of all the autos that have been loaded. The name is the name of the file, without the .json
     */
    @SuppressWarnings("unused")
    public List<String> getAutonomousNames() {
        ArrayList<String> names = new ArrayList<>(autonomousList.size());
        for (File absoluteFilePath : autonomousList.keySet()) {
            String fileName = absoluteFilePath.getName();
            names.add(fileName.substring(0, fileName.lastIndexOf('.'))); // Strip the file extension
        }
        return names;
    }

    /**
     * @return A list of the absolute paths of all the autos that have been loaded.
     */
    @SuppressWarnings("unused")
    public Set<File> getAbsoluteAutonomousPaths() {
        return autonomousList.keySet();
    }


    /**
     * @param name             The name of the auto to get
     * @param side             The side of the field to get the auto for
     * @param allowNetworkAuto If true, the network auto will be returned if one is available
     * @return The auto with the given name, or null if it is not found
     */
    public synchronized @Nullable GuiAuto getAuto(String name, String side, boolean allowNetworkAuto) {
        @Nullable GuiAuto selectedAuto;
        networkAutoLock.lock();
        try {
            if (allowNetworkAuto && networkAuto != null) {
                selectedAuto = networkAuto;
            } else {
                File autoPath = new File(AUTO_DIRECTORY + side + (side.endsWith("/") ? "" : "/") + name + ".auto");

                if (!autonomousList.containsKey(autoPath)) {
                    autoPath = new File(AUTO_DIRECTORY + name + ".auto"); // Try the name without the side
                }

                // If the auto is still not found, try the json extension
                if (!autonomousList.containsKey(autoPath)) {
                    autoPath = new File(AUTO_DIRECTORY + side + (side.endsWith("/") ? "" : "/") + name + ".json");
                }

                if (!autonomousList.containsKey(autoPath)) {
                    autoPath = new File(AUTO_DIRECTORY + name + ".json"); // Try the name without the side
                }

                selectedAuto = autonomousList.get(autoPath);
            }
        } finally {
            networkAutoLock.unlock();
        }
        return selectedAuto;
    }

    /**
     * Run an auto with the given name and side. These names should match the names given by {@link #getAutonomousNames()}.
     * <p>
     * If the auto is not found with the given side, the code will look for an auto with no side. This means you don't have to
     * perform checks to determine if the auto is sided or not.
     *
     * @param name                    The name of the auto to run.
     * @param side                    The side of the robot that the auto will be run on.
     * @param allowRunningNetworkAuto Weather to allow network autos to be run instead of the selected auto. If a network auto is
     *                                loaded and this is true, it will be run instead of the selected auto.
     */
    @SuppressWarnings("unused")
    public synchronized void runAutonomous(String name, String side, boolean allowRunningNetworkAuto) {
        @Nullable GuiAuto selectedAuto;

        selectedAuto = getAuto(name, side, allowRunningNetworkAuto);

        // If the auto is null, it means that the auto was not found.
        if (selectedAuto == null) {
            DriverStation.reportError("Could not find auto: " + name +
                            "\nExpected to find the auto in:\n" +
                            AUTO_DIRECTORY + side + (side.endsWith("/") ? "" : "/") + name + ".auto" +
                            "\nOr:\n" + AUTO_DIRECTORY + name + ".auto" +
                            "\nOr:\n" + AUTO_DIRECTORY + side + (side.endsWith("/") ? "" : "/") + name + ".json" +
                            "\nOr:\n" + AUTO_DIRECTORY + name + ".json",
                    false);
            return;
        }

        selectedAuto.schedule();
    }

    /**
     * Runs an auto that is located at the given absolute file path.
     *
     * @param file                    The absolute file path of the auto to run.
     * @param allowRunningNetworkAuto Weather to allow network autos to be run instead of the selected auto. If a network auto is
     *                                loaded and this is true, it will be run instead of the selected auto.
     */
    @SuppressWarnings("unused")
    public synchronized void runAutonomous(File file, boolean allowRunningNetworkAuto) {
        @Nullable GuiAuto selectedAuto;
        networkAutoLock.lock();
        try {
            if (allowRunningNetworkAuto && networkAuto != null) {
                selectedAuto = networkAuto;
            } else {
                selectedAuto = autonomousList.get(file);
            }
        } finally {
            networkAutoLock.unlock();
        }

        // If the auto is null, it means that the auto was not found.
        if (selectedAuto == null) {
            DriverStation.reportError("Could not find auto: " + file.getAbsolutePath(), false);
            return;
        }
        selectedAuto.schedule();
    }
}
