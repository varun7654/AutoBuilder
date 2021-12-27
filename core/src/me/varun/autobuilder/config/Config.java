package me.varun.autobuilder.config;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private List<String> scriptMethods;
    private String selectedAutoFile;
    private int teamNumber;
    private float robotLength;
    private float robotWidth;
    private float pointScaleFactor;
    private float originX;
    private float originY;
    private PathingConfig pathingConfig;
    private boolean networkTablesEnabled;
    private String robotCodeDataFile;
    private boolean reflectionEnabled;

    @JsonCreator
    public Config(@JsonProperty(value = "scriptMethods") List<String> scriptMethods,
                  @JsonProperty(value = "selectedAuto") String selectedAuto,
                  @JsonProperty(value = "teamNumber") Integer teamNumber,
                  @JsonProperty(value = "robotLength") Float robotLength,
                  @JsonProperty(value = "robotWidth") Float robotWidth,
                  @JsonProperty(value = "pointScaleFactor") Float pointScaleFactor,
                  @JsonProperty(value = "originX") Float originX,
                  @JsonProperty(value = "originY") Float originY,
                  @JsonProperty(value = "pathingConfig") PathingConfig pathingConfig,
                  @JsonProperty(value = "networkTablesEnabled") Boolean networkTablesEnabled,
                  @JsonProperty(value = "robotCodeDataFile") String robotCodeDataFile,
                  @JsonProperty("useReflection") Boolean reflectionEnabled) {
        if (scriptMethods == null) {
            this.scriptMethods = new ArrayList<>();
            this.scriptMethods.addAll(List.of("print", "sleep"));
        } else {
            this.scriptMethods = scriptMethods;
        }

        this.selectedAutoFile = selectedAuto == null ? "auto.json" : selectedAuto;
        this.teamNumber = teamNumber == null ? 3476 : teamNumber;
        this.robotLength = robotLength == null ? 0.9191625f : robotLength;
        this.robotWidth = robotWidth == null ? 0.9229725f : robotWidth;
        this.pointScaleFactor = pointScaleFactor == null ? 129.7007874015748f : pointScaleFactor;
        this.originX = originX == null ? -422f : originX;
        this.originY = originY == null ? -589f : originY;
        this.pathingConfig = pathingConfig == null ? new PathingConfig() : pathingConfig;
        this.networkTablesEnabled = networkTablesEnabled == null ? true : networkTablesEnabled;
        this.robotCodeDataFile = robotCodeDataFile == null ? "robotCodeData.json" : robotCodeDataFile;
        this.reflectionEnabled = reflectionEnabled == null ? true : reflectionEnabled;
    }

    public Config(){
        this(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @JsonProperty("scriptMethods")
    public List<String> getScriptMethods(){
        return scriptMethods;
    }

    @JsonProperty("selectedAuto")
    public String getSelectedAuto(){
        return selectedAutoFile;
    }

    @JsonProperty("teamNumber")
    public int getTeamNumber(){
        return teamNumber;
    }

    @JsonProperty("robotLength")
    public float getRobotLength(){
        return robotLength;
    }

    @JsonProperty("robotWidth")
    public float getRobotWidth(){
        return robotWidth;
    }

    @JsonProperty("pointScaleFactor")
    public float getPointScaleFactor(){
        return pointScaleFactor;
    }

    @JsonProperty("originX")
    public float getOriginX(){
        return originX;
    }

    @JsonProperty("originY")
    public float getOriginY() {
        return originY;
    }

    @JsonProperty("pathingConfig")
    public PathingConfig getPathingConfig() {
        return pathingConfig;
    }

    @JsonProperty("networkTablesEnabled")
    public boolean isNetworkTablesEnabled() {
        return networkTablesEnabled;
    }

    @JsonProperty("robotCodeDataFile")
    public String getRobotCodeDataFile() {
        return robotCodeDataFile;
    }

    @JsonProperty("useReflection")
    public boolean getReflectionEnabled() {
        return reflectionEnabled;
    }

    @JsonProperty("readMe")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String getReadMe() {
        return "the scriptMethods contains the list of valid methods that will be allowed in the script block. " +
                "print and sleep are currently hardcoded to allow for error checking on the arguments. " +
                "You to edit them you will need to clone & compile the code. You can find them at src/me/varun/autobuilder/scripting/parser. " +
                "selectedAuto point to the files that the code will read to get and save data. " +
                "The team number is used for connect to your robot though network tables. " +
                "The robot length and width are in meters and is used to draw the blue box when clicking on a point. " +
                "The point scale factor is calculated by getting the (length and pixels of the field in the image)/(length of the field in meters). " +
                "It is used to render everything in the correct position. " +
                "The units for the origin is in pixels. It represents how much the image should be translated so that (0,0) " +
                "in the application space is (0,0) on the image. " +
                "You can disable network tables by setting networkTablesEnabled to false. This is useful if you are using the " +
                "app while not connected to the robot and are getting lag spikes/errors in the console";
    }
}
