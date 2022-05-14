package com.dacubeking.autobuilder.gui.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LineTest {

    @Test
    void fromString() {
        Line line = Line.fromString("L:(1.0,2.0),(3.0,4.0),#eb2d4e");
        assertNotNull(line);
        assertEquals(line.start, new Vector2(1, 2));
        assertEquals(line.end, new Vector2(3, 4));
        assertEquals(Color.valueOf("#eb2d4e"), line.color);
    }

    @Test
    void fromString2() {
        Line line = Line.fromString("L:(8.122,122.2123),(5.12312,12312.2123),#7db0ab");
        assertNotNull(line);
        assertEquals(8.122f, line.start.x);
        assertEquals(122.2123f, line.start.y);
        assertEquals(5.12312f, line.end.x);
        assertEquals(12312.2123f, line.end.y);
        assertEquals(Color.valueOf("#7db0ab"), line.color);
    }
}