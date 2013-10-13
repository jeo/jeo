package org.jeo.mongo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Feature;
import org.junit.Test;

import com.mongodb.DBObject;

public class MongoNestedTest extends MongoTest {

    @Override
    protected MongoTestData createTestData() {
        return new NestedTestData();
    }

    @Test
    public void testNestedProperties() throws IOException {
        VectorDataset data = mongo.get("states");
        Cursor<Feature> c = data.cursor(new Query());
        assertTrue(c.hasNext());

        Feature f = c.next();

        assertNotNull(f.get("pop"));
        assertTrue(f.get("pop") instanceof DBObject);

        assertNotNull(f.get("pop.male"));
        assertTrue(f.get("pop.male") instanceof Number);
    }
}
