package me.varun.autobuilder;


import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Config {
    private final ArrayList<String> scriptMethods;
    private final String selectedAuto;
    private String shooterConfig;
    private final File autoDirectoryLocation;
    private final String storageLocation;

    @JsonCreator
    public Config(@JsonProperty(value = "scriptmethods", defaultValue = "\"print\",\"deployIntake\",\"undeployIntake\",\"intakeOn\",\"intakeOff\",\"intakeReverse\",\"snailOn\",\"snailOff\",\"snailReverse\",\"frontActive\",\"frontInactive\",\"frontReverse\",\"visionIdle\",\"visionWin\",\"visionAim\",\"setShooterSpeed\",\"fireShooter\",\"stopFiringShooter\",\"shootBalls\",\"turnOnIntakeTrack\",\"turnOffIntakeTrack\"")
                              List<String> scriptMethods,
                  @JsonProperty(value = "selectedAuto", defaultValue = "data.json") String selectedAuto,
                  @JsonProperty(value = "selectedShooterConfig", defaultValue = "shooterconfig.json") String shooterConfig,
                  @JsonProperty(value = "storageLocation", defaultValue = "/AppData/Roaming/AutoBuilder/config.json") String storageLocation){
        this.scriptMethods = (ArrayList<String>) scriptMethods;
        this.selectedAuto = selectedAuto;
        this.shooterConfig = shooterConfig;
        this.autoDirectoryLocation = new File(Gdx.files.getExternalStoragePath() + storageLocation);
        this.storageLocation = storageLocation;
    }

    @JsonProperty("scriptmethods")
    public ArrayList<String> getScriptMethods(){
        return scriptMethods;
    }

    @JsonProperty("storageLocation")
    public String getStorageLocation(){
        return storageLocation;
    }


    public File getAutoDirectory(){
        return autoDirectoryLocation;
    }

    @JsonProperty("selectedAuto")
    public String getSelectedAuto(){
        return selectedAuto;
    }

    @JsonProperty("selectedShooterConfig")
    public String getSelectedShooterConfig(){
        return shooterConfig;
    }
}
