package org.jeo.filter;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.junit.Test;

public class FilterTest {
    
    @Test
    public void testLiteral() {
        Literal<Integer> l = new Literal<Integer>(12);
        assertEquals(Integer.valueOf(12), l.evaluate(null));
    }

    @Test
    public void testProperty() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");

        Property<String> p = new Property<String>("foo");
        assertEquals("bar", p.evaluate(Features.create(map)));
    }

    @Test
    public void testComparison() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("str", "one");
        map.put("int", 1);

        Feature f = Features.create(map);

        Comparison<Feature> c = new Comparison<Feature>(Comparison.Type.EQUAL, 
            new Property<String>("str"), new Literal<String>("one"));
        assertTrue(c.apply(f));

        c = new Comparison<Feature>(Comparison.Type.EQUAL, 
                new Property<Integer>("int"), new Literal<Integer>(2));
        assertFalse(c.apply(f));

        c = new Comparison<Feature>(Comparison.Type.LESS, 
                new Property<Integer>("int"), new Literal<Integer>(2));
        assertTrue(c.apply(f));

        c = new Comparison<Feature>(Comparison.Type.LESS_OR_EQUAL, 
                new Property<Integer>("int"), new Literal<Integer>(1));
        assertTrue(c.apply(f));

        c = new Comparison<Feature>(Comparison.Type.GREATER_OR_EQUAL, 
                new Property<Integer>("int"), new Literal<Integer>(1));
        assertTrue(c.apply(f));

        c = new Comparison<Feature>(Comparison.Type.GREATER, 
                new Property<Integer>("int"), new Literal<Integer>(1));
        assertFalse(c.apply(f));
    }

    @Test
    public void testLogic() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("str", "one");
        map.put("int", 1);

        Feature f = Features.create(map);

        Comparison<Feature> c1 = new Comparison<Feature>(Comparison.Type.EQUAL, 
                new Property<String>("str"), new Literal<String>("one"));
        Comparison<Feature> c2 = new Comparison<Feature>(Comparison.Type.EQUAL, 
                new Property<Integer>("int"), new Literal<Integer>(1));

        Logic<Feature> l = new Logic<Feature>(Logic.Type.AND, c1, c2);
        assertTrue(l.apply(f));
    }
}
