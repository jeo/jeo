package org.jeo.geojson;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geom.GeometryBuilder;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

public class GeoJSONWriterTest {

    GeoJSONWriter w;

    @Before
    public void setUp() {
        w = new GeoJSONWriter();
    }

    @Test
    public void testPoint() throws IOException {
        w.point(new GeometryBuilder().point(1,2));
        assertJSON("{'type':'Point','coordinates':[1,2]}");
    }

    @Test
    public void testLineString() throws IOException{
        w.lineString(new GeometryBuilder().lineString(1,2,3,4));
        assertJSON("{'type':'LineString','coordinates':[[1,2],[3,4]]}");
    }

    @Test
    public void testPolygon() throws IOException{
        w.polygon(new GeometryBuilder().polygon(1,2,3,4,5,6,1,2));
        assertJSON("{'type':'Polygon','coordinates':[[[1,2],[3,4],[5,6],[1,2]]]}");
    }

    @Test
    public void testPolygonWithHole() throws IOException{
        GeometryBuilder b = new GeometryBuilder();
        w.polygon(b.polygon(b.linearRing(100.0, 0.0, 101.0, 0.0, 101.0, 1.0, 100.0, 1.0, 100.0, 0.0), 
                b.linearRing(100.2, 0.2, 100.8, 0.2, 100.8, 0.8, 100.2, 0.8, 100.2, 0.2)));
        assertJSON("{'type':'Polygon','coordinates':[" +
            "[[100,0],[101,0],[101,1],[100,1],[100,0]]," +
            "[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]}");
    }

    @Test
    public void testMultiPoint() throws IOException{
        w.multiPoint(new GeometryBuilder().multiPoint(1,2,3,4));
        assertJSON("{'type':'MultiPoint','coordinates':[[1,2],[3,4]]}");
    }

    @Test
    public void testMultiLineString() throws IOException{
        GeometryBuilder b = new GeometryBuilder();
        w.multiLineString(b.multiLineString(b.lineString(1,2,3,4), b.lineString(5,6,7,8)));
        assertJSON("{'type':'MultiLineString','coordinates':[[[1,2],[3,4]],[[5,6],[7,8]]]}");
    }

    @Test
    public void testMultiPolygon()throws IOException {
        GeometryBuilder b = new GeometryBuilder();
        w.multiPolygon(b.multiPolygon(
            b.polygon(102.0, 2.0, 103.0, 2.0, 103.0, 3.0, 102.0, 3.0,102.0, 2.0),
            b.polygon(b.linearRing(100.0, 0.0, 101.0, 0.0, 101.0, 1.0, 100.0, 1.0, 100.0, 0.0),
                b.linearRing(100.2, 0.2, 100.8, 0.2, 100.8, 0.8, 100.2, 0.8, 100.2, 0.2))));
        assertJSON("{'type':'MultiPolygon','coordinates':[" +
            "[[[102,2],[103,2],[103,3],[102,3],[102,2]]]," +
            "[[[100,0],[101,0],[101,1],[100,1],[100,0]]," +
            "[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]]}");
    }

    @Test
    public void testGeometryCollection() throws IOException{
        GeometryBuilder b = new GeometryBuilder();
        w.geometryCollection(
            b.geometryCollection(b.point(100.0, 0.0), b.lineString(101.0, 0.0,102.0, 1.0)));
        assertJSON("{'type':'GeometryCollection','geometries':[" +
            "{'type':'Point','coordinates':[100,0]}," +
            "{'type':'LineString','coordinates':[[101,0],[102,1]]}]}");
    }

    @Test
    public void testFeature() throws IOException {
        Feature f = Features.create("1", schema(), new GeometryBuilder().point(0,0), "anvil", 10.99);
        w.feature(f);
        assertJSON("{'type':'Feature','id':'1','geometry':{'type':'Point','coordinates':[0,0]}," +
            "'properties':{'name':'anvil','cost':10.99}}");
    }

    @Test
    public void testFeatureCollection() throws IOException{
        Schema schema = schema();
        GeometryBuilder b = new GeometryBuilder();

        w.featureCollection();
        w.feature(Features.create("1", schema, b.point(0,0), "anvil", 10.99));
        w.feature(Features.create("2", schema, b.point(1,1), "dynamite", 2.99));
        w.feature(Features.create("3", schema, b.point(2,2), "bomb", 7.99));

        w.endFeatureCollection();
        assertJSON("{'type':'FeatureCollection','features':[" +
            "{'type':'Feature','id':'1','geometry':{'type':'Point','coordinates':[0,0]}," +
            "'properties':{'name':'anvil','cost':10.99}}," +
            "{'type':'Feature','id':'2','geometry':{'type':'Point','coordinates':[1,1]}," +
            "'properties':{'name':'dynamite','cost':2.99}}," +
            "{'type':'Feature','id':'3','geometry':{'type':'Point','coordinates':[2,2]}," +
            "'properties':{'name':'bomb','cost':7.99}}]}");
    }

    Schema schema() {
        return new SchemaBuilder("widget").field("geometry", Geometry.class)
            .field("name", String.class).field("cost", Double.class).schema();
    }

    void print() {
        System.out.println(w.toString().replaceAll("\"", "'"));
    }

    void assertJSON(String json) {
        assertEquals(json.replaceAll("'", "\""), w.toString());
    }
}
