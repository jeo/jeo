package org.jeo.postgis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

public class PostGISTest {

    
    PostGISWorkspace pg;

    @BeforeClass
    public static void logging() {
        //uncomment to view debug logs during test
//        Logger log = Logger.getLogger(LoggerFactory.getLogger(PostGIS.class).getName());
//        log.setLevel(Level.FINE);
//
//        ConsoleHandler h = new ConsoleHandler();
//        h.setLevel(Level.FINE);
//        log.addHandler(h);
    }

    @BeforeClass
    public static void connect()  {
        try {
            Connection cx = 
                PostGISWorkspace.createDataSource(new PostGISOpts("jeo")).getConnection();
            Assume.assumeNotNull(cx);
            cx.close();
        }
        catch(Exception e) {
            Assume.assumeTrue(false);
        }
    }

    @Before
    public void setUp() throws Exception {
        pg = new PostGISWorkspace(new PostGISOpts("jeo"));
    }

    @After
    public void tearDown() throws Exception {
        pg.dispose();
    }
    @Test
    public void testLayers() throws Exception {
        Iterators.find(pg.layers(), Predicates.equalTo("states"));
        try {
            Iterators.find(pg.layers(), Predicates.equalTo("geometry_columns"));
            fail();
        }
        catch(NoSuchElementException e) {}
        try {
            Iterators.find(pg.layers(), Predicates.equalTo("geography_columns"));
            fail();
        }
        catch(NoSuchElementException e) {}
        try {
            Iterators.find(pg.layers(), Predicates.equalTo("raster_columns"));
            fail();
        }
        catch(NoSuchElementException e) {}
        try {
            Iterators.find(pg.layers(), Predicates.equalTo("raster_overviews"));
            fail();
        }
        catch(NoSuchElementException e) {}
        try {
            Iterators.find(pg.layers(), Predicates.equalTo("topology"));
            fail();
        }
        catch(NoSuchElementException e) {}
    }

    @Test
    public void testSchema() throws Exception {
        VectorData states = pg.get("states");
        assertNotNull(states);
        
        Schema schema = states.getSchema();

        assertNotNull(schema.field("STATE_NAME"));
        assertNotNull(schema.geometry());
        assertEquals(MultiPolygon.class, schema.geometry().getType());

        assertNotNull(schema.crs());
        assertEquals("EPSG:4326", schema.crs().getName());
    }

    @Test
    public void testBounds() throws Exception {
        VectorData states = pg.get("states");

        Envelope bounds = states.bounds();
        assertNotNull(bounds);

        assertEquals(-124.7, bounds.getMinX(), 0.1);
        assertEquals(25.0, bounds.getMinY(), 0.1);
        assertEquals(-67.0, bounds.getMaxX(), 0.1);
        assertEquals(49.3, bounds.getMaxY(), 0.1);
    }

    @Test
    public void testCount() throws Exception {
        VectorData states = pg.get("states");
        assertEquals(49, states.count(new Query()));
    }
    
    @Test
    public void testCountWithBounds() throws Exception {
        VectorData states = pg.get("states");
        Set<String> abbrs = Sets.newHashSet("MO", "OK", "TX", "NM", "AR", "LA"); 

        Envelope bbox = new Envelope(-106.649513, -93.507217, 25.845198, 36.493877);
        assertEquals(abbrs.size(), states.count(new Query().bounds(bbox)));
    }

    @Test
    public void testCursorRead() throws Exception {
        VectorData states = pg.get("states");

        Cursor<Feature> c = states.cursor(new Query());
        
        assertNotNull(c);
        for (int i = 0; i < 49; i++) {
            assertTrue(c.hasNext());

            Feature f = c.next();
            //assertEquals(new Integer(i+1), Integer.valueOf(f.getId()));
            assertNotNull(f);

            assertTrue(f.geometry() instanceof MultiPolygon);
            assertNotNull(f.get("STATE_NAME"));
        }

        assertFalse(c.hasNext());
        assertNull(c.next());
        c.close();
    }
}
