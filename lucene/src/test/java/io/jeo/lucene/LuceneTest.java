/* Copyright 2015 The jeo project. All rights reserved.
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
package org.jeo.lucene;

import com.vividsolutions.jts.geom.Polygonal;
import io.jeo.vector.Feature;
import io.jeo.vector.VectorQuery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LuceneTest {

    static LuceneDataset data;

    @BeforeClass
    public static void setUp() throws Exception {
        data = LuceneTests.setUpStatesData();
    }

    @AfterClass
    public static void tearDown() {
        data.close();
    }

    @Test
    public void testFeatureGeometry() throws Exception {
        Feature f = data.cursor(new VectorQuery()).first().get();

        assertNotNull(f.geometry());
        assertTrue(f.geometry() instanceof Polygonal);
    }
}
