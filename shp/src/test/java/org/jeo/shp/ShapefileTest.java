package org.jeo.shp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.jeo.feature.Feature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
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
        Cursor<Feature> c = shp.cursor(new Query());
        
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

    @Test
    public void testReadBounds() throws Exception {
        Set<String> abbrs = Sets.newHashSet("MO", "OK", "TX", "NM", "AR", "LA"); 
        
        Envelope bbox = new Envelope(-106.649513, -93.507217, 25.845198, 36.493877);

        assertEquals(abbrs.size(), Cursors.size(shp.cursor(new Query().bounds(bbox))));
        for (Feature f: shp.cursor(new Query().bounds(bbox))) {
            abbrs.remove(f.get("STATE_ABBR"));
        }

        assertTrue(abbrs.isEmpty());
    }
}
