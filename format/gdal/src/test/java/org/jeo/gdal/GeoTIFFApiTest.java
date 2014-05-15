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
package org.jeo.gdal;

import org.jeo.Tests;
import org.jeo.data.RasterApiTestBase;
import org.jeo.data.RasterDataset;
import org.junit.BeforeClass;

import java.io.File;

public class GeoTIFFApiTest extends RasterApiTestBase {

    @BeforeClass
    public static void initGDAL() {
        GDALTest.initGDAL();
    }

    @Override
    protected RasterDataset createRasterDataDEM() throws Exception {
        return GDALTestData.dem();
    }

    @Override
    protected RasterDataset createRasterDataRGB() throws Exception {
        return GDALTestData.rgb();
    }
}
