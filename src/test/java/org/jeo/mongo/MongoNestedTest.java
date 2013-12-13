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
