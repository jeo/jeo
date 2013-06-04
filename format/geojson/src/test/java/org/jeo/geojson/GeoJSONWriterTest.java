package org.jeo.geojson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.jeo.geom.Geom;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

public class GeoJSONWriterTest extends GeoJSONTestSupport {

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
        w.obj();
        w.key("foo").value("bar");
        w.key("point").point(Geom.point(1,2 ));
        w.endObj();

        JSONObject obj = (JSONObject) JSONValue.parse(string());
        assertEquals("bar", obj.get("foo"));
        assertTrue(obj.get("point") instanceof JSONObject);
    }

    @Test
    public void testArray() throws Exception {
        w.obj();
        w.key("foo").array()
            .point(Geom.point(1, 2)).point(Geom.point(3, 4))
        .endArray();
        w.endObj();

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

    String string() {
        return w.getWriter().toString();
    }
}
