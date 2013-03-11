package org.jeo.map;

import static org.junit.Assert.*;

import org.jeo.map.Selector.Type;
import org.junit.Test;

public class StylesheetTest {

    @Test
    public void testSelect() {
        Stylesheet style = new StyleBuilder()
            .rule().select("Map").set("background-color", "white")
            .rule().select("#widgets").filter("cost > 10").set("line-color", "#123")
            .style();

        assertEquals(2, style.getRules().size());

        assertEquals(1, style.select("Map", Type.NAME).size());
        assertEquals(1, style.select("widgets", Type.ID).size());
    }
}
