package com.dacubeking.autobuilder.gui.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RectangleTest {

    @Test
    void fromString() {
        Rectangle rectangle = Rectangle.fromString("R:(3.123,1.123),1.45323,-5.131,3.123,#170559");
        assertNotNull(rectangle);
        assertEquals(3.123f, rectangle.bottomLeftCorner.x);
        assertEquals(1.123f, rectangle.bottomLeftCorner.y);
        assertEquals(1.45323f, rectangle.width);
        assertEquals(-5.131f, rectangle.height);
        assertEquals(3.123f, rectangle.rotation);
        assertEquals(Color.valueOf("#170559"), rectangle.color);
    }

    @Test
    void fromString2() {
        Rectangle rectangle = Rectangle.fromString("(-213.0,-23.0),2.453,5.131,-2.123,#170559");
        assertNotNull(rectangle);
        assertEquals(-213f, rectangle.bottomLeftCorner.x);
        assertEquals(-23f, rectangle.bottomLeftCorner.y);
        assertEquals(2.453f, rectangle.width);
        assertEquals(5.131f, rectangle.height);
        assertEquals(-2.123f, rectangle.rotation);
        assertEquals(Color.valueOf("#170559"), rectangle.color);
    }
}