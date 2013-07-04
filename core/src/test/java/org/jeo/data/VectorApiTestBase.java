package org.jeo.data;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jeo.TestData;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.geom.Envelopes;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Abstract test case that exercises all aspects of the {@link VectorData} interface.
 * <p>
 * This test uses the {@link TestData#states()} dataset as a basis for testing and test implementors
 * must override {@link #createVectorData()} and return an instance backed by the states data.
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class VectorApiTestBase {

    VectorData data;

    @Before
    public final void setUp() throws Exception {
        init();
        data = createVectorData();
    }

    protected void init() throws Exception {
    }

    protected abstract VectorData createVectorData() throws Exception;

    @Test
    public void testGetName() {
        assertEquals("states", data.getName());
    }

    @Test
    public void testSchema() throws IOException {
        Schema schema = data.getSchema();
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
    public void testCount() throws IOException {
        // count all
        assertEquals(49, data.count(new Query()));

        // count within bounds
        Set<String> abbrs = Sets.newHashSet("MO", "OK", "TX", "NM", "AR", "LA"); 

        Envelope bbox = new Envelope(-106.649513, -93.507217, 25.845198, 36.493877);
        assertEquals(abbrs.size(), data.count(new Query().bounds(bbox)));

        // count with spatial filters
        assertEquals(abbrs.size(), data.count(new Query().filter(String.format("INTERSECTS(%s, %s)", 
            data.getSchema().geometry().getName(), Envelopes.toPolygon(bbox)))));

        // count with attribute filters
        assertEquals(1, data.count(new Query().filter("STATE_NAME = 'Texas'")));
        assertEquals(48, data.count(new Query().filter("STATE_NAME <> 'Texas'")));
        assertEquals(2, data.count(new Query().filter("P_MALE > P_FEMALE")));
        assertEquals(3, data.count(new Query().filter("P_MALE >= P_FEMALE")));

        // count with logical filters
        assertEquals(1, data.count(new Query().filter("P_MALE > P_FEMALE AND SAMP_POP > 200000")));
        assertEquals(5, data.count(new Query().filter("P_MALE > P_FEMALE OR SAMP_POP > 2000000")));
        assertEquals(1, data.count(new Query().filter("P_MALE > P_FEMALE AND NOT SAMP_POP > 200000")));
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
            data.getSchema().geometry().getName(), Envelopes.toPolygon(bbox)))), 
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
}
