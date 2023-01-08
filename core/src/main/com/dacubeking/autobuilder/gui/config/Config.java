package com.dacubeking.autobuilder.gui.config;


import com.dacubeking.autobuilder.gui.AutoBuilder;
import com.dacubeking.autobuilder.gui.RenderEvents;
import com.fasterxml.jackson.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private List<String> scriptMethods;
    private String selectedAutoFile;
    private String shooterConfigFile;
    private String teamNumber;
    private float robotLength;
    private float robotWidth;
    private float pointScaleFactor;
    private float originX;
    private float originY;
    private PathingConfig pathingConfig;
    private boolean networkTablesEnabled;
    private String robotCodeDataFile;
    private boolean reflectionEnabled;
    private boolean isHolonomic;
    private boolean shooterConfigEnabled;

    @JsonCreator
    public Config(@JsonProperty(value = "scriptMethods") List<String> scriptMethods,
                  @JsonProperty(value = "selectedAuto") String selectedAuto,
                  @JsonProperty(defaultValue = "shooterconfig.json", value = "selectedShooterConfig") String shooterConfig,
                  @JsonProperty(value = "teamNumber") Object teamNumber,
                  @JsonProperty(value = "robotLength") Float robotLength,
                  @JsonProperty(value = "robotWidth") Float robotWidth,
                  @JsonProperty(value = "pointScaleFactor") Float pointScaleFactor,
                  @JsonProperty(value = "originX") Float originX,
                  @JsonProperty(value = "originY") Float originY,
                  @JsonProperty(value = "pathingConfig") PathingConfig pathingConfig,
                  @JsonProperty(value = "networkTablesEnabled") Boolean networkTablesEnabled,
                  @JsonProperty(value = "robotCodeDataFile") String robotCodeDataFile,
                  @JsonProperty("useReflection") Boolean reflectionEnabled,
                  @JsonProperty("isHolonomic") Boolean isHolonomic,
                  @JsonProperty(value = "shooterConfigEnabled", defaultValue = "false") Boolean shooterConfigEnabled) {
        if (scriptMethods == null) {
            this.scriptMethods = new ArrayList<>();
            this.scriptMethods.addAll(List.of("print", "sleep"));
        } else {
            this.scriptMethods = scriptMethods;
        }

        this.selectedAutoFile = shooterConfig == null ? "auto.json" : selectedAuto;
        this.shooterConfigFile = shooterConfig == null ? "shooterconfig.json" : shooterConfig;
        if (teamNumber == null) {
            this.teamNumber = "3476";
        } else if (teamNumber instanceof Integer) {
            this.teamNumber = String.valueOf(teamNumber);
        } else {
            this.teamNumber = (String) teamNumber;
        }
        this.robotLength = robotLength == null ? 0.9191625f : robotLength;
        this.robotWidth = robotWidth == null ? 0.9229725f : robotWidth;
        this.pointScaleFactor = pointScaleFactor == null ? 159.83f : pointScaleFactor;
        this.originX = originX == null ? -598f : originX;
        this.originY = originY == null ? -1080f : originY;
        this.pathingConfig = pathingConfig == null ? new PathingConfig() : pathingConfig;
        this.networkTablesEnabled = networkTablesEnabled == null ? true : networkTablesEnabled;
        this.robotCodeDataFile = robotCodeDataFile == null ? "robotCodeData.json" : robotCodeDataFile;
        this.reflectionEnabled = reflectionEnabled == null ? true : reflectionEnabled;
        this.isHolonomic = isHolonomic == null ? true : isHolonomic;
        this.shooterConfigEnabled = shooterConfigEnabled == null ? false : shooterConfigEnabled;
    }

    public Config() {
        this(null, null, null, "3476", null, null, null, null, null, null, null, null, null, null, null);
    }

    @JsonProperty("scriptMethods")
    public synchronized List<String> getScriptMethods() {
        return scriptMethods;
    }

    @JsonProperty("selectedAuto")
    public synchronized String getSelectedAuto() {
        return selectedAutoFile;
    }

    @JsonIgnore
    public synchronized File getAutoPath() {
        File path = new File(getSelectedAuto());
        if (!path.isAbsolute()) {
            path = new File(AutoBuilder.USER_DIRECTORY + "/" + path);
        }

        if (path.exists()) {
            return path;
        } else {
            return new File(path.getParentFile().getAbsolutePath() + "/NOTDEPLOYABLE" + new File(getSelectedAuto()).getName());
        }
    }

    @JsonIgnore
    public synchronized File getShooterConfigPath() {
        File path = new File(getSelectedShooterConfig());
        if (!path.isAbsolute()) {
            path = new File(AutoBuilder.USER_DIRECTORY + "/" + path);
        }
        return path;
    }

    @JsonProperty("selectedShooterConfig")
    public synchronized String getSelectedShooterConfig() {
        return shooterConfigFile;
    }

    @JsonProperty("teamNumber")
    public synchronized String getTeamNumber() {
        return teamNumber;
    }

    @JsonProperty("robotLength")
    public synchronized float getRobotLength() {
        return robotLength;
    }

    @JsonProperty("robotWidth")
    public synchronized float getRobotWidth() {
        return robotWidth;
    }

    @JsonProperty("pointScaleFactor")
    public synchronized float getPointScaleFactor() {
        return pointScaleFactor;
    }

    @JsonProperty("originX")
    public synchronized float getOriginX() {
        return originX;
    }

    @JsonProperty("originY")
    public synchronized float getOriginY() {
        return originY;
    }

    @JsonProperty("pathingConfig")
    public synchronized PathingConfig getPathingConfig() {
        return pathingConfig;
    }

    @JsonProperty("networkTablesEnabled")
    public synchronized boolean isNetworkTablesEnabled() {
        return networkTablesEnabled;
    }

    @JsonProperty("robotCodeDataFile")
    public synchronized String getRobotCodeDataFile() {
        return robotCodeDataFile;
    }

    @JsonProperty("useReflection")
    public synchronized boolean getReflectionEnabled() {
        return reflectionEnabled;
    }

    @JsonProperty("isHolonomic")
    public synchronized boolean isHolonomic() {
        return isHolonomic;
    }

    @JsonProperty("shooterConfigEnabled")
    public synchronized boolean isShooterConfigEnabled() {
        return shooterConfigEnabled;
    }

    @JsonProperty("readMe")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private synchronized String getReadMe() {
        return "the scriptMethods contains the list of valid methods that will be allowed in the script block. " +
                "print and sleep are currently hardcoded to allow for error checking on the arguments. " +
                "You to edit them you will need to clone & compile the code. You can find them at " +
                "src/me/varun/autobuilder/scripting/parser. " +
                "selectedAuto & selectedShooterConfig point to the files that the code will read to get and save data. " +
                "The team number is used for connect to your robot though network tables. " +
                "The robot length and width are in meters and is used to draw the blue box when clicking on a point. " +
                "The point scale factor is calculated by getting the (length and pixels of the field in the image)/(length of " +
                "the field in meters). " +
                "It is used to render everything in the correct position. " +
                "The units for the origin is in pixels. It represents how much the image should be translated so that (0,0) " +
                "in the application space is (0,0) on the image. " +
                "You can disable network tables by setting networkTablesEnabled to false. This is useful if you are using the " +
                "app while not connected to the robot and are getting lag spikes/errors in the console";
    }

    @Override
    public synchronized String toString() {
        return "Config{" +
                "scriptMethods=" + scriptMethods +
                ", selectedAutoFile='" + selectedAutoFile + '\'' +
                ", shooterConfigFile='" + shooterConfigFile + '\'' +
                ", teamNumber=" + teamNumber +
                ", robotLength=" + robotLength +
                ", robotWidth=" + robotWidth +
                ", pointScaleFactor=" + pointScaleFactor +
                ", originX=" + originX +
                ", originY=" + originY +
                ", pathingConfig=" + pathingConfig +
                ", networkTablesEnabled=" + networkTablesEnabled +
                ", robotCodeDataFile='" + robotCodeDataFile + '\'' +
                ", reflectionEnabled=" + reflectionEnabled +
                ", isHolonomic=" + isHolonomic +
                '}';
    }

    public synchronized void setConfig(Config config) {
        if (this.pointScaleFactor != config.pointScaleFactor
                || this.originX != config.originX
                || this.originY != config.originY) {
            RenderEvents.fireRenderCacheDeletionEvent();
        }


        this.scriptMethods = config.scriptMethods;
        this.selectedAutoFile = config.selectedAutoFile;
        this.shooterConfigFile = config.shooterConfigFile;
        this.teamNumber = config.teamNumber;
        this.robotLength = config.robotLength;
        this.robotWidth = config.robotWidth;
        this.pointScaleFactor = config.pointScaleFactor;
        this.originX = config.originX;
        this.originY = config.originY;
        this.pathingConfig = config.pathingConfig;
        this.networkTablesEnabled = config.networkTablesEnabled;
        this.robotCodeDataFile = config.robotCodeDataFile;
        this.reflectionEnabled = config.reflectionEnabled;
        this.isHolonomic = config.isHolonomic;
        this.shooterConfigEnabled = config.shooterConfigEnabled;
    }

    public synchronized void setAuto(String auto) {
        this.selectedAutoFile = auto;
    }

    public synchronized void setTeamNumber(String teamNumber) {
        this.teamNumber = teamNumber;
    }

    public synchronized void setRobotLength(float robotLength) {
        this.robotLength = robotLength;
    }

    public synchronized void setRobotWidth(float robotWidth) {
        this.robotWidth = robotWidth;
    }

    public synchronized void setPointScaleFactor(float pointScaleFactor) {
        this.pointScaleFactor = pointScaleFactor;
        RenderEvents.fireRenderCacheDeletionEvent();
    }

    public synchronized void setOriginX(float originX) {
        this.originX = originX;
        RenderEvents.fireRenderCacheDeletionEvent();
    }

    public synchronized void setOriginY(float originY) {
        this.originY = originY;
        RenderEvents.fireRenderCacheDeletionEvent();
    }

    public synchronized void setHolonomic(boolean isHolonomic) {
        this.isHolonomic = isHolonomic;
    }

    public synchronized void setNetworkTablesEnabled(boolean networkTablesEnabled) {
        this.networkTablesEnabled = networkTablesEnabled;
    }

    public synchronized void setPathingConfig(PathingConfig pathingConfig) {
        this.pathingConfig = pathingConfig;
    }

    public synchronized Config copy() {
        return new Config(new ArrayList<>(scriptMethods), selectedAutoFile, shooterConfigFile, teamNumber, robotLength,
                robotWidth, pointScaleFactor, originX, originY,
                new PathingConfig(pathingConfig), networkTablesEnabled, robotCodeDataFile,
                reflectionEnabled, isHolonomic, shooterConfigEnabled);
    }
}
