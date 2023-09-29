package com.dacubeking.AutoBuilder.robot.drawable;

import com.dacubeking.AutoBuilder.robot.utility.Vector2;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathTest {

    @Test
    void testToString() {
        Path path = new Path(new Color8Bit(235, 45, 78), new Vector2(1, 3), new Vector2(3, 4), new Vector2(5, 6),
                new Vector2(7, 6), new Vector2(9, 10), new Vector2(11, 15));
        System.out.println(path);
        assertEquals("P:(1.0,3.0),(3.0,4.0),(5.0,6.0),(7.0,6.0),(9.0,10.0),(11.0,15.0),#eb2d4e", path.toString());
    }

    @Test
    void testToString2() {
        Path path = new Path(new Color8Bit(225, 78, 132), new Vector2(5, 1), new Vector2(3.345f, 12.324f), new Vector2(2.567f,
                4.234f), new Vector2(1.234f, 3.234f), new Vector2(0.234f, 2.234f), new Vector2(0.234f, 1.234f));
        assertEquals("P:(5.0,1.0),(3.345,12.324),(2.567,4.234),(1.234,3.234),(0.234,2.234),(0.234,1.234),#e14e84",
                path.toString());
    }

    @Test
    void testToString3() {
        Path path = new Path(new Vector2[]{new Vector2(1, 2), new Vector2(3, 4), new Vector2(5, 6), new Vector2(7, 6),
                new Vector2(9, 10), new Vector2(11, 15)}, new Color8Bit(239, 76, 71));
        assertEquals("P:(1.0,2.0),(3.0,4.0),(5.0,6.0),(7.0,6.0),(9.0,10.0),(11.0,15.0),#ef4c47", path.toString());
    }
}