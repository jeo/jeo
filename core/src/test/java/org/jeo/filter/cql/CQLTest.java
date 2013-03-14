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
