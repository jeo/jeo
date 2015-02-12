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
import org.jeo.vector.Query;
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
            "-fc", "epsg:4326", "-tc", "epsg:900913", "mem://#cities", "mem://#reprojected");

        VectorDataset orig =  (VectorDataset)Memory.open().get("cities");

        VectorDataset repr = (VectorDataset) Memory.open().get("reprojected");
        assertNotNull(repr);

        assertEquals(orig.count(new Query()), repr.count(new Query()));

        CoordinateTransform tx = Proj.transform(Proj.crs("epsg:4326"), Proj.crs("epsg:900913"));

        Cursor<Feature> c = repr.cursor(new Query());
        for (Feature f : orig.cursor(new Query())) {
            assertTrue(c.hasNext());
            Geometry g = f.geometry();
            Geometry h = c.next().geometry();

            assertTrue(Proj.transform(g, tx).equals(h));
        }
    }

    @Test
    public void testMultify() throws Exception {
        cli.handle("convert", "--multify", "mem://#cities", "mem://#multified");

        VectorDataset mult = (VectorDataset) Memory.open().get("multified");
        assertNotNull(mult);

        VectorDataset orig =  (VectorDataset)Memory.open().get("cities");
        
        assertEquals(orig.count(new Query()), mult.count(new Query()));

        Cursor<Feature> c = mult.cursor(new Query());
        for (Feature f : orig.cursor(new Query())) {
            assertTrue(c.hasNext());
            Point g = (Point) f.geometry();
            MultiPoint h = (MultiPoint) c.next().geometry();

            assertEquals(1, h.getNumGeometries());
            assertTrue(g.equals(h.getGeometryN(0)));
        }
    }

    @Test
    public void testFilter() throws Exception {
        cli.handle("convert", "-f", "name = 'Calgary'", "mem://#cities", "mem://#filtered");

        VectorDataset filt = (VectorDataset) Memory.open().get("filtered");
        assertEquals(1, filt.count(new Query()));

        Feature f = Cursors.first(filt.cursor(new Query()));
        assertNotNull(f);
        assertEquals("Calgary", f.get("name"));
    }

    @Test
    public void testBBOXFilter() throws Exception {
        cli.handle("convert", "-b", "-123.5,47.5,122.5,48.5", "mem://#cities", "mem://#filtered");

        VectorDataset filt = (VectorDataset) Memory.open().get("filtered");
        assertEquals(1, filt.count(new Query()));

        Feature f = Cursors.first(filt.cursor(new Query()));
        assertNotNull(f);
        assertEquals("Vancouver", f.get("name"));
    }

    @Test
    public void testOutputAsGeoJSON() throws Exception {
        cli.handle("convert", "mem://#cities");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        dump(bout);

        GeoJSONReader r = new GeoJSONReader();
        Cursor<Feature> c = 
            (Cursor<Feature>) r.read(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(((VectorDataset)Memory.open().get("cities")).count(new Query()), 
            Cursors.size(c));
    }
}
