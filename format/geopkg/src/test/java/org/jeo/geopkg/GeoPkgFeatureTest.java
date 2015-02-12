/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.geopkg;

import static org.jeo.Tests.unzip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jeo.data.Cursor;
import org.jeo.vector.Query;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.Feature;
import org.jeo.vector.Features;
import org.jeo.vector.Schema;
import org.jeo.vector.SchemaBuilder;
import org.jeo.geom.Geom;
import org.jeo.geopkg.Entry.DataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import org.jeo.data.Transaction;
import org.jeo.data.Transactional;

public class GeoPkgFeatureTest extends GeoPkgTestSupport {

    GeoPkgWorkspace geopkg;

    @Before
    public void setUp() throws Exception {
        File dir = unzip(getClass().getResourceAsStream("usa.gpkg.zip"), newTmpDir());
        geopkg = GeoPackage.open(new File(dir, "usa.gpkg"));
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
        assertEquals(49, geopkg.count(entry, new Query()));
    }

    @Test
    public void testRead() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        Cursor<Feature> c = geopkg.cursor(entry, new Query());
        
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
    public void testReadWithFilter() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        Cursor<Feature> c = geopkg.cursor(entry, new Query().filter("STATE_NAME = 'Texas'"));
        
        assertNotNull(c);
        assertTrue(c.hasNext());
        assertNotNull(c.next());
        assertFalse(c.hasNext());
        c.close();
    }

    @Test
    public void testAdd() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        Schema schema = geopkg.schema(entry);

        Cursor<Feature> cursor = geopkg.cursor(entry, new Query().append());
        assertTrue(cursor.hasNext());
        Geometry g = Geom.point(0,0).buffer(1);
        Feature f = cursor.next();
        f.put(schema.geometry().getName(), g);
        f.put("STATE_NAME", "JEOLAND");
        cursor.write().close();

        assertEquals(50, geopkg.count(entry, new Query()));

        Cursor<Feature> c = geopkg.cursor(entry, new Query().bounds(g.getEnvelopeInternal()));
        assertTrue(c.hasNext());

        assertEquals("JEOLAND", c.next().get("STATE_NAME"));
        c.close();
    }

    @Test
    public void testUpdate() throws Exception {
        VectorDataset states = (VectorDataset) geopkg.get("states");
        assertEquals(1, states.count(new Query().filter("STATE_ABBR = 'TX'")));
        assertEquals(0, states.count(new Query().filter("STATE_ABBR = 'XT'")));

        Cursor<Feature> c = states.cursor(new Query().filter("STATE_NAME = 'Texas'").update());
        assertTrue(c.hasNext());

        Feature f = c.next();
        f.put("STATE_ABBR", "XT");
        c.write().close();
        
        assertEquals(0, states.count(new Query().filter("STATE_ABBR = 'TX'")));
        assertEquals(1, states.count(new Query().filter("STATE_ABBR = 'XT'")));
    }

    @Test
    public void testDelete() throws Exception {
        VectorDataset states = (VectorDataset) geopkg.get("states");
        assertEquals(49, states.count(new Query()));

        Cursor<Feature> c = states.cursor(new Query().filter("STATE_ABBR = 'TX'").update());
        assertTrue(c.hasNext());
        c.next();
        c.remove().close();

        assertEquals(48, states.count(new Query()));
        assertEquals(0, states.count(new Query().filter("STATE_NAME = 'Texas'")));
    }

    @Test
    public void testCreate() throws Exception {
        Schema schema = new SchemaBuilder("widgets").field("geometry", Point.class)
            .field("name", String.class).field("cost", Double.class).schema();

        FeatureEntry entry = new FeatureEntry();
        entry.setSrid(4326);
        entry.setBounds(new Envelope(-180, 180, -90, 90));
        geopkg.create(entry, schema);

        geopkg.insert(entry, Features.create(null, schema, Geom.point(1,2), "anvil", 10.99), null);

        Cursor<Feature> c = geopkg.cursor(entry, new Query());
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

        Backend.Results rs = geopkg.rawQuery("SELECT data_type FROM gpkg_contents WHERE table_name = 'widgets'");
        try {
            assertTrue(rs.next());
            assertEquals(DataType.Feature.value(), rs.getString(0));
        } finally {
            rs.close();
        }
    }
    
    private void assertCleanState(VectorDataset states) throws IOException {
        assertEquals(1, states.count(new Query().filter("STATE_ABBR = 'TX'")));
        assertEquals(49, states.count(new Query()));
    }

    @Test
    public void testTransaction() throws Exception {
        // ensure a transaction works across mutiple operations
        VectorDataset states = (VectorDataset) geopkg.get("states");
        assertCleanState(states);
        Transaction tx = ((Transactional) states).transaction(null);

        // update one
        Cursor<Feature> c = states.cursor(new Query().filter("STATE_NAME = 'Texas'").update().transaction(tx));
        assertTrue(c.hasNext());
        Feature f = c.next();
        f.put("STATE_ABBR", "XT");
        c.write().close();

        // remove one
        c = states.cursor(new Query().filter("STATE_ABBR = 'WV'").update().transaction(tx));
        assertTrue(c.hasNext());
        c.next();
        c.remove().close();

        // add one
        c = states.cursor(new Query().append().transaction(tx));
        assertTrue(c.hasNext());
        f = c.next();
        f.put("STATE_ABBR", "XX");
        c.write().close();

        tx.rollback();
        assertCleanState(states);
    }
}
