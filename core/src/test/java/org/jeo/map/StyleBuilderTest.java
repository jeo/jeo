package org.jeo.map;

import static org.junit.Assert.*;

import org.junit.Test;

public class StyleBuilderTest {

    @Test
    public void testSimple() throws Exception {
        Stylesheet style = 
            new StyleBuilder().rule().select("Map").set("background-color", "green").style();

        assertEquals(1, style.getRules().size());

        Rule r = style.getRules().get(0);
        assertEquals(RGB.green, r.color(null, "background-color", null));

        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertEquals("Map", s.getName());
    }
}
