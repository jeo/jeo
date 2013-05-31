package org.jeo.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeomBuilderTest {

    GeomBuilder gb;

    @Before
    public void setUp() {
        gb = new GeomBuilder();
    }

    @Test
    public void testPoint() {
        Point p = gb.point(1,2).toPoint();
        assertPoint(p, 1, 2);
    }

    @Test
    public void testPointZ() {
        Point p = gb.pointz(1,2,3).toPoint();
        assertPointZ(p, 1,2,3);
    }

    @Test
    public void testLineString() {
        LineString l = gb.point(1,2).point(3,4).toLineString();
        assertEquals(2, l.getNumPoints());
        assertPoint(l.getPointN(0), 1,2);
        assertPoint(l.getPointN(1), 3,4);
    }

    @Test
    public void testLineStringZ() {
        LineString l = gb.pointsz(1,2,3,4,5,6).toLineString();
        assertEquals(2, l.getNumPoints());
        assertPointZ(l.getPointN(0), 1,2,3);
        assertPointZ(l.getPointN(1), 4,5,6);
    }

    @Test
    public void testPolygon() {
        Polygon p = gb.points(1,2,3,4,5,6,1,2).ring().toPolygon();
        LineString outer = p.getExteriorRing();
        assertEquals(4, outer.getNumPoints());
        assertPoint(outer.getPointN(0), 1,2);
        assertPoint(outer.getPointN(1), 3,4);
        assertPoint(outer.getPointN(2), 5,6);
        assertPoint(outer.getPointN(3), 1,2);
    }

    @Test
    public void testPolygonWithHole() {
        Polygon p = gb.points(0,0,10,0,10,10,0,10,0,0).ring().points(4,4,6,4,6,6,4,6,4,4).ring()
            .toPolygon();
                
        LineString outer = p.getExteriorRing();
        assertEquals(5, outer.getNumPoints());
        assertPoint(outer.getPointN(0), 0,0);
        assertPoint(outer.getPointN(1), 10,0);
        assertPoint(outer.getPointN(2), 10,10);
        assertPoint(outer.getPointN(3), 0,10);
        assertPoint(outer.getPointN(4), 0,0);

        assertEquals(1, p.getNumInteriorRing());
        LineString inner = p.getInteriorRingN(0);
        assertEquals(5, outer.getNumPoints());
        assertPoint(inner.getPointN(0), 4,4);
        assertPoint(inner.getPointN(1), 6,4);
        assertPoint(inner.getPointN(2), 6,6);
        assertPoint(inner.getPointN(3), 4,6);
        assertPoint(inner.getPointN(4), 4,4);
    }

    @Test
    public void testMultiPoint() {
        MultiPoint mp = gb.points(0,1,2,3).toMultiPoint();
        assertEquals(2, mp.getNumGeometries());

        assertPoint((Point) mp.getGeometryN(0), 0,1);
        assertPoint((Point) mp.getGeometryN(1), 2,3);
    }

    @Test
    public void testMultiLineString() {
        MultiLineString ml = 
            gb.points(0,1,2,3).lineString().points(4,5,6,7).lineString().toMultiLineString();

        assertEquals(2, ml.getNumGeometries());
        LineString l1 = (LineString) ml.getGeometryN(0);
        assertEquals(2, l1.getNumPoints());
        assertPoint(l1.getPointN(0), 0,1);
        assertPoint(l1.getPointN(1), 2,3);
        
        LineString l2 = (LineString) ml.getGeometryN(1);
        assertEquals(2, l2.getNumPoints());
        assertPoint(l2.getPointN(0), 4,5);
        assertPoint(l2.getPointN(1), 6,7);
    }

    @Test
    public void testMultiPolygon() {
        MultiPolygon mp = gb.points(1,2,3,4,5,6,1,2).ring().polygon()
          .points(7,8,9,10,11,12,7,8).ring().polygon().toMultiPolygon();

        assertEquals(2, mp.getNumGeometries());
    }

    @Test
    public void testGeometryCollection() {
        GeometryCollection col = gb.point(1,2).point().points(1,2,3,4).lineString()
            .points(1,2,3,4,5,6,1,2).ring().polygon().toCollection();

        assertEquals(3, col.getNumGeometries());
        assertTrue(col.getGeometryN(0) instanceof Point);
        assertTrue(col.getGeometryN(1) instanceof LineString);
        assertTrue(col.getGeometryN(2) instanceof Polygon);
    }

    void assertPoint(Point p, double x, double y) {
        assertEquals(x, p.getX(), 0.1);
        assertEquals(y, p.getY(), 0.1);
    }

    void assertPointZ(Point p, double x, double y, double z) {
        assertEquals(x, p.getX(), 0.1);
        assertEquals(y, p.getY(), 0.1);
        assertEquals(z, p.getCoordinate().z, 0.1);
    }
}
