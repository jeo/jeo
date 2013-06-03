package org.jeo.data.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geom.GeomBuilder;
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
        MemVector data = mem.create(schema);
        data.getFeatures().add(Features.create(null, data.getSchema(), gb.point(0,0).toPoint(), 1, 
            "anvil", 10.99));
        data.getFeatures().add(Features.create(null, data.getSchema(), 
            gb.points(10,10,20,20).toLineString(), 2, "bomb", 11.99));
        data.getFeatures().add(Features.create(null, data.getSchema(), 
            gb.point(100,100).toPoint().buffer(10), 3, "dynamate", 12.99));
    }

    @Test
    public void testLayers() throws IOException {
        assertTrue(Iterables.any(mem.list(), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return "widgets".equals(input);
            }
        }));
    }

    @Test
    public void testCount() throws IOException {
        MemVector widgets = (MemVector) mem.get("widgets"); 
        assertEquals(3, widgets.count(null));
        assertEquals(2, widgets.count(new Query().limit(2)));
        assertEquals(1, widgets.count(new Query().bounds(new Envelope(-1,1,-1,1))));
        assertEquals(2, widgets.count(new Query().filter("cost < 12.0")));
        assertEquals(1, widgets.count(new Query().filter("cost < 12.0").offset(1)));
    }

    @Test
    public void testCursorRead() throws IOException {
        MemVector widgets = (MemVector) mem.get("widgets");
        assertCovered(widgets.cursor(new Query()), 1, 2, 3);
        assertCovered(widgets.cursor(new Query().limit(2)), 1, 2);
        assertCovered(widgets.cursor(new Query().bounds(new Envelope(-1,1,-1,1))), 1);
        assertCovered(widgets.cursor(new Query().filter("cost < 12.0")), 1, 2);
        assertCovered(widgets.cursor(new Query().filter("cost < 12.0").offset(1)), 2);
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
        MemVector widgets = (MemVector) mem.get("widgets");
        assertEquals(0, widgets.count(new Query().filter("cost  > 13.0")));

        Cursor<Feature> c = widgets.cursor(new Query().update());
        for (Feature f : c) {
            f.put("cost", ((Double)f.get("cost"))*2);
            c.write();
        }

        assertEquals(3, widgets.count(new Query().filter("cost  > 13.0")));
        
        c = widgets.cursor(new Query().append());
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

        assertEquals(5, widgets.count(null));
        assertCovered(widgets.cursor(new Query().filter("cost < 3.0")), 4, 5);
    }
}
