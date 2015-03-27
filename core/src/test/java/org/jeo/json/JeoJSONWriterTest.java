/* Copyright 2014 The jeo project. All rights reserved.
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
package org.jeo.json;

import org.jeo.TestData;
import org.jeo.util.Util;
import org.junit.Before;
import org.junit.Test;


import java.io.*;

import static org.junit.Assert.assertEquals;

public class JeoJSONWriterTest {

    ByteArrayOutputStream out;

    @Before
    public void setUp() {
        out = new ByteArrayOutputStream();
    }

    @Test
    public void testVectorDataset() throws Exception {
        JeoJSONWriter w = new JeoJSONWriter(output(), 2);
        w.dataset(TestData.states()).flush();

        JSONObject obj = (JSONObject) JSONValue.parseWithException(input());
        assertEquals("states", obj.get("name"));
        assertEquals("vector", obj.get("type"));
    }

    @Test
    public void testRasterDataset() throws Exception {
        JeoJSONWriter w = new JeoJSONWriter(output(), 2);
        w.dataset(TestData.dem()).flush();

        JSONObject obj = (JSONObject) JSONValue.parseWithException(input());
        assertEquals("dem", obj.get("name"));
        assertEquals("raster", obj.get("type"));
    }

    @Test
    public void testTileDataset() throws Exception {
        JeoJSONWriter w = new JeoJSONWriter(output(), 2);
        w.dataset(TestData.ne1()).flush();

        JSONObject obj = (JSONObject) JSONValue.parseWithException(input());
        assertEquals("ne1", obj.get("name"));
        assertEquals("tile", obj.get("type"));
    }

    Writer output() {
        return new OutputStreamWriter(out);
    }

    Reader input() {
        return new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), Util.UTF_8);
    }
}
