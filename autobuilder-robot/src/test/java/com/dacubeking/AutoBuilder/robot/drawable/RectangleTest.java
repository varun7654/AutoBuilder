package com.dacubeking.AutoBuilder.robot.drawable;

import com.dacubeking.AutoBuilder.robot.utility.Vector2;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RectangleTest {

    @Test
    void testToString() {
        Rectangle rectangle = new Rectangle(new Vector2(3.123f, 1.123f), 1.45323f, -5.131f, 3.123f,
                new Color8Bit(23, 5, 89));
        System.out.println(rectangle);
        assertEquals("R:(3.123,1.123),1.45323,-5.131,3.123,#170559", rectangle.toString());
    }

    @Test
    void testToString2() {
        Rectangle rectangle = new Rectangle(new Vector2(-213, -23), 2.453f, 5.131f, -2.123f,
                new Color8Bit(23, 5, 89));

        assertEquals("R:(-213.0,-23.0),2.453,5.131,-2.123,#170559", rectangle.toString());
    }
}