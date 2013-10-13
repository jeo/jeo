package org.jeo.geojson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.jeo.geojson.simple.JSONArray;
import org.jeo.geojson.simple.JSONObject;
import org.jeo.geojson.simple.JSONValue;
import org.jeo.geom.Geom;
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

    String string() {
        return w.getWriter().toString();
    }
}
