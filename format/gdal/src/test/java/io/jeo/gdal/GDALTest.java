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
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        File dir = Tests.newTmpDir("gdal", "data");
        Tests.unzip(getClass().getResourceAsStream("dem.tif.zip"), dir);

        data = new File(dir, "dem.tif");
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
}
