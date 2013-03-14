package org.jeo.map;

import static org.junit.Assert.*;

import org.junit.Test;

public class StylesheetTest {

    @Test
    public void testSelect() {
        Stylesheet style = new StyleBuilder()
            .rule().select("Map").set("background-color", "white").endRule()
            .rule().select("#widgets").filter("cost > 10").set("line-color", "#123").endRule()
            .style();

        assertEquals(2, style.getRules().size());

        assertEquals(1, style.selectByName("Map").list().size());
        assertEquals(1, style.selectById("widgets").list().size());
    }
}
