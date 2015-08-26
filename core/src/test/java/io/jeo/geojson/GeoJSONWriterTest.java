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
package io.jeo.geojson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import io.jeo.geom.Geom;
import io.jeo.json.JSONArray;
import io.jeo.json.JSONObject;
import io.jeo.json.JSONValue;
import io.jeo.vector.MapFeature;
import org.junit.Before;
import org.junit.Test;

public class GeoJSONWriterTest {

    GeoJSONWriter w;

    @Before
    public void setUp() {
        w = new GeoJSONWriter(new StringWriter());
    }

    @Test
    public void testSingleObject() throws IOException {
        w.point(Geom.point(1, 2));
        
        JSONObject obj = (JSONObject) JSONValue.parse(string());

        assertEquals("Point", obj.get("type"));
    }

    @Test
    public void testSimpleObject() throws IOException {
        w.object();
        w.key("foo").value("bar");
        w.key("point").point(Geom.point(1,2 ));
        w.endObject();

        JSONObject obj = (JSONObject) JSONValue.parse(string());
        assertEquals("bar", obj.get("foo"));
        assertTrue(obj.get("point") instanceof JSONObject);
    }

    @Test
    public void testArray() throws Exception {
        w.object();
        w.key("foo").array()
            .point(Geom.point(1, 2)).point(Geom.point(3, 4))
        .endArray();
        w.endObject();

        JSONObject obj = (JSONObject) JSONValue.parse(string());
        assertTrue(obj.get("foo") instanceof JSONArray);
    }

    @Test
    public void testTopLevelArray() throws Exception {
        w.array();
        w.point(Geom.point(1,2)).point(Geom.point(3,4));
        w.endArray();

        JSONArray arr = (JSONArray) JSONValue.parse(string());
        assertEquals(2 ,arr.size());
    }

    @Test
    public void testObjectWithArrayProperties() throws Exception {
        w.object();

        w.key("foo").array();
        w.value("one").value("two");
        w.endArray();

        w.key("bar").array();
        w.value("three").value("four");
        w.endArray();

        w.endObject();

        JSONObject obj = (JSONObject) JSONValue.parse(string());
        assertEquals(2, obj.keySet().size());
       
        assertTrue(obj.get("foo") instanceof JSONArray);
        assertEquals(2, ((JSONArray)obj.get("foo")).size());

        assertTrue(obj.get("bar") instanceof JSONArray);
        assertEquals(2, ((JSONArray)obj.get("bar")).size());
    }

    @Test
    public void testObjectWithEmptyObjectProperties() throws Exception {
        w.object();

        w.key("foo").object().endObject();
        w.key("bar").object().endObject();

        w.endObject();

        JSONObject obj = (JSONObject) JSONValue.parse(string());
        assertEquals(2, obj.keySet().size());
       
        assertTrue(obj.get("foo") instanceof JSONObject);
        assertEquals(0, ((JSONObject)obj.get("foo")).size());

        assertTrue(obj.get("bar") instanceof JSONObject);
        assertEquals(0, ((JSONObject)obj.get("bar")).size());
    }

    @Test
    public void testFeature() throws Exception {
        Map<String,Object> map = new HashMap<>();
        map.put("geom", Geom.point(0, 0));
        map.put("name", "zero");

        w.feature(new MapFeature(map));

        JSONObject obj = (JSONObject) JSONValue.parse(string());
        assertEquals("Feature", obj.get("type"));
        assertNotNull(obj.get("geometry"));
        assertNotNull(obj.get("properties"));
    }

    String string() {
        return w.getWriter().toString();
    }
}
