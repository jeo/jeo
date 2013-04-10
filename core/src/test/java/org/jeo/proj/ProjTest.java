package org.jeo.proj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jeo.geom.GeometryBuilder;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class ProjTest {

    @Test
    public void testCrs() {
        CoordinateReferenceSystem crs = Proj.crs("EPSG:4326");
        assertNotNull(crs);
    }

    @Test
    public void testEpsgCode() {
        CoordinateReferenceSystem crs = Proj.crs("EPSG:4326");
        assertEquals(4326, Proj.epsgCode(crs).intValue());
    }

    @Test
    public void testBounds() {
        CoordinateReferenceSystem crs = Proj.crs("EPSG:3005");
        assertNotNull(Proj.bounds(crs));
    }

    @Test
    public void testReproject() throws Exception {
        GeometryBuilder gb = new GeometryBuilder();

        Point p = Proj.reproject(gb.point(-117, 63.15), Proj.crs("epsg:4326"), Proj.crs("epsg:3157"));
        assertEquals(802027.258082, p.getX(), 0.1);
        assertEquals(7016429.376474, p.getY(), 0.1);
    }

    @Test
    public void testReprojectEnvelope() throws Exception {
        Envelope e = new Envelope(490967.4065, 491909.5552, 5457747.3926, 5458839.7600);
        e = Proj.reproject(e, Proj.crs("epsg:3157"), Proj.crs("epsg:4326"));
        assertEquals(-123.12, e.getMinX(), 0.01);
        assertEquals(-123.11, e.getMaxX(), 0.01);
        assertEquals(49.27, e.getMinY(), 0.01);
        assertEquals(49.28, e.getMaxY(), 0.01);
    }

    @Test
    public void test900913() {
        assertNotNull(Proj.crs("epsg:900913"));
    }

}
