package com.dacubeking.AutoBuilder.robot.drawable;

import com.dacubeking.AutoBuilder.robot.utility.Vector2;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineTest {

    @Test
    void testToString() {
        Line line = new Line(new Vector2(1, 2), new Vector2(3, 4), new Color8Bit(235, 45, 78));
        System.out.println(line);
        assertEquals("L:(1.0,2.0),(3.0,4.0),#eb2d4e", line.toString());
    }
}