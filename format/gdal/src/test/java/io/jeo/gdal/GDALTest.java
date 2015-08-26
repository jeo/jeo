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
package io.jeo.gdal;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import io.jeo.Tests;
import io.jeo.geom.Bounds;
import io.jeo.raster.DataType;
import io.jeo.raster.Raster;
import io.jeo.raster.RasterQuery;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class GDALTest {

    File data;

    @BeforeClass
    public static void initGDAL() {
        try {
            GDAL.init();
        }
        catch(Throwable e) {
            Assume.assumeTrue(false);
        }
    }

    @Before
    public void setUp() throws Exception {
        Path dir = Tests.newTmpDir("gdal", "data");
        Tests.unzip(getClass().getResourceAsStream("dem.tif.zip"), dir);

        data = dir.resolve("dem.tif").toFile();
    }

    @Test
    public void testOpen() throws Exception {
        GDAL drv = new GDAL();
        assertTrue(drv.canOpen(data, null, null));

        GDALDataset ds = drv.open(data, null);
        assertNotNull(ds);
    }

    @Test
    public void testReadRaster() throws Exception {
        ByteBuffer buf = ByteBuffer.allocateDirect(10000);
        buf.order(ByteOrder.nativeOrder());
        Dataset ds = gdal.Open(data.getAbsolutePath());

        ds.ReadRaster_Direct(0, 0, 1, 1, 1, 1, gdalconst.GDT_Float32, buf, null);
    }

    /**
     * I'm not sure what the right X,Y order is... for the sample square image, it does not matter :)
     */
    private void printRaster(Raster out) {
      int idx = 0;
      int w = out.size().width();
      int h = out.size().height();
      for(int x=0; x<w; x++) {
        System.out.print( x+"> \t");
        for(int y=0; y<h; y++) {
          Object v = out.data().get(idx++);
          System.out.print( v + "\t");
        }
        System.out.println();
      }
    }

    @Test
    public void testQueryRaster() throws Exception {

      GDALDataset ds = GDAL.open(data);
      
      Raster out = ds.read(new RasterQuery());
      int count = out.data().size();
      assertEquals(count, (ds.size().width()*ds.size().height()));
      printRaster(out);
      
      Bounds smaller = ds.bounds().scale(0.5);
      
      System.out.println( ds.bounds() + " [DS] " + ds.bounds().getWidth() + " >> " +ds.bounds().getArea() );
      System.out.println( smaller + " [S1] " + smaller.getWidth() + " >> " +smaller.getArea() );

      // This is a smaller area
      assertTrue(ds.bounds().getArea()> smaller.getArea());
      ds.bounds().contains(smaller);
      
      RasterQuery q1 = new RasterQuery()
        .datatype(DataType.DOUBLE)
        .bounds(smaller);


      Raster out1 = ds.read(q1);

      System.out.println( out1.bounds() + " :: " + out1.size());

      printRaster(out1);
    }
}
