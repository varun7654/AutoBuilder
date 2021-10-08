package me.varun.autobuilder;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Config {


    ArrayList<String> scriptMethods;
    @JsonCreator
    public Config(@JsonProperty(required = true, value = "scriptmethods") List<String> scriptMethods){
        this.scriptMethods = (ArrayList<String>) scriptMethods;
    }

    @JsonProperty("scriptmethods")
    public ArrayList<String> getScriptMethods(){
        return scriptMethods;

    }
}
