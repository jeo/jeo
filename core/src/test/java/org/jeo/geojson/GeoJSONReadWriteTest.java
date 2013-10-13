package org.jeo.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;
import org.jeo.geom.GeomBuilder;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJSONReadWriteTest {

    GeoJSONReader reader;
    GeoJSONWriter writer;

    GeometryFactory gf;
    GeomBuilder gb;

    @Before
    public void setUp() {
        reader = new GeoJSONReader();
        writer = new GeoJSONWriter(new StringWriter());
        gf = new GeometryFactory();
        gb = new GeomBuilder(gf);
    }

    @Test
    public void testParsePoint() throws Exception {
        assertTrue(point().equals(reader.point(pointText())));
        assertTrue(point3d().equals(reader.point(point3dText())));
    }

    @Test
    public void testEncodePoint() throws Exception {
        assertEquals(pointText(), toString(writer.point(point())));
        assertEquals(point3dText(), toString(writer.point(point3d())));
    }

    @Test
    public void testParseLineString() throws Exception {
        assertTrue(line().equals(reader.lineString(lineText())));
        assertTrue(line3d().equals(reader.lineString(line3dText())));
    }

    @Test
    public void testEncodeLineString() throws Exception {
        assertEquals(lineText(), toString(writer.lineString(line())));
        assertEquals(line3dText(), toString(writer.lineString(line3d())));
    }

    @Test
    public void testParsePolygon() throws Exception {
        assertTrue(polygon1().equals(reader.polygon(polygonText1())));
        assertTrue(polygon2().equals(reader.polygon(polygonText2())));
        assertTrue(polygon3d().equals(reader.polygon(polygonText3d())));
    }

    @Test
    public void testEncodePolygon() throws Exception {
        assertEquals(polygonText1(), toString(writer.polygon(polygon1())));
        assertEquals(polygonText2(), toString(writer.polygon(polygon2())));
        assertEquals(polygonText3d(), toString(writer.polygon(polygon3d())));
    }

    @Test
    public void testParseMultiPoint() throws Exception {
        assertTrue(multiPoint().equals(reader.multiPoint(multiPointText())));
        assertTrue(multiPoint3d().equals(reader.multiPoint(multiPoint3dText())));
    }

    @Test
    public void testEncodeMultiPoint() throws Exception {
        assertEquals(multiPointText(), toString(writer.multiPoint(multiPoint())));
        assertEquals(multiPoint3dText(), toString(writer.multiPoint(multiPoint3d())));
    }

    @Test
    public void testParseMultiLineString() throws Exception {
        assertTrue(multiLine().equals(reader.multiLineString(multiLineText())));
        assertTrue(multiLine3d().equals(reader.multiLineString(multiLine3dText())));
    }

    @Test
    public void testEncodeMultiLineString() throws Exception {
        assertEquals(multiLineText(), toString(writer.multiLineString(multiLine())));
        assertEquals(multiLine3dText(), toString(writer.multiLineString(multiLine3d())));
    }

    @Test
    public void testParseMultiPolygon() throws Exception {
        assertTrue(multiPolygon().equals(reader.multiPolygon(multiPolygonText())));
        assertTrue(multiPolygon3d().equals(reader.multiPolygon(multiPolygon3dText())));
    }

    @Test
    public void testEncodeMultiPolygon() throws Exception {
        assertEquals(multiPolygonText(), toString(writer.multiPolygon(multiPolygon())));
        assertEquals(multiPolygon3dText(), toString(writer.multiPolygon(multiPolygon3d())));
    }

    @Test
    public void testParseGeometryCollection() throws Exception {
        assertEquals(collection(), reader.geometryCollection(collectionText()));
    }

    @Test
    public void testEncodeGeometryCollection() throws Exception {
        assertEquals(collectionText(), toString(writer.geometryCollection(collection())));
        //assertEquals(collection3dText(), toString(writer.geometryCollection(collection3d())));
    }

    @Test
    public void testParseFeature() throws Exception {
        Feature f = reader.feature(featureText(1));
        assertNotNull(f);
        
        assertTrue(f.get("geometry") instanceof Point);
        assertEquals(1l, f.get("int"));
        assertEquals(1.1, (Double) f.get("double"), 0.1);
        assertEquals("one", f.get("string"));
        assertEquals("feature.1", f.getId());
        assertNotNull(f.getCRS());
    }

    @Test
    public void testParseFeature2() throws Exception {
        Feature f = (Feature) reader.read(featureText(1));
        assertNotNull(f);
        
        assertTrue(f.get("geometry") instanceof Point);
        assertEquals(1l, f.get("int"));
        assertEquals(1.1, (Double) f.get("double"), 0.1);
        assertEquals("one", f.get("string"));
        assertEquals("feature.1", f.getId());
        assertNotNull(f.getCRS());
    }

    @Test
    public void testParseFeatureCollection() throws Exception {
        Cursor<Feature> c = reader.features(featureCollectionText());

        assertTrue(c.hasNext());
        Feature f = c.next();
        assertEquals("zero", f.get("string"));

        assertTrue(c.hasNext());
        f = c.next();
        assertEquals("one", f.get("string"));

        assertTrue(c.hasNext());
        f = c.next();
        assertEquals("two", f.get("string"));

        assertFalse(c.hasNext());
    }

    @Test
    public void testParseFeatureCollection2() throws Exception {
        Cursor<Feature> c = (Cursor<Feature>) reader.read(featureCollectionText());

        assertTrue(c.hasNext());
        Feature f = c.next();
        assertEquals("zero", f.get("string"));

        assertTrue(c.hasNext());
        f = c.next();
        assertEquals("one", f.get("string"));

        assertTrue(c.hasNext());
        f = c.next();
        assertEquals("two", f.get("string"));

        assertFalse(c.hasNext());
    }
    
    String pointText() {
        return strip("{'type': 'Point','coordinates':[100.1,0.1]}");
    }

    Point point() {
        return gb.point(100.1, 0.1).toPoint();
    }

    String point3dText() {
        return strip("{'type': 'Point','coordinates':[100.1,0.1,10.2]}");
    }

    Point point3d() {
        return gb.pointz(100.1, 0.1, 10.2).toPoint();
    }

    String lineText() {
        return strip(
            "{'type': 'LineString', 'coordinates': [[100.1,0.1],[101.1,1.1]]}");
    }

    LineString line() {
        return gb.points(100.1, 0.1, 101.1,1.1).toLineString();
    }

    String line3dText() {
        return strip(
            "{'type': 'LineString', 'coordinates': [[100.1,0.1,10.2],[101.1,1.1,10.2]]}");
    }

    LineString line3d() {
        return gb.pointsz(100.1, 0.1, 10.2, 101.1,1.1, 10.2).toLineString();
    }

    Polygon polygon1() {
        return gb.points(100.1, 0.1, 101.1, 0.1, 101.1, 1.1, 100.1, 1.1, 100.1, 0.1).ring().toPolygon();
    }

    String polygonText1() {
        return strip("{ 'type': 'Polygon',"+
        "'coordinates': ["+
        "  [ [100.1, 0.1], [101.1, 0.1], [101.1, 1.1], [100.1, 1.1], [100.1, 0.1] ]"+
        "  ]"+
         "}");
    }

    String polygonText2() {
        return strip("{ 'type': 'Polygon',"+
        "    'coordinates': ["+
        "      [ [100.1, 0.1], [101.1, 0.1], [101.1, 1.1], [100.1, 1.1], [100.1, 0.1] ],"+
        "      [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ]"+
        "      ]"+
        "   }");
    }

    Polygon polygon2() {
        return gb.points(100.1, 0.1, 101.1, 0.1, 101.1, 1.1, 100.1, 1.1, 100.1, 0.1).ring()
          .points(100.2, 0.2, 100.8, 0.2, 100.8, 0.8, 100.2, 0.8, 100.2, 0.2).ring().toPolygon();
    }

    String polygonText3d() {
        return strip("{ 'type': 'Polygon',"+
        "    'coordinates': ["+
        "      [ [100.1, 0.1, 10.2], [101.1, 0.1, 11.2], [101.1, 1.1, 11.2], [100.1, 1.1, 10.2], [100.1, 0.1, 10.2] ],"+
        "      [ [100.2, 0.2, 10.2], [100.8, 0.2, 11.2], [100.8, 0.8, 11.2], [100.2, 0.8, 10.2], [100.2, 0.2, 10.2] ]"+
        "      ]"+
        "   }");
    }
    
    Polygon polygon3d() {
        return gb.pointsz(100.1, 0.1, 10.2, 101.1, 0.1, 11.2, 101.1, 1.1, 11.2, 100.1, 1.1, 10.2, 100.1, 0.1, 10.2)
            .ring().pointsz(100.2, 0.2, 10.2, 100.8, 0.2, 11.2, 100.8, 0.8, 11.2, 100.2, 0.8, 10.2, 100.2, 0.2, 10.2)
            .ring().toPolygon();
    }

    String multiPointText() {
        return strip(
            "{ 'type': 'MultiPoint',"+
                "'coordinates': [ [100.1, 0.1], [101.1, 1.1] ]"+
            "}");
    }

    MultiPoint multiPoint() {
        return gb.points(100.1, 0.1, 101.1, 1.1).toMultiPoint();
    }
    
    String multiPoint3dText() {
        return strip(
            "{ 'type': 'MultiPoint',"+
                "'coordinates': [ [100.1, 0.1, 10.2], [101.1, 1.1, 11.2] ]"+
            "}");
    }

    MultiPoint multiPoint3d() {
        return gb.pointsz(100.1, 0.1, 10.2, 101.1, 1.1, 11.2).toMultiPoint();
    }

    String multiLineText() {
        return strip(
            "{ 'type': 'MultiLineString',"+
            "    'coordinates': ["+
            "        [ [100.1, 0.1], [101.1, 1.1] ],"+
            "        [ [102.1, 2.1], [103.1, 3.1] ]"+
            "      ]"+
            "    }");
    }

    MultiLineString multiLine() {
        return gb.points(100.1, 0.1, 101.1, 1.1).lineString()
          .points(102.1, 2.1, 103.1, 3.1).lineString().toMultiLineString();
    }
    
    String multiLine3dText() {
        return strip(
            "{ 'type': 'MultiLineString',"+
            "    'coordinates': ["+
            "        [ [100.1, 0.1, 10.2], [101.1, 1.1, 10.2] ],"+
            "        [ [102.1, 2.1, 11.2], [103.1, 3.1, 11.2] ]"+
            "      ]"+
            "    }");
    }

    MultiLineString multiLine3d() {
        return gb.pointsz(100.1, 0.1, 10.2, 101.1, 1.1, 10.2).lineString()
            .pointsz(102.1, 2.1, 11.2, 103.1, 3.1, 11.2).lineString().toMultiLineString();
    }

    String multiPolygonText() {
        return strip(
        "{ 'type': 'MultiPolygon',"+
        "    'coordinates': ["+
        "      [[[102.1, 2.1], [103.1, 2.1], [103.1, 3.1], [102.1, 3.1], [102.1, 2.1]]],"+
        "      [[[100.1, 0.1], [101.1, 0.1], [101.1, 1.1], [100.1, 1.1], [100.1, 0.1]],"+
        "       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]"+
        "      ]"+
        "    }");
    }

    MultiPolygon multiPolygon() {
        return gb.points(102.1, 2.1,103.1, 2.1,103.1, 3.1,102.1, 3.1,102.1, 2.1).ring().polygon()
            .points(100.1, 0.1, 101.1, 0.1, 101.1, 1.1, 100.1, 1.1, 100.1, 0.1).ring()
            .points(100.2, 0.2, 100.8, 0.2, 100.8, 0.8, 100.2, 0.8, 100.2, 0.2).ring().polygon()
            .toMultiPolygon();
    }

    String multiPolygon3dText() {
        return strip(
        "{ 'type': 'MultiPolygon',"+
        "    'coordinates': ["+
        "      [[[102.1, 2.1, 10.2], [103.1, 2.1, 10.2], [103.1, 3.1, 10.2], [102.1, 3.1, 10.2], [102.1, 2.1, 10.2]]],"+
        "      [[[100.1, 0.1, 10.2], [101.1, 0.1, 10.2], [101.1, 1.1, 10.2], [100.1, 1.1, 10.2], [100.1, 0.1, 10.2]],"+
        "       [[100.2, 0.2, 10.2], [100.8, 0.2, 10.2], [100.8, 0.8, 10.2], [100.2, 0.8, 10.2], [100.2, 0.2, 10.2]]]"+
        "      ]"+
        "    }");
    }

    MultiPolygon multiPolygon3d() {
        return gb.pointsz(102.1, 2.1, 10.2, 103.1, 2.1, 10.2, 103.1, 3.1, 10.2, 102.1, 3.1, 10.2, 102.1, 2.1, 10.2).ring().polygon()
            .pointsz(100.1, 0.1, 10.2, 101.1, 0.1, 10.2, 101.1, 1.1, 10.2, 100.1, 1.1, 10.2, 100.1, 0.1, 10.2).ring()
            .pointsz(100.2, 0.2, 10.2, 100.8, 0.2, 10.2, 100.8, 0.8, 10.2, 100.2, 0.8, 10.2, 100.2, 0.2, 10.2).ring().polygon()
            .toMultiPolygon();

    }

    String collectionText() {
        return strip(
            "{ 'type': 'GeometryCollection',"+
            "    'geometries': ["+
            "      { 'type': 'Point',"+
            "        'coordinates': [100.1, 0.1]"+
            "        },"+
            "      { 'type': 'LineString',"+
            "        'coordinates': [ [101.1, 0.1], [102.1, 1.1] ]"+
            "        }"+
            "    ]"+
            "  }");
    }

    GeometryCollection collection() {
        return gb.point(100.1,0.1).point().points(101.1, 0.1, 102.1, 1.1).lineString().toCollection();
    }

    Feature feature(int val) {
        List<Object> l = new ArrayList<Object>(); 
        l.add(new GeomBuilder().point(val+0.1, val+0.1).toPoint());
        l.add(val);
        l.add(val + 0.1);
        l.add(toString(val));
        
        return new ListFeature("feature."+val, l);
    }

    String featureText(int val) {
        String text = 
        "{" +
        "  'type': 'Feature'," +
        "  'geometry': {" +
        "     'type': 'Point'," +
        "     'coordinates': [" + (val+0.1) + "," + (val+0.1) + "]" +
        "   }, " +
        "  'properties': {" +
        "     'int': " + val + "," +
        "     'double': " + (val + 0.1) + "," +
        "     'string': '" + toString(val) + "'" + 
        "   }," +
        "  'crs': {" + 
        "    'properties': {" +
        "      'name': 'EPSG:4326'" + 
        "     }," +
        "   }, " + 
        "   'id':'feature." + val + "'" +
        "}";
        
        return strip(text);
    }

    List<Feature> featureCollection() {
        List<Feature> collection = new ArrayList<Feature>();
        for (int i = 0; i < 3; i++) {
            collection.add(feature(i));
        }
        return collection;
    }

    String featureCollectionText() {
        return featureCollectionText(false,false);
    }
    
    String featureCollectionText(boolean withBounds, boolean withCRS) {
        return featureCollectionText(withBounds, withCRS, false);
    }
    
    String featureCollectionText(boolean withBounds, boolean withCRS, boolean crsAfter) {
        StringBuffer sb = new StringBuffer();
        sb.append("{'type':'FeatureCollection',");
        if (withBounds) {
            List<Feature> features = featureCollection();
            Envelope bbox;
            try {
                bbox = Cursors.extent(Cursors.create(features));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sb.append("'bbox': [");
            sb.append(bbox.getMinX()).append(",").append(bbox.getMinY()).append(",")
                .append(bbox.getMaxX()).append(",").append(bbox.getMaxY());
            sb.append("],");
        }
        if (withCRS && !crsAfter) {
            sb.append("'crs': {");
            sb.append("  'type': 'name',");
            sb.append("  'properties': {");
            sb.append("    'name': 'EPSG:4326'");
            sb.append("   }");
            sb.append("},");
        }
        sb.append("'features':[");
        for (int i = 0; i < 3; i++) {
            sb.append(featureText(i)).append(",");
        }
        sb.setLength(sb.length()-1);
        sb.append("]");
        if (withCRS && crsAfter) {
            sb.append(",'crs': {");
            sb.append("  'type': 'name',");
            sb.append("  'properties': {");
            sb.append("    'name': 'EPSG:4326'");
            sb.append("   }");
            sb.append("}");
        }
        sb.append("}");
        return strip(sb.toString());
    }

    String toString(GeoJSONWriter writer) {
        StringWriter w = (StringWriter) writer.getWriter();
        this.writer = new GeoJSONWriter(new StringWriter());
        return w.toString();
    }

    String strip(String json) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ' ' || c == '\n') continue;
            if (c == '\'') {
                sb.append("\"");
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    String toString(int val) {
        return val == 0 ? "zero" : 
            val == 1 ? "one" :
            val == 2 ? "two" : 
            val == 3 ? "three" : "four";
    }

}
