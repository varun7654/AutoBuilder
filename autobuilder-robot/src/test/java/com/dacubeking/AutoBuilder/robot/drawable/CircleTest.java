package com.dacubeking.AutoBuilder.robot.drawable;

import com.dacubeking.AutoBuilder.robot.utility.Vector2;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CircleTest {

    static Random random;

    @BeforeAll
    static void setUp() {
        random = new Random(719479);
    }

    @Test
    void testToString() {
        Circle circle = new Circle(new Vector2(1, 2), 3, new Color8Bit(255, 255, 255));
        assertEquals("C:(1.0,2.0),3.0,#ffffff", circle.toString());
    }

    @Test
    void testToString2() {
        Circle circle = new Circle(5.12312f, 12312.2123f, 3, new Color8Bit(20, 67, 124));
        System.out.println(circle);
        assertEquals("C:(5.12312,12312.212),3.0,#14437c", circle.toString());
    }
}