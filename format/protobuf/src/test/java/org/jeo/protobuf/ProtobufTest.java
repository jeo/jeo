package org.jeo.protobuf;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.geom.Geom;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ProtobufTest {

    ByteArrayOutputStream out;

    @Before
    public void setUp() {
        out = new ByteArrayOutputStream();
    }

    @Test
    public void testPoint() throws IOException {
        writer().point(Geom.point(1, 2));

        Point p = reader().point();
        assertEquals(1, p.getX(), 0.1);
        assertEquals(2, p.getY(), 0.1);
    }

    @Test
    public void testLineString() throws IOException {
        LineString l = Geom.lineString(1,2,3,4,5,6);
        writer().lineString(l);

        LineString l2 = reader().lineString();
        assertTrue(l.equalsExact(l2));
    }

    @Test
    public void testPolygon() throws IOException {
        Polygon p = 
            Geom.build().points(2,2,4,2,4,4,2,4).ring().points(0,0,10,0,10,10,0,10,0,0).toPolygon();
        writer().polygon(p);

        Polygon p2 = reader().polygon();
        assertTrue(p.equalsExact(p2));
    }

    @Test
    public void testMultiPoint() throws IOException {
        MultiPoint mp = Geom.build().points(1,2,3,4,5,6).toMultiPoint();
        writer().multiPoint(mp);

        MultiPoint mp2 = reader().multiPoint();
        assertTrue(mp.equalsExact(mp2));
    }

    @Test
    public void testMultiLineString() throws IOException {
        MultiLineString ml = Geom.build().points(1,2,3,4,5,6).lineString()
            .points(7,8,9,10,11,12).lineString().toMultiLineString();
        writer().multiLineString(ml);

        MultiLineString ml2 = reader().multiLineString();
        assertTrue(ml.equalsExact(ml2));
    }

    @Test
    public void testMultiPolygon() throws IOException {
        MultiPolygon mp = 
            Geom.build().point(0,0).point().buffer(10).point(100,100).point().buffer(10).toMultiPolygon();
        writer().multiPolygon(mp);

        MultiPolygon mp2 = reader().multiPolygon();
        assertTrue(mp.equalsExact(mp2));
    }

    @Test
    public void testFeature() throws Exception {
        Map<String,Object> map = new LinkedHashMap<String, Object>();
        map.put("geometry", Geom.point(0,0).buffer(10));
        map.put("name", "bomb");
        map.put("cost", 1.99);

        Feature f = new BasicFeature(null, map);
        try (ProtobufWriter w = writer()) {
            w.feature(f);
        }

        Feature f2 = reader().feature(f.schema());
        assertTrue(f.geometry().equalsExact(f2.geometry()));
        assertEquals("bomb", f2.get("name"));
        assertEquals(1.99, f2.get("cost"));
    }

    ProtobufWriter writer() {
        return new ProtobufWriter(out);
    }

    ProtobufReader reader() {
        return new ProtobufReader(new ByteArrayInputStream(out.toByteArray()));
    }
}

