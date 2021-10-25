package me.varun.autobuilder;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Config {
    ArrayList<String> scriptMethods;
    String selectedAuto;

    @JsonCreator
    public Config(@JsonProperty(required = true, value = "scriptmethods") List<String> scriptMethods,
                  @JsonProperty(required = true, value = "selectedAuto") String selectedAuto){
        this.scriptMethods = (ArrayList<String>) scriptMethods;
        this.selectedAuto = selectedAuto;
    }

    public Config(@JsonProperty(required = true, value = "scriptmethods") List<String> scriptMethods){
        this.scriptMethods = (ArrayList<String>) scriptMethods;
        selectedAuto = "data.json";
    }

    @JsonProperty("scriptmethods")
    public ArrayList<String> getScriptMethods(){
        return scriptMethods;
    }

    @JsonProperty("selectedAuto")
    public String getSelectedAuto(){
        return selectedAuto;
    }
}
