package org.jeo.proj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;

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
}
