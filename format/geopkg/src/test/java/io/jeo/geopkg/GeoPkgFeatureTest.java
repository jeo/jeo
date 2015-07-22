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
package io.jeo.geopkg;

import static io.jeo.Tests.unzip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import io.jeo.geom.Bounds;
import io.jeo.vector.FeatureWriteCursor;
import io.jeo.vector.ListFeature;
import org.apache.commons.io.FileUtils;
import io.jeo.data.Cursor;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.Feature;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import io.jeo.geom.Geom;
import io.jeo.geopkg.Entry.DataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import io.jeo.data.Transaction;
import io.jeo.data.Transactional;

public class GeoPkgFeatureTest extends GeoPkgTestSupport {

    GeoPkgWorkspace geopkg;

    @Before
    public void setUp() throws Exception {
        Path dir = unzip(getClass().getResourceAsStream("usa.gpkg.zip"), newTmpDir());
        geopkg = GeoPackage.open(dir.resolve("usa.gpkg"));
    }

    @After
    public void tearDown() throws Exception {
        geopkg.close();
        FileUtils.deleteQuietly(geopkg.file().getParentFile());
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
        assertEquals("states", schema.name());

        assertNotNull(schema.geometry());
        assertEquals(MultiPolygon.class, schema.geometry().type());

        assertNotNull(schema.field("STATE_NAME"));
    }

    @Test
    public void testCount() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        assertEquals(49, geopkg.count(entry, new VectorQuery()));
    }

    @Test
    public void testRead() throws Exception {
        FeatureEntry entry = geopkg.feature("states");
        Cursor<Feature> c = geopkg.read(entry, new VectorQuery());
        
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
        Cursor<Feature> c = geopkg.read(entry, new VectorQuery().filter("STATE_NAME = 'Texas'"));
        
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

        FeatureWriteCursor cursor = geopkg.append(entry, new VectorQuery());
        assertTrue(cursor.hasNext());
        Geometry g = Geom.point(0,0).buffer(1);
        Feature f = cursor.next();
        f.put(schema.geometry().name(), g);
        f.put("STATE_NAME", "JEOLAND");
        cursor.write().close();

        assertEquals(50, geopkg.count(entry, new VectorQuery()));

        Cursor<Feature> c = geopkg.read(entry, new VectorQuery().bounds(g.getEnvelopeInternal()));
        assertTrue(c.hasNext());

        assertEquals("JEOLAND", c.next().get("STATE_NAME"));
        c.close();
    }

    @Test
    public void testUpdate() throws Exception {
        VectorDataset states = (VectorDataset) geopkg.get("states");
        assertEquals(1, states.count(new VectorQuery().filter("STATE_ABBR = 'TX'")));
        assertEquals(0, states.count(new VectorQuery().filter("STATE_ABBR = 'XT'")));

        FeatureWriteCursor c = states.update(new VectorQuery().filter("STATE_NAME = 'Texas'"));
        assertTrue(c.hasNext());

        Feature f = c.next();
        f.put("STATE_ABBR", "XT");
        c.write().close();
        
        assertEquals(0, states.count(new VectorQuery().filter("STATE_ABBR = 'TX'")));
        assertEquals(1, states.count(new VectorQuery().filter("STATE_ABBR = 'XT'")));
    }

    @Test
    public void testDelete() throws Exception {
        VectorDataset states = (VectorDataset) geopkg.get("states");
        assertEquals(49, states.count(new VectorQuery()));

        FeatureWriteCursor c = states.update(new VectorQuery().filter("STATE_ABBR = 'TX'"));
        assertTrue(c.hasNext());
        c.next();
        c.remove().close();

        assertEquals(48, states.count(new VectorQuery()));
        assertEquals(0, states.count(new VectorQuery().filter("STATE_NAME = 'Texas'")));
    }

    @Test
    public void testCreate() throws Exception {
        Schema schema = new SchemaBuilder("widgets").field("geometry", Point.class)
            .field("name", String.class).field("cost", Double.class).schema();

        FeatureEntry entry = new FeatureEntry();
        entry.setSrid(4326);
        entry.setBounds(new Bounds(-180, 180, -90, 90));
        geopkg.create(entry, schema);

        geopkg.insert(entry, new ListFeature(schema, Geom.point(1,2), "anvil", 10.99), null);

        Cursor<Feature> c = geopkg.read(entry, new VectorQuery());
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
        assertEquals(1, states.count(new VectorQuery().filter("STATE_ABBR = 'TX'")));
        assertEquals(49, states.count(new VectorQuery()));
    }

    @Test
    public void testTransaction() throws Exception {
        // ensure a transaction works across mutiple operations
        VectorDataset states = (VectorDataset) geopkg.get("states");
        assertCleanState(states);
        Transaction tx = ((Transactional) states).transaction(null);

        // update one
        FeatureWriteCursor c = states.update(new VectorQuery().filter("STATE_NAME = 'Texas'").transaction(tx));
        assertTrue(c.hasNext());
        Feature f = c.next();
        f.put("STATE_ABBR", "XT");
        c.write().close();

        // remove one
        c = states.update(new VectorQuery().filter("STATE_ABBR = 'WV'").transaction(tx));
        assertTrue(c.hasNext());
        c.next();
        c.remove().close();

        // add one
        c = states.append(new VectorQuery().transaction(tx));
        assertTrue(c.hasNext());
        f = c.next();
        f.put("STATE_ABBR", "XX");
        c.write().close();

        tx.rollback();
        assertCleanState(states);
    }
}
