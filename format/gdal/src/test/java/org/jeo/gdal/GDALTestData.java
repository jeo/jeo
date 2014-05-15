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
import org.jeo.data.RasterDataset;

import java.io.File;

/**
 * Utility class exposing GDAL test datasets.
 */
public class GDALTestData {

    /**
     * Determines if GDAL is avaialble, any class using this test data should call this.
     */
    public static boolean isAvailable() {
        try {
            GDAL.init();
            return true;
        }
        catch(Throwable t) {
            return false;
        }
    }

    /**
     * Returns the same dataset as described by {@link org.jeo.TestData#dem()}.
     */
    public static RasterDataset dem() throws Exception {
        File dir = Tests.newTmpDir("gdal", "data");
        Tests.unzip(GDALTestData.class.getResourceAsStream("dem.tif.zip"), dir);

        return GeoTIFF.open(new File(dir, "dem.tif"));
    }

    /**
     * Returns the same dataset as described by {@link org.jeo.TestData#rgb()}.
     */
    public static RasterDataset rgb() throws Exception {
        File dir = Tests.newTmpDir("gdal", "data");
        Tests.unzip(GDALTestData.class.getResourceAsStream("rgb.tif.zip"), dir);

        return GeoTIFF.open(new File(dir, "rgb.tif"));
    }
}
