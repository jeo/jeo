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
package io.jeo.geobuf;

import static io.jeo.vector.VectorQuery.all;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import io.jeo.TestData;
import io.jeo.geom.Geom;
import io.jeo.vector.BasicFeature;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeobufTest {

    ByteArrayOutputStream out;

    @Before
    public void setUp() {
        out = new ByteArrayOutputStream();
    }

    @Test
    public void testPoint() throws IOException {
        writer().write(Geom.point(30, 10)).close();

        Point p = reader().point();
        assertEquals(30, p.getX(), 0.1);
        assertEquals(10, p.getY(), 0.1);
    }

    @Test
    public void testLineString() throws IOException {
        LineString l1 = Geom.lineString(30,10, 10,30, 40,40);
        writer().write(l1).close();

        LineString l2 = reader().lineString();
        assertTrue(l1.equalsExact(l2));
    }

    @Test
    public void testPolygon() throws IOException {
        Polygon p1 = Geom.build().points(30,10, 40,40, 20,40, 10,20).ring().toPolygon();
        writer().write(p1);

        Polygon p2 = reader().polygon();
        assertTrue(p1.equalsExact(p2));
    }

    @Test
    public void testPolygonWithHole() throws IOException {
        Polygon p1 = Geom.build()
            .points(35, 10, 45, 45, 15, 40, 10, 20, 35, 10).ring()
            .points(20, 30, 35, 35, 30, 20, 20, 30).ring()
            .toPolygon();

        writer().write(p1);

        Polygon p2 = reader().polygon();
        assertTrue(p1.equalsExact(p2));
    }

    @Test
    public void testMultiPoint() throws IOException {
        MultiPoint mp = Geom.build().points(10, 40, 40, 30, 20, 20, 30, 10).toMultiPoint();
        writer().write(mp);

        MultiPoint mp2 = reader().multiPoint();
        assertTrue(mp.equalsExact(mp2));
    }

    @Test
    public void testMultiLineString() throws IOException {
        MultiLineString ml1 = Geom.build()
            .points(10, 10, 20, 20, 10, 40).lineString()
            .points(40, 40, 30, 30, 40, 20, 30, 10).lineString()
            .toMultiLineString();
        writer().write(ml1);

        MultiLineString ml2 = reader().multiLineString();
        assertTrue(ml1.equalsExact(ml2));
    }

    @Test
    public void testMultiPolygon() throws IOException {
        MultiPolygon mp1 = Geom.build()
            .points(30, 20, 45, 40,10, 40, 30, 20).polygon()
            .points(15, 5, 40, 10, 10, 20, 5, 10, 15, 5).polygon()
            .toMultiPolygon();
        writer().write(mp1);

        MultiPolygon mp2 = reader().multiPolygon();
        assertTrue(mp1.equalsExact(mp2));
    }

    @Test
    public void testFeature1() throws Exception {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("geometry", Geom.build().points(100.0, 0.0, 101.0, 0.0, 101.0, 1.0, 100.0, 1.0).toPolygon());
        map.put("prop0", "value0");
        map.put("prop1", 1);
        map.put("prop2", 2.2);

        Feature f1 = new BasicFeature(null, map);
        try (GeobufWriter w = writer()) {
            w.write(f1);
        }

        Feature f2 = reader().feature();
        assertTrue(f1.geometry().equalsExact(f2.geometry()));
        assertEquals("value0", f2.get("prop0"));
        assertEquals(1l, f2.get("prop1"));
        assertEquals(2.2, f2.get("prop2"));
    }

    @Test
    public void testFeature2() throws Exception {
        Feature f1 = TestData.states().cursor(all()).first().get();
        try (GeobufWriter w = writer()) {
            w.write(f1);
        }
        Feature f2 = reader().feature();
        assertTrue(f1.geometry().equalsExact(f2.geometry()));
    }

    @Test
    public void testFeatureCollection() throws Exception {
        writer().write(TestData.states().cursor(all()));

        FeatureCursor fc = reader().featureCollection();
        assertEquals(TestData.states().cursor(all()).count(), fc.count());
    }

    GeobufWriter writer() throws IOException {
        return new GeobufWriter(out);
    }

    GeobufReader reader() throws IOException {
        return new GeobufReader(new ByteArrayInputStream(out.toByteArray()));
    }

    //@Test
    public void testData() throws Exception {
        GeobufWriter w = new GeobufWriter(Files.newOutputStream(Paths.get(
            "/Users/jdeolive/Projects/jeo/git/jeo/format/geobuf/src/test/resources/io/jeo/geobuf/states.pbf")));
        w.write(TestData.states().cursor(all())).close();
    }
}

