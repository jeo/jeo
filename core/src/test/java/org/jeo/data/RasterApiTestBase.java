/* Copyright 2014 The jeo project. All rights reserved.
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
package org.jeo.data;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import org.jeo.TestData;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.jeo.raster.Band;

import org.jeo.raster.DataType;
import org.jeo.raster.Raster;
import org.jeo.raster.Stats;
import org.jeo.util.Dimension;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Abstract test case that exercises all aspects of the {@link org.jeo.data.RasterDataset} interface.
 * <p>
 * This test uses the {@link TestData#dem()} and {@link TestData#rgb()} datasets as a basis for testing.
 * Test implementers must override {@link #createRasterDataDEM()} and {@link #createRasterDataRGB()} and
 * return the appropriate test data.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class RasterApiTestBase {

    RasterDataset dem;
    RasterDataset rgb;

    @Before
    public final void setUp() throws Exception {
        init();
        dem = createRasterDataDEM();
        rgb = createRasterDataRGB();
    }

    protected void init() throws Exception {
    }

    protected abstract RasterDataset createRasterDataDEM() throws Exception;

    protected abstract RasterDataset createRasterDataRGB() throws Exception;

    @Test
    public void testGetName() {
        assertEquals("dem", dem.getName());
    }

    @Test
    public void testSize() throws IOException {
        Dimension size = dem.size();
        assertEquals(Integer.valueOf(10), size.width());
        assertEquals(Integer.valueOf(10), size.height());
    }

    @Test
    public void testBounds() throws IOException {
        Envelope bbox = dem.bounds();
        assertNotNull(bbox);

        assertEquals(589980.0, bbox.getMinX(), 0.1);
        assertEquals(4913700.0, bbox.getMinY(), 0.1);
        assertEquals(609000.0, bbox.getMaxX(), 0.1);
        assertEquals(4928010.0, bbox.getMaxY(), 0.1);
    }

    @Test
    public void testCRS() throws IOException {
        CoordinateReferenceSystem crs = dem.crs();
        assertNotNull(crs);

        CoordinateReferenceSystem geo = Proj.EPSG_4326;
        Point p = Proj.reproject(Geom.point(-225518.34, 5709856.02), crs, geo);
        assertEquals(-115.37, p.getX(), 0.01);
        assertEquals(51.08, p.getY(), 0.01);
    }

    @Test
    public void testBands() throws IOException {
        List<Band> bands = dem.bands();
        assertNotNull(bands);
        assertEquals(1, bands.size());

        Band b = bands.get(0);
        assertEquals(Band.Color.GRAY, b.color());
    }

    @Test
    public void testStats() throws IOException {
        Band b = dem.bands().get(0);
        Stats stats = b.stats();
        assertNotNull(stats);

        assertEquals(1098.0, stats.min(), 0.1);
        assertEquals(1746.0, stats.max(), 0.1);
        assertEquals(1355.7, stats.mean(), 0.1);
        assertEquals(182.9, stats.stdev(), 0.1);
   }

    @Test
    public void testRead() throws IOException {
        // read all data
        ByteBuffer buf = dem.read(new RasterQuery().datatype(DataType.FLOAT)
            .bounds(new Envelope(589980.0, 609000.0, 4913700.0, 4928010.0))).data().buffer();
        assertNotNull(buf);

        FloatBuffer fb = buf.asFloatBuffer();
        assertEquals(100, fb.limit());
        assertEquals(1099.0, fb.get(0), 0.1);
        assertEquals(1234.0, fb.get(1), 0.1);
        assertEquals(1393.0, fb.get(98), 0.1);
        assertEquals(1461.0, fb.get(99), 0.1);

        // read subset
        buf = dem.read(new RasterQuery().datatype(DataType.FLOAT)
            .bounds(new Envelope(589980.0, 599490.0, 4920855.0, 4928010.0))).data().buffer();
        assertNotNull(buf);

        fb = buf.asFloatBuffer();
        assertEquals(100, fb.limit());

        assertEquals(1099.0, fb.get(0), 0.1);
        assertEquals(1099.0, fb.get(1), 0.1);
        assertEquals(1234.0, fb.get(2), 0.1);
        assertEquals(1234.0, fb.get(3), 0.1);
        assertEquals(1099.0, fb.get(10), 0.1);
        assertEquals(1099.0, fb.get(11), 0.1);
        assertEquals(1234.0, fb.get(12), 0.1);
        assertEquals(1234.0, fb.get(13), 0.1);

        assertEquals(1369.0, fb.get(86), 0.1);
        assertEquals(1369.0, fb.get(87), 0.1);
        assertEquals(1326.0, fb.get(88), 0.1);
        assertEquals(1326.0, fb.get(89), 0.1);
        assertEquals(1369.0, fb.get(96), 0.1);
        assertEquals(1369.0, fb.get(97), 0.1);
        assertEquals(1326.0, fb.get(98), 0.1);
        assertEquals(1326.0, fb.get(99), 0.1);

        // read superset
//        buf = dem.read(new RasterQuery().datatype(DataType.FLOAT)
//            .bounds(new Envelope(580470.0, 618510.0, 4906545.0, 4935165.0)));
//        assertNotNull(buf);
//
//        fb = buf.asFloatBuffer();
//        assertEquals(100, fb.limit());
//        assertEquals(0.0, fb.get(), 0.1);
//        assertEquals(0.0, fb.get(), 0.1);
//
//        assertNotEquals(0, fb.get(22), 0.1);
//        assertNotEquals(0, fb.get(66), 0.1);
//        assertEquals(0.0, fb.get(67), 0.1);
    }

    @Test
    public void testReadRGB() throws IOException {
        ByteBuffer buf = rgb.read(new RasterQuery()
            .datatype(DataType.INT).bounds(new Envelope(-180, 180, -90, 90))).data().buffer();
        assertNotNull(buf);

        IntBuffer ibuf = buf.asIntBuffer();
        assertEquals(4, ibuf.limit());
        assertEquals(255, ibuf.get());
        assertEquals(65280, ibuf.get());
        assertEquals(16711680, ibuf.get());
        assertEquals(8421504, ibuf.get());
    }

    @Test
    public void testReproject() throws IOException {
        Assume.assumeTrue("Driver does not support reprojection",
            ((RasterDriver)dem.getDriver()).supports(RasterDriver.Capability.REPROJECT));

        RasterQuery q = new RasterQuery()
            .size(10,10)
            .bounds(new Envelope(-103.871006154, -103.629326769, 44.37021187, 44.5016256196))
            .crs(Proj.EPSG_4326);

        Raster ras = dem.read(q);
        ByteBuffer buf = ras.data().buffer();

        FloatBuffer fb = buf.asFloatBuffer();
        assertEquals(100, fb.limit());
    }
}
