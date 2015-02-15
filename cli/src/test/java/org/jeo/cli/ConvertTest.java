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
package org.jeo.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.vector.VectorQuery;
import org.jeo.vector.VectorDataset;
import org.jeo.data.mem.Memory;
import org.jeo.vector.Feature;
import org.jeo.geojson.GeoJSONReader;
import org.jeo.proj.Proj;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class ConvertTest extends CLITestSupport {

    @Test
    public void testReproject() throws Exception {
        cli.handle("convert", 
            "-fc", "epsg:4326", "-tc", "epsg:900913", "mem://test#cities", "mem://test#reprojected");

        VectorDataset orig =  (VectorDataset)Memory.open("test").get("cities");

        VectorDataset repr = (VectorDataset) Memory.open("test").get("reprojected");
        assertNotNull(repr);

        assertEquals(orig.count(new VectorQuery()), repr.count(new VectorQuery()));

        CoordinateTransform tx = Proj.transform(Proj.crs("epsg:4326"), Proj.crs("epsg:900913"));

        Cursor<Feature> c = repr.cursor(new VectorQuery());
        for (Feature f : orig.cursor(new VectorQuery())) {
            assertTrue(c.hasNext());
            Geometry g = f.geometry();
            Geometry h = c.next().geometry();

            assertTrue(Proj.transform(g, tx).equals(h));
        }
    }

    @Test
    public void testMultify() throws Exception {
        cli.handle("convert", "--multify", "mem://test#cities", "mem://test#multified");

        VectorDataset mult = (VectorDataset) Memory.open("test").get("multified");
        assertNotNull(mult);

        VectorDataset orig =  (VectorDataset)Memory.open("test").get("cities");
        
        assertEquals(orig.count(new VectorQuery()), mult.count(new VectorQuery()));

        Cursor<Feature> c = mult.cursor(new VectorQuery());
        for (Feature f : orig.cursor(new VectorQuery())) {
            assertTrue(c.hasNext());
            Point g = (Point) f.geometry();
            MultiPoint h = (MultiPoint) c.next().geometry();

            assertEquals(1, h.getNumGeometries());
            assertTrue(g.equals(h.getGeometryN(0)));
        }
    }

    @Test
    public void testFilter() throws Exception {
        cli.handle("convert", "-f", "name = 'Calgary'", "mem://test#cities", "mem://test#filtered");

        VectorDataset filt = (VectorDataset) Memory.open("test").get("filtered");
        assertEquals(1, filt.count(new VectorQuery()));

        Feature f = filt.cursor(new VectorQuery()).first().get();
        assertNotNull(f);
        assertEquals("Calgary", f.get("name"));
    }

    @Test
    public void testBBOXFilter() throws Exception {
        cli.handle("convert", "-b", "-123.5,47.5,122.5,48.5", "mem://test#cities", "mem://test#filtered");

        VectorDataset filt = (VectorDataset) Memory.open("test").get("filtered");
        assertEquals(1, filt.count(new VectorQuery()));

        Feature f = filt.cursor(new VectorQuery()).first().get();
        assertNotNull(f);
        assertEquals("Vancouver", f.get("name"));
    }

    @Test
    public void testOutputAsGeoJSON() throws Exception {
        cli.handle("convert", "mem://test#cities");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        dump(bout);

        GeoJSONReader r = new GeoJSONReader();
        Cursor<Feature> c = 
            (Cursor<Feature>) r.read(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(((VectorDataset)Memory.open("test").get("cities")).count(new VectorQuery()), c.count());
    }
}
