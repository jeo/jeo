package org.jeo.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;
import org.junit.Test;

public class FilterTest {
    
    @Test
    public void testLiteral() {
        Literal l = new Literal(12);
        assertEquals(Integer.valueOf(12), l.evaluate(null));
    }

    @Test
    public void testProperty() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");

        Property p = new Property("foo");
        assertEquals("bar", p.evaluate(new BasicFeature(null, map)));
    }

    @Test
    public void testComparison() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("str", "one");
        map.put("int", 1);

        Feature f = new BasicFeature(null, map);

        Comparison c = new Comparison(Comparison.Type.EQUAL, 
            new Property("str"), new Literal("one"));
        assertTrue(c.apply(f));

        c = new Comparison(Comparison.Type.EQUAL, 
                new Property("int"), new Literal(2));
        assertFalse(c.apply(f));

        c = new Comparison(Comparison.Type.LESS, 
                new Property("int"), new Literal(2));
        assertTrue(c.apply(f));

        c = new Comparison(Comparison.Type.LESS_OR_EQUAL, 
                new Property("int"), new Literal(1));
        assertTrue(c.apply(f));

        c = new Comparison(Comparison.Type.GREATER_OR_EQUAL, 
                new Property("int"), new Literal(1));
        assertTrue(c.apply(f));

        c = new Comparison(Comparison.Type.GREATER, 
                new Property("int"), new Literal(1));
        assertFalse(c.apply(f));
    }

    @Test
    public void testComparisonConversion() {
        assertTrue(
            new Comparison(Comparison.Type.EQUAL, new Literal(1), new Literal(1.0)).apply(null));
        assertTrue(
            new Comparison(Comparison.Type.EQUAL, new Literal("1"), new Literal(1)).apply(null));
        assertTrue(
            new Comparison(Comparison.Type.EQUAL, new Literal(1.0), new Literal("1")).apply(null));
    }

    @Test
    public void testLogic() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("str", "one");
        map.put("int", 1);

        Feature f = new BasicFeature(null, map);

        Comparison c1 = new Comparison(Comparison.Type.EQUAL, 
                new Property("str"), new Literal("one"));
        Comparison c2 = new Comparison(Comparison.Type.EQUAL, 
                new Property("int"), new Literal(1));

        Logic l = new Logic(Logic.Type.AND, c1, c2);
        assertTrue(l.apply(f));
    }
}
