package org.jeo.geopkg;

import static org.jeo.Tests.unzip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import jsqlite.Database;
import jsqlite.Stmt;

import org.apache.commons.io.FileUtils;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.ListFeature;
import org.jeo.feature.Schema;
import org.jeo.geom.Geom;
import org.jeo.geom.GeometryBuilder;
import org.jeo.geopkg.Entry.DataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class GeoPkgFeatureTest extends GeoPkgTestSupport {

    GeoPackage geopkg;

    @Before
    public void setUp() throws Exception {
        File dir = unzip(getClass().getResourceAsStream("states.db.zip"), newTmpDir());
        geopkg = new GeoPackage(new File(dir, "states.db"));
    }

    @After
    public void tearDown() throws Exception {
        geopkg.close();
        FileUtils.deleteQuietly(geopkg.getFile().getParentFile());
    }

    @Test
    public void testFeatureEntry() throws Exception {
        List<FeatureEntry> entries = geopkg.features();
        assertEquals(1, entries.size());

        FeatureEntry entry = entries.get(0);
        assertEquals("states", entry.getTableName());
    }

    @Test
    public void testCreateSchema() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        assertNotNull(entry);

        Schema schema = geopkg.createSchema(entry);
        assertEquals("states", schema.getName());

        assertNotNull(schema.geometry());
        assertEquals(MultiPolygon.class, schema.geometry().getType());

        assertNotNull(schema.field("STATE_NAME"));
    }

    @Test
    public void testCount() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        assertEquals(49, geopkg.count(entry, null));
    }

    @Test
    public void testRead() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        Cursor<Feature> c = geopkg.read(entry, null);
        
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
    public void testAdd() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        Schema schema = geopkg.schema(entry);

        Geometry g = new GeometryBuilder().point(0,0).buffer(1);
        Feature f = new ListFeature(null, null, schema);
        f.put(schema.geometry().getName(), g);
        f.put("STATE_NAME", "JEOLAND");
        geopkg.add(entry, f);

        assertEquals(50, geopkg.count(entry, null));

        Cursor<Feature> c = geopkg.read(entry, g.getEnvelopeInternal());
        assertTrue(c.hasNext());

        assertEquals("JEOLAND", c.next().get("STATE_NAME"));
        c.close();
    }

    @Test
    public void testCreate() throws Exception {
        Schema schema = Features.schema("widgets", 
            "geometry", Point.class, "name", String.class, "cost", Double.class);

        FeatureEntry entry = new FeatureEntry();
        entry.setSrid(4326);
        entry.setBounds(new Envelope(-180, 180, -90, 90));
        geopkg.create(entry, schema);

        GeometryBuilder gb = new GeometryBuilder();
        geopkg.add(entry, Features.create(null, schema, gb.point(1,2), "anvil", 10.99));

        Cursor<Feature> c = geopkg.read(entry, null);
        try {
            assertTrue(c.hasNext());
    
            Feature f = c.next();
            assertEquals(((Point)f.geometry()).getX(), 1f, 0.1);
            assertEquals(((Point)f.geometry()).getY(), 2f, 0.1);
            assertEquals("anvil", f.get("name"));
            assertEquals(10.99, ((Number)f.get("cost")).doubleValue(), 0.1);
        }
        finally {
            c.close();
        }

        //test re-loading the entry
        entry = geopkg.feature("widgets");
        assertNotNull(entry);

        assertEquals(Geom.Type.POINT, entry.getGeometryType());
        
        Database db = geopkg.getDatabase();
        Stmt st = db.prepare(
            "SELECT data_type FROM geopackage_contents WHERE table_name = ?");
        st.bind(1, "widgets");

        assertTrue(st.step());
        assertEquals(DataType.Feature.value(), st.column_string(0));

        st.close();
    }
}
