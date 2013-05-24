package org.jeo.shp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.jeo.Tests;
import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geom.GeometryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ShapefileTest {

    @Rule public TestName name = new TestName();

    ShpDataset shp;

    @Before
    public void setUp() throws Exception {
        shp = ShpData.states();
    }

    @After
    public void tearDown() throws Exception {
        shp.dispose();
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
            assertEquals(new Integer(i+1), Integer.valueOf(f.getId()));
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

    @Test
    public void testCreateAndAppend() throws Exception {
        Schema schema = new SchemaBuilder("widgets").field("shape", Polygon.class, "epsg:4326")
            .field("name", String.class).field("cost", Double.class).schema();

        File file = new File(Tests.newTmpDir(), "widgets.shp");
        assertFalse(file.exists());

        ShpDataset shp = ShpDataset.create(file, schema);
        assertTrue(file.exists());
        assertTrue(new File(file.getParentFile(), "widgets.shx").exists());
        assertTrue(new File(file.getParentFile(), "widgets.dbf").exists());
        assertTrue(new File(file.getParentFile(), "widgets.prj").exists());

        GeometryBuilder gb = new GeometryBuilder();
        Cursor<Feature> c = shp.cursor(new Query().append());
        
        Feature f = c.next();
        f.put(shp.getSchema().geometry().getName(), gb.point(0,0).buffer(10));
        f.put("name", "bomb");
        f.put("cost", 1.99);
        c.write();

        f = c.next();
        f.put(shp.getSchema().geometry().getName(), gb.lineString(0,0,1,1).buffer(1));
        f.put("name", "dynamite");
        f.put("cost", 2.99);
        c.write();

        f = c.next();
        f.put(shp.getSchema().geometry().getName(), gb.polygon(-5,5, 5,5, 2,-2, 3,-5, -3,-5, -2,-2, -5,5));
        f.put("name", "anvil");
        f.put("cost", 3.99);
        c.write();

        c.close();
        
        assertEquals(3, shp.count(new Query()));

        c = shp.cursor(new Query().filter("name = 'bomb'"));
        assertTrue(c.hasNext());
        assertEquals(1.99, c.next().get("cost"));
    }

    @Test
    public void testUpdate() throws Exception {
        Cursor<Feature> c = shp.cursor(new Query().update());
        while(c.hasNext()) {
            Feature f = c.next();
            f.put("STATE_ABBR", f.get("STATE_ABBR").toString().toLowerCase());
            c.write();
        }
        c.close();

        c = shp.cursor(new Query());

        ShpDataset shp2 = ShpData.states();
        Cursor<Feature> d = shp2.cursor(new Query());
        while(d.hasNext()) {
            assertTrue(c.hasNext());
            Feature f = c.next();
            Feature g = d.next();

            String abbr1 = f.get("STATE_ABBR").toString();
            String abbr2 = g.get("STATE_ABBR").toString();

            assertFalse(abbr1.equals(abbr2));
            assertEquals(abbr1.toUpperCase(), abbr2);
        }

        c.close();
        d.close();
        
        shp2.dispose();
    }
}
