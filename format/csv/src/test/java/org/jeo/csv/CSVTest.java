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
package org.jeo.csv;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jeo.Tests;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class CSVTest {

    CSVDataset csv;

    @Before
    public void setUp() throws IOException {
        csv = new CSVDataset(Tests.newTmpFile("jeo", "csv", csv()), new CSVOpts().xy("lon", "lat"));
    }

    @Test
    public void testCount() throws Exception {
        assertEquals(4, csv.count(new Query()));
    }

    @Test
    public void testBounds() throws Exception {
        assertEquals(new Envelope(2, 8, 1, 7), csv.bounds());
    }

    @Test
    public void testChunks() throws Exception {
        assertEquals(1, Cursors.size(csv.cursor(new Query().filter("name = 'fire,cracker'"))));
    }

    InputStream csv() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("name, cost, lat, lon").append("\n");
        sb.append("bomb, 1.99, 1,2").append("\n");
        sb.append("dynamite, 2.99, 3,4").append("\n");
        sb.append("anvil, 3.99,5,6").append("\n");
        sb.append("\"fire,cracker\", 4.99,7, 8").append("\n");

        return new ByteArrayInputStream(sb.toString().getBytes());
    }
}
