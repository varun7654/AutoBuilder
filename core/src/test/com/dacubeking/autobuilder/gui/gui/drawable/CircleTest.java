package com.dacubeking.autobuilder.gui.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CircleTest {

    @Test
    void fromString() {
        Circle circle = Circle.fromString("C:(5.12312,12312.2123),3.0,#144778");
        assertNotNull(circle);
        assertEquals(5.12312f, circle.center.x);
        assertEquals(12312.2123f, circle.center.y);
        assertEquals(3.0f, circle.radius);
        assertEquals(Color.valueOf("#144778"), circle.color);
    }

    @Test
    void fromString2() {
        Circle circle = Circle.fromString("C:(8.122,122.2123),2.1235,#143478");
        assertNotNull(circle);
        assertEquals(8.122f, circle.center.x);
        assertEquals(122.2123f, circle.center.y);
        assertEquals(2.1235f, circle.radius);
        assertEquals(Color.valueOf("#143478"), circle.color);
    }
}