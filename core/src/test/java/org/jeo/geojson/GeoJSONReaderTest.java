package org.jeo.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.jeo.feature.Feature;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJSONReaderTest {

    GeoJSONReader r;

    @Before
    public void setUp() {
        r = new GeoJSONReader();
    }

    @Test
    public void testPoint() throws Exception {
        Point p = (Point) read("{ 'type': 'Point', 'coordinates': [100.0, 0.0] }");
        assertNotNull(p);

        assertCoord(p.getCoordinate(), 100.0, 0.0);
    }

    private void assertCoord(Coordinate c, double x, double y) {
        assertEquals(x, c.x, 0.1);
        assertEquals(y, c.y, 0.1);
    }

    @Test
    public void testLineString() throws Exception {
        LineString l = (LineString)
            read("{ 'type': 'LineString', 'coordinates': [ [100.0, 0.0], [101.0, 1.0] ] }");

        assertNotNull(l);
        assertCoord(l.getCoordinates()[0], 100.0, 0.0);
        assertCoord(l.getCoordinates()[1], 101.0, 1.0);
    }

    @Test
    public void testPolygon() throws Exception {
        Polygon p = (Polygon) read("{ 'type': 'Polygon', " +
            "'coordinates': [ [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ] ] }");
        assertNotNull(p);
        assertCoord(p.getExteriorRing().getCoordinates()[0], 100.0, 0.0);
        assertCoord(p.getExteriorRing().getCoordinates()[1], 101.0, 0.0);
        assertCoord(p.getExteriorRing().getCoordinates()[2], 101.0, 1.0);
        assertCoord(p.getExteriorRing().getCoordinates()[3], 100.0, 1.0);
        assertCoord(p.getExteriorRing().getCoordinates()[4], 100.0, 0.0);
    }

    @Test
    public void testMultiPoint() throws Exception {
        MultiPoint mp = (MultiPoint) 
            read("{ 'type': 'MultiPoint', 'coordinates': [ [100.0, 0.0], [101.0, 1.0] ] }");
        assertNotNull(mp);
        assertCoord(mp.getCoordinates()[0], 100.0, 0.0);
        assertCoord(mp.getCoordinates()[1], 101.0, 1.0);
    }

    @Test
    public void testMultiLineString() throws Exception {
        MultiLineString ml = (MultiLineString) read("{ 'type': 'MultiLineString', 'coordinates': [ " +
            "[ [100.0, 0.0], [101.0, 1.0] ], [ [102.0, 2.0], [103.0, 3.0] ] ] }");
        assertNotNull(ml);

        assertCoord(ml.getGeometryN(0).getCoordinates()[0], 100.0, 0.0);
        assertCoord(ml.getGeometryN(0).getCoordinates()[1], 101.0, 1.0);
        assertCoord(ml.getGeometryN(1).getCoordinates()[0], 102.0, 2.0);
        assertCoord(ml.getGeometryN(1).getCoordinates()[1], 103.0, 3.0);
    }

    @Test
    public void testMultiPolygon() throws Exception {
        MultiPolygon mp = (MultiPolygon) read("{ 'type': 'MultiPolygon', 'coordinates': [ " +
            "[[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]], " +
            "[[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]], " +
             "[[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]] ] }");
        assertNotNull(mp);

        assertCoord(((Polygon)mp.getGeometryN(0)).getExteriorRing().getCoordinates()[0], 102.0, 2.0);
        assertCoord(((Polygon)mp.getGeometryN(0)).getExteriorRing().getCoordinates()[1], 103.0, 2.0);
        assertCoord(((Polygon)mp.getGeometryN(0)).getExteriorRing().getCoordinates()[2], 103.0, 3.0);
        assertCoord(((Polygon)mp.getGeometryN(0)).getExteriorRing().getCoordinates()[3], 102.0, 3.0);
        assertCoord(((Polygon)mp.getGeometryN(0)).getExteriorRing().getCoordinates()[4], 102.0, 2.0);

        assertCoord(((Polygon)mp.getGeometryN(1)).getExteriorRing().getCoordinates()[0], 100.0, 0.0);
        assertCoord(((Polygon)mp.getGeometryN(1)).getExteriorRing().getCoordinates()[1], 101.0, 0.0);
        assertCoord(((Polygon)mp.getGeometryN(1)).getExteriorRing().getCoordinates()[2], 101.0, 1.0);
        assertCoord(((Polygon)mp.getGeometryN(1)).getExteriorRing().getCoordinates()[3], 100.0, 1.0);
        assertCoord(((Polygon)mp.getGeometryN(1)).getExteriorRing().getCoordinates()[4], 100.0, 0.0);

        assertCoord(((Polygon)mp.getGeometryN(1)).getInteriorRingN(0).getCoordinates()[0], 100.2, 0.2);
        assertCoord(((Polygon)mp.getGeometryN(1)).getInteriorRingN(0).getCoordinates()[1], 100.8, 0.2);
        assertCoord(((Polygon)mp.getGeometryN(1)).getInteriorRingN(0).getCoordinates()[2], 100.8, 0.8);
        assertCoord(((Polygon)mp.getGeometryN(1)).getInteriorRingN(0).getCoordinates()[3], 100.2, 0.8);
        assertCoord(((Polygon)mp.getGeometryN(1)).getInteriorRingN(0).getCoordinates()[4], 100.2, 0.2);
    }

    @Test
    public void testGeometryCollection() {
        GeometryCollection gc = (GeometryCollection) read("{ 'type': 'GeometryCollection', 'geometries': [ " +
            "{ 'type': 'Point', 'coordinates': [100.0, 0.0] }, " +
            "{ 'type': 'LineString', 'coordinates': [ [101.0, 0.0], [102.0, 1.0] ] } ] }");

        assertNotNull(gc);
        assertEquals(2, gc.getNumGeometries());
        assertTrue(gc.getGeometryN(0) instanceof Point);
        assertTrue(gc.getGeometryN(1) instanceof LineString);

        assertCoord(gc.getGeometryN(0).getCoordinate(), 100.0, 0.0);
        assertCoord(gc.getGeometryN(1).getCoordinates()[0], 101.0, 0.0);
        assertCoord(gc.getGeometryN(1).getCoordinates()[1], 102.0, 1.0);
    }

    @Test
    public void testFeature() {
        Feature f = (Feature) read("{ 'type': 'Feature', 'geometry': { 'type': 'Polygon', " +
            "'coordinates': [ [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ] ] }, " +
            "'properties': { 'prop0': 'this', 'prop1': 'that'} }");

        assertTrue(f.geometry() instanceof Polygon);

        Set<String> set = Sets.newHashSet("this", "that");
        set.removeAll(f.values());
        assertTrue(set.isEmpty());
    }

    @Test
    public void testFeatureCollection() {
        @SuppressWarnings("unchecked")
        List<Feature> features = (List<Feature>) read("{ 'type': 'FeatureCollection', 'features': [ " +
            "{ 'type': 'Feature', 'geometry': {'type': 'Point', 'coordinates': [102.0, 0.5]}, " +
            "'properties': {'prop0': 'value0'} }, " +
            "{ 'type': 'Feature', 'geometry': { 'type': 'LineString', " +
            "'coordinates': [ [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0] ] }, " +
            "'properties': { 'prop0': 'value0', 'prop1': 0.0 } }, " +
            "{ 'type': 'Feature', 'geometry': { 'type': 'Polygon', " +
            "'coordinates': [ [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ] ] }, " +
            "'properties': { 'prop0': 'value0', 'prop1': {'this': 'that'} } } ] }");

        assertEquals(3, features.size());
    }

    Object read(String json) {
        return r.read(json);
    }
}
