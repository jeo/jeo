package org.jeo.map;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RuleListTest {

    @Test
    public void testSelect() {
        Style style = new StyleBuilder()
            .rule().select("Map").set("background-color", "white").endRule()
            .rule().select("#widgets").filter("cost > 10").set("line-color", "#123").endRule()
            .style();

        RuleList rules = style.getRules();
        assertEquals(2, rules.size());

        assertEquals(1, rules.selectByName("Map", false).size());
        assertEquals(1, rules.selectById("widgets", false).size());
    }
}
