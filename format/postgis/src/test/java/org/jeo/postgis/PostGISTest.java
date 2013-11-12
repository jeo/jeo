package org.jeo.postgis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.DatasetHandle;
import org.jeo.data.Query;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geom.GeomBuilder;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class PostGISTest {

    PostGISWorkspace pg;

    //uncomment to view debug logs during test
//    @BeforeClass
//    public static void logging() {
//        PostGISTests.logging();
//    }

    @BeforeClass
    public static void connect()  {
        try {
            PostGISTests.connect();
        }
        catch(Exception e) {
            Assume.assumeTrue(false);
        }
    }

    @Before
    public void rollback() throws Exception {
        PGPoolingDataSource ds = PostGISWorkspace.createDataSource(PostGISTests.OPTS); 
        Connection cx = ds.getConnection();
        Statement st = cx.createStatement();
        st.executeUpdate(
            "DELETE FROM states WHERE \"STATE_NAME\" = 'JEOLAND' OR \"STATE_NAME\" is null");
        st.executeUpdate("UPDATE states set \"STATE_ABBR\" = upper(\"STATE_ABBR\")");
        st.executeUpdate("DROP TABLE IF EXISTS widgets");
        st.executeUpdate("DELETE FROM geometry_columns WHERE f_table_name = 'widgets'");
        st.close();
        cx.close();
        ds.close();
    }

    @Before
    public void setUp() throws Exception {
        pg = new PostGISWorkspace(PostGISTests.OPTS);
    }

    @After
    public void tearDown() throws Exception {
        pg.close();
    }
    @Test
    public void testLayers() throws Exception {
        Iterables.find(pg.list(), refFor("states"));
        try {
            Iterables.find(pg.list(), refFor("geometry_columns"));
            fail();
        }
        catch(NoSuchElementException e) {}
        try {
            Iterables.find(pg.list(), refFor("geography_columns"));
            fail();
        }
        catch(NoSuchElementException e) {}
        try {
            Iterables.find(pg.list(), refFor("raster_columns"));
            fail();
        }
        catch(NoSuchElementException e) {}
        try {
            Iterables.find(pg.list(), refFor("raster_overviews"));
            fail();
        }
        catch(NoSuchElementException e) {}
        try {
            Iterables.find(pg.list(), refFor("topology"));
            fail();
        }
        catch(NoSuchElementException e) {}
    }

    Predicate<DatasetHandle> refFor(final String name) {
        return new Predicate<DatasetHandle>() {
            @Override
            public boolean apply(DatasetHandle input) {
                return name.equals(input.getName());
            }
        };
    }

    @Test
    public void testSchema() throws Exception {
        VectorDataset states = pg.get("states");
        assertNotNull(states);
        
        Schema schema = states.schema();

        assertNotNull(schema.field("STATE_NAME"));
        assertNotNull(schema.geometry());
        assertEquals(MultiPolygon.class, schema.geometry().getType());

        assertNotNull(schema.crs());
        assertEquals("EPSG:4326", schema.crs().getName());
    }

    @Test
    public void testBounds() throws Exception {
        VectorDataset states = pg.get("states");

        Envelope bounds = states.bounds();
        assertNotNull(bounds);

        assertEquals(-124.7, bounds.getMinX(), 0.1);
        assertEquals(25.0, bounds.getMinY(), 0.1);
        assertEquals(-67.0, bounds.getMaxX(), 0.1);
        assertEquals(49.3, bounds.getMaxY(), 0.1);
    }

    @Test
    public void testCount() throws Exception {
        VectorDataset states = pg.get("states");
        assertEquals(49, states.count(new Query()));
    }
    
    @Test
    public void testCountWithBounds() throws Exception {
        VectorDataset states = pg.get("states");
        Set<String> abbrs = Sets.newHashSet("MO", "OK", "TX", "NM", "AR", "LA"); 

        Envelope bbox = new Envelope(-106.649513, -93.507217, 25.845198, 36.493877);
        assertEquals(abbrs.size(), states.count(new Query().bounds(bbox)));
    }

    @Test
    public void testCursorRead() throws Exception {
        VectorDataset states = pg.get("states");

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

    @Test
    public void testCursorFilter() throws Exception {
        VectorDataset states = pg.get("states");
        assertEquals(1, Cursors.size(states.cursor(new Query().filter("STATE_NAME = 'Texas'"))));
    }

    @Test
    public void testCursorUpdate() throws Exception {
        VectorDataset states = pg.get("states");
        
        Cursor<Feature> c = states.cursor(new Query().update());
        while(c.hasNext()) {
            Feature f = c.next();

            String abbr = f.get("STATE_ABBR").toString();
            assertEquals(abbr, abbr.toUpperCase());

            f.put("STATE_ABBR", f.get("STATE_ABBR").toString().toLowerCase());
            c.write();
        }
        c.close();

        for (Feature f : states.cursor(new Query())) {
            String abbr = f.get("STATE_ABBR").toString();

            assertEquals(abbr, abbr.toLowerCase());
        }

        c.close();
    }

    @Test
    public void testCursorInsert() throws Exception {
        VectorDataset states = pg.get("states");
        Schema schema = states.schema();

        Cursor<Feature> c = states.cursor(new Query().append());
        Feature f = c.next();

        GeomBuilder gb = new GeomBuilder();
        Geometry g = gb.point(0,0).point().buffer(1).toMultiPolygon();
        f.put(schema.geometry().getName(), g);
        f.put("STATE_NAME", "JEOLAND");
        c.write();
        c.close();

        assertEquals(50, states.count(new Query()));

        c = states.cursor(new Query().bounds(g.getEnvelopeInternal()));
        assertTrue(c.hasNext());

        assertEquals("JEOLAND", c.next().get("STATE_NAME"));
        c.close();
    }

    @Test
    public void testCreate() throws Exception {
        Schema widgets = new SchemaBuilder("widgets").field("shape", Polygon.class)
            .field("name", String.class).field("cost", Double.class).schema();

        PostGISDataset data = pg.create(widgets);
        assertEquals(0, data.count(new Query()));

        GeomBuilder gb = new GeomBuilder();
        Cursor<Feature> c = data.cursor(new Query().append());

        Feature f = c.next();
        f.put("shape", gb.point(0,0).point().buffer(10).get());
        f.put("name", "bomb");
        f.put("cost", 1.99);
        c.write();

        f = c.next();
        f.put("shape", gb.points(0,0,1,1).lineString().buffer(1).get());
        f.put("name", "dynamite");
        f.put("cost", 2.99);
        c.write();

        f = c.next();
        f.put("shape", gb.points(-5,5, 5,5, 2,-2, 3,-5, -3,-5, -2,-2, -5,5).ring().toPolygon());
        f.put("name", "anvil");
        f.put("cost", 3.99);

        c.write();

        data = pg.get("widgets");
        assertEquals(3, data.count(new Query()));

        c = data.cursor(new Query().filter("name = 'bomb'"));
        assertTrue(c.hasNext());
        assertEquals(1.99, c.next().get("cost"));
    }
}
