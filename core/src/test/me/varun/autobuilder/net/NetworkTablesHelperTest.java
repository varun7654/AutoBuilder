package me.varun.autobuilder.net;

import edu.wpi.first.networktables.NetworkTableInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NetworkTablesHelperTest {

    NetworkTableInstance instance;

    @BeforeEach
    void setUp() {
        instance = NetworkTableInstance.create();
        instance.startServer("", "localhost", 1181);
        while (NetworkTablesHelper.getInstance().isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Waiting for connection...");
        }
        System.out.println("Connected!");
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void test() throws InterruptedException {
        Thread.sleep(10000);
    }
}