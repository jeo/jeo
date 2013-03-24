package org.jeo.shp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

public class ShapefileTest {

    @Rule public TestName name = new TestName();

    Shapefile shp;

    @Before
    public void setUp() throws Exception {
        shp = ShpData.states();
    }

    @Test
    public void testCountAll() throws IOException {
        assertEquals(49, shp.count(null));
    }

    @Test
    public void testBounds() throws IOException {
        Envelope e = shp.bounds();
        assertEquals(-124.73, e.getMinX(), 0.01);
        assertEquals(24.96, e.getMinY(), 0.01);
        assertEquals(-66.97, e.getMaxX(), 0.01);
        assertEquals(49.37, e.getMaxY(), 0.01);

    }

    @Test
    public void testRead() throws IOException {
        Cursor<Feature> c = shp.cursor(null, null);
        
        assertNotNull(c);
        for (int i = 0; i < 49; i++) {
            assertTrue(c.hasNext());

            Feature f = c.next();
            assertNotNull(f);

            assertTrue(f.geometry() instanceof MultiPolygon);
            assertNotNull(f.get("STATE_NAME"));
        }

        assertFalse(c.hasNext());
        assertNull(c.next());
        c.close();
    }
}
