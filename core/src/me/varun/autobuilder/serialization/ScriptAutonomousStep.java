package me.varun.autobuilder.serialization;

public class ScriptAutonomousStep extends AbstractAutonomousStep{

    private final String script;

    public ScriptAutonomousStep(String script) {
        this.script = script;
    }

    @Override
    public void execute() {

    }

    @Override
    public String toString() {
        return "ScriptAutonomousStep{" +
                "script='" + script + '\'' +
                '}';
    }
}
