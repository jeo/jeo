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
package io.jeo.data.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import io.jeo.data.Cursor;
import io.jeo.data.Dataset;
import io.jeo.geom.GeomBuilder;
import io.jeo.vector.FeatureWriteCursor;
import io.jeo.data.Handle;
import io.jeo.vector.ListFeature;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.Feature;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class MemoryTest {

    MemWorkspace mem;

    @Before
    public void setUp() throws IOException {
        GeomBuilder gb = new GeomBuilder();

        mem = new MemWorkspace();
        Schema schema = new SchemaBuilder("widgets")
            .field("geometry", Geometry.class)
            .field("id", Integer.class)
            .field("name", String.class)
            .field("cost", Double.class).schema();
        MemVectorDataset data = mem.create(schema);
        data.add(new ListFeature(data.schema(), gb.point(0, 0).toPoint(), 1, "anvil", 10.99));
        data.add(new ListFeature(data.schema(), gb.points(10,10,20,20).toLineString(), 2, "bomb", 11.99));
        data.add(new ListFeature(data.schema(), gb.point(100,100).toPoint().buffer(10), 3, "dynamite", 12.99));
    }

    @Test
    public void testLayers() throws IOException {
        assertTrue(Iterables.any(mem.list(), new Predicate<Handle<Dataset>>() {
            @Override
            public boolean apply(Handle<Dataset> h) {
                return "widgets".equals(h.name());
            }
        }));
    }

    @Test
    public void testCount() throws IOException {
        MemVectorDataset widgets = (MemVectorDataset) mem.get("widgets");
        assertEquals(3, widgets.count(new VectorQuery()));
        assertEquals(2, widgets.count(new VectorQuery().limit(2)));
        assertEquals(1, widgets.count(new VectorQuery().bounds(new Envelope(-1,1,-1,1))));
        assertEquals(2, widgets.count(new VectorQuery().filter("cost < 12.0")));
        assertEquals(1, widgets.count(new VectorQuery().filter("cost < 12.0").offset(1)));
    }

    @Test
    public void testCursorRead() throws IOException {
        MemVectorDataset widgets = (MemVectorDataset) mem.get("widgets");
        assertCovered(widgets.read(new VectorQuery()), 1, 2, 3);
        assertCovered(widgets.read(new VectorQuery().limit(2)), 1, 2);
        assertCovered(widgets.read(new VectorQuery().bounds(new Envelope(-1, 1, -1, 1))), 1);
        assertCovered(widgets.read(new VectorQuery().filter("cost < 12.0")), 1, 2);
        assertCovered(widgets.read(new VectorQuery().filter("cost < 12.0").offset(1)), 2);
    }

    void assertCovered(Cursor<Feature> c, Integer... ids) {
        Set<Integer> s = Sets.newHashSet(ids);
        for (Feature f : c) {
            s.remove(f.get("id"));
        }
        assertTrue(s.isEmpty());
    }

    @Test
    public void testCursorWrite() throws IOException {
        MemVectorDataset widgets = (MemVectorDataset) mem.get("widgets");
        assertEquals(0, widgets.count(new VectorQuery().filter("cost  > 13.0")));

        FeatureWriteCursor c = widgets.update(new VectorQuery());
        for (Feature f : c) {
            f.put("cost", ((Double) f.get("cost")) * 2);
            c.write();
        }

        assertEquals(3, widgets.count(new VectorQuery().filter("cost  > 13.0")));
        
        c = widgets.append(new VectorQuery());
        assertTrue(c.hasNext());

        Feature f = c.next();
        f.put("id", 4);
        f.put("cost", 1.99);
        f.put("name", "marble");
        c.write();

        f = c.next();
        f.put("id", 5);
        f.put("cost", 2.99);
        f.put("name", "tack");
        c.write();

        assertEquals(5, widgets.count(new VectorQuery()));
        assertCovered(widgets.read(new VectorQuery().filter("cost < 3.0")), 4, 5);
    }
}
