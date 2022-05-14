package com.dacubeking.autobuilder.gui.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PathTest {

    @Test
    void fromString() {
        Path path = Path.fromString("P:(1.0,3.0),(3.0,4.0),(5.0,6.0),(7.0,6.0),(9.0,10.0),(11.0,15.0),#eb2d4e");
        assertNotNull(path);
        assertEquals(6, path.vertices.size);
        assertEquals(new Array<Vector2>() {{
            add(new Vector2(1, 3));
            add(new Vector2(3, 4));
            add(new Vector2(5, 6));
            add(new Vector2(7, 6));
            add(new Vector2(9, 10));
            add(new Vector2(11, 15));
        }}, path.vertices);
        assertEquals(Color.valueOf("#eb2d4e"), path.color);
    }

    @Test
    void fromString2() {
        Path path = Path.fromString("P:(8.122,122.2123),(5.12312,12312.2123),(7.12312,12312.2123),(9.12312,12312.2123),(11" +
                ".12312,12312.2123),(13.12312,12312.2123),(15.12312,12312.2123),(1.0,2.0),(3.0,4.0),(5.0,6.0),(7.0,6.0),(9.0,10" +
                ".0),(11.0,15.0),#7ab0ab");
        assertNotNull(path);
        assertEquals(13, path.vertices.size);
        assertEquals(new Array<Vector2>() {{
            add(new Vector2(8.122f, 122.2123f));
            add(new Vector2(5.12312f, 12312.2123f));
            add(new Vector2(7.12312f, 12312.2123f));
            add(new Vector2(9.12312f, 12312.2123f));
            add(new Vector2(11.12312f, 12312.2123f));
            add(new Vector2(13.12312f, 12312.2123f));
            add(new Vector2(15.12312f, 12312.2123f));
            add(new Vector2(1.0f, 2.0f));
            add(new Vector2(3.0f, 4.0f));
            add(new Vector2(5.0f, 6.0f));
            add(new Vector2(7.0f, 6.0f));
            add(new Vector2(9.0f, 10.0f));
            add(new Vector2(11.0f, 15.0f));
        }}, path.vertices);
        assertEquals(Color.valueOf("#7ab0ab"), path.color);
    }
}