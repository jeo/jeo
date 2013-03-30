package org.jeo.filter.cql;

import static org.junit.Assert.*;

import org.jeo.filter.Comparison;
import org.jeo.filter.Filter;
import org.jeo.filter.Literal;
import org.jeo.filter.Logic;
import org.jeo.filter.Property;
import org.jeo.filter.Spatial;
import org.junit.Test;

public class CQLTest {

    @Test
    public void testSimpleComparison() throws ParseException {
        Filter f = CQL.parse("foo = 'bar'");
        assertTrue(f instanceof Comparison);

        Comparison c = (Comparison)f;
        assertTrue(c.getLeft() instanceof Property);
        assertTrue(c.getRight() instanceof Literal);

        Literal l = (Literal) c.getRight();
        assertEquals("bar", l.evaluate(null));
    }

    @Test
    public void testBetween() throws ParseException {
        Filter f = CQL.parse("foo BETWEEN 1 AND 10");
        assertTrue(f instanceof Logic);
        
        Logic l = (Logic)f;
        assertEquals(Logic.Type.AND, l.getType());
        assertEquals(2, l.getParts().size());

        Comparison c = (Comparison)l.getParts().get(0);
        assertEquals(Comparison.Type.GREATER_OR_EQUAL, c.getType());
        assertEquals("foo", ((Property)c.getLeft()).getProperty());
        assertEquals(1, c.getRight().evaluate(null));
        
        c = (Comparison)l.getParts().get(1);
        assertEquals(Comparison.Type.LESS_OR_EQUAL, c.getType());
        assertEquals("foo", ((Property)c.getLeft()).getProperty());
        assertEquals(10, c.getRight().evaluate(null));
    }
    
    @Test
    public void testLogic() throws ParseException {
        Filter f = CQL.parse("foo > 3 AND bar <= 10");
        assertTrue(f instanceof Logic);
    }

    @Test
    public void testSpatial() throws ParseException {
        Filter f = CQL.parse("INTERSECTS(the_geom, POINT(0 0))");
        assertTrue(f instanceof Spatial);
    }
}
