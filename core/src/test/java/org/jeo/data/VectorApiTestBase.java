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
package org.jeo.data;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jeo.TestData;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.junit.Before;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * Abstract test case that exercises all aspects of the {@link VectorDataset} interface.
 * <p>
 * This test uses the {@link TestData#states()} dataset as a basis for testing and test implementors
 * must override {@link #createVectorData()} and return an instance backed by the states data.
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class VectorApiTestBase {

    VectorDataset data;

    @Before
    public final void setUp() throws Exception {
        init();
        data = createVectorData();
    }

    protected void init() throws Exception {
    }

    protected abstract VectorDataset createVectorData() throws Exception;

    @Test
    public void testGetName() {
        assertEquals("states", data.getName());
    }

    @Test
    public void testSchema() throws IOException {
        Schema schema = data.schema();
        assertNotNull(schema);

        assertNotNull(schema.geometry());
    }

    @Test
    public void testBounds() throws IOException {
        Envelope bbox = data.bounds();
        assertNotNull(bbox);

        assertEquals(-124.73, bbox.getMinX(), 0.01);
        assertEquals(24.96, bbox.getMinY(), 0.01);
        assertEquals(-66.96, bbox.getMaxX(), 0.01);
        assertEquals(49.37, bbox.getMaxY(), 0.01);
    }

    @Test
    public void testCRS() throws IOException {
        CoordinateReferenceSystem crs = data.crs();
        assertNotNull(crs);

        CoordinateReferenceSystem geo = Proj.EPSG_4326;
        Point p = Proj.reproject(Geom.point(115.37, 51.08), crs, geo);
        assertEquals(115.37, p.getX(), 0.01);
        assertEquals(51.08, p.getY(), 0.01);
    }

    @Test
    public void testCount() throws IOException {
        // count all
        assertEquals(49, data.count(new Query()));

        // count within bounds
        Set<String> abbrs = Sets.newHashSet("MO", "OK", "TX", "NM", "AR", "LA"); 

        Envelope bbox = new Envelope(-106.649513, -93.507217, 25.845198, 36.493877);
        assertEquals(abbrs.size(), data.count(new Query().bounds(bbox)));

        // count with spatial filters
        assertEquals(abbrs.size(), data.count(new Query().filter(String.format("INTERSECTS(%s, %s)", 
            data.schema().geometry().getName(), Envelopes.toPolygon(bbox)))));

        // count with attribute filters
        assertEquals(1, data.count(new Query().filter("STATE_NAME = 'Texas'")));
        assertEquals(48, data.count(new Query().filter("STATE_NAME <> 'Texas'")));
        assertEquals(2, data.count(new Query().filter("P_MALE > P_FEMALE")));
        assertEquals(3, data.count(new Query().filter("P_MALE >= P_FEMALE")));

        // count with logical filters
        assertEquals(1, data.count(new Query().filter("P_MALE > P_FEMALE AND SAMP_POP > 200000")));
        assertEquals(5, data.count(new Query().filter("P_MALE > P_FEMALE OR SAMP_POP > 2000000")));
        assertEquals(1, data.count(new Query().filter("P_MALE > P_FEMALE AND NOT SAMP_POP > 200000")));

        // count with id filters
        String fid = fidFor(data, "STATE_NAME = 'Texas'");
        assertEquals(1, data.count(new Query().filter(String.format("IN ('%s')", fid))));
    }

    @Test
    public void testCursorRead() throws Exception {
        // all
        assertEquals(49, Cursors.size(data.cursor(new Query())));

        // limit offset
        assertEquals(39, Cursors.size(data.cursor(new Query().offset(10))));
        assertEquals(10, Cursors.size(data.cursor(new Query().limit(10))));

        // bounds
        Envelope bbox = new Envelope(-106.649513, -93.507217, 25.845198, 36.493877);
        assertCovered(data.cursor(new Query().bounds(bbox)), "MO", "OK", "TX", "NM", "AR", "LA");

        // spatial filter
        assertCovered(data.cursor(new Query().filter(String.format("INTERSECTS(%s, %s)", 
            data.schema().geometry().getName(), Envelopes.toPolygon(bbox)))), 
            "MO", "OK", "TX", "NM", "AR", "LA");

        // comparison filter
        assertCovered(data.cursor(new Query().filter("STATE_NAME = 'Texas'")), "TX");
        assertNotCovered(data.cursor(new Query().filter("STATE_NAME <> 'Texas'")), "TX");
        assertCovered(data.cursor(new Query().filter("P_MALE > P_FEMALE")), "NV", "CA");
        assertCovered(data.cursor(new Query().filter("P_MALE >= P_FEMALE")), "NV", "CA", "WY");

        // logic filters
        assertCovered(
            data.cursor(new Query().filter("P_MALE > P_FEMALE AND SAMP_POP > 200000")), "CA");
        assertCovered(data.cursor(new Query().filter("P_MALE > P_FEMALE OR SAMP_POP > 2000000")), 
            "TX", "NY", "PA", "NV", "CA");
        assertCovered(
            data.cursor(new Query().filter("P_MALE > P_FEMALE AND NOT SAMP_POP > 200000")), "NV");

        // id filter
        String fid = fidFor(data, "STATE_NAME = 'Texas'");
        assertCovered(data.cursor(new Query().filter(String.format("IN ('%s')", fid))), "TX");
    }

    void assertNotCovered(Cursor<Feature> cursor, String... abbrs) throws IOException {
        final Set<String> set = Sets.newHashSet(abbrs);
        try {
            Iterables.find(cursor, new Predicate<Feature>() {
                @Override
                public boolean apply(Feature input) {
                    return set.contains(input.get("STATE_ABBR"));
                }
            });
            fail();
        }
        catch(NoSuchElementException expected) {}
    }

    void assertCovered(Cursor<Feature> cursor, String... abbrs) throws IOException {
        Set<String> set = Sets.newHashSet(abbrs);
        int count = 0;
        while(cursor.hasNext()) {
            set.remove(cursor.next().get("STATE_ABBR"));
            count++;
        }

        assertTrue(set.isEmpty());
        assertEquals(abbrs.length, count);
    }

    String fidFor(VectorDataset dataset, String filter) throws IOException {
        Cursor<Feature> c = dataset.cursor(new Query().filter(filter));
        try {
            assertTrue(c.hasNext());
            return c.next().getId();
        }
        finally {
            c.close();
        }
    }
}
