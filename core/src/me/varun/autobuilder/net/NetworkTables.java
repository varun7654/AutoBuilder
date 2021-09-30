package me.varun.autobuilder.net;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import me.varun.autobuilder.gui.elements.AbstractGuiItem;
import me.varun.autobuilder.serialization.Autonomous;
import me.varun.autobuilder.serialization.GuiSerializer;

import java.io.IOException;
import java.util.List;

public class NetworkTables {

    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("autodata");
    NetworkTableEntry autoPath = table.getEntry("autoPath");

    static NetworkTables networkTablesInstance = new NetworkTables();

    public static NetworkTables getInstance(){
        return networkTablesInstance;
    }

    private NetworkTables(){

    }

    public void start() {
        inst.startClientTeam(3476);  // where TEAM=190, 294, etc, or use inst.startClient("hostname") or similar
        //inst.startDSClient();  // recommended if running on DS computer; this gets the robot IP from the DS
    }

    public void pushData(List<AbstractGuiItem> guiItemList){

        if(inst.isConnected()){
            try {
                String autonomousString = Serializer.serializeToString(GuiSerializer.serializeAutonomousForDeployment(guiItemList));
                autoPath.setString(autonomousString);
                Autonomous autonomous = Serializer.deserialize(autoPath.getString(null));
                System.out.println("Sent Data: " + autonomous);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            System.out.println("Cannot Send Data Not Connected");
        }

    }
}