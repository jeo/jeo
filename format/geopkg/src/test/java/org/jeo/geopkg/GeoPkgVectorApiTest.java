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
package org.jeo.geopkg;

import java.io.File;

import org.jeo.Tests;
import org.jeo.vector.VectorApiTestBase;
import org.jeo.vector.VectorDataset;
import org.junit.After;

public class GeoPkgVectorApiTest extends VectorApiTestBase {

    GeoPkgWorkspace gpkg;

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File dir = Tests.newTmpDir("gpkg", "states");
        Tests.unzip(getClass().getResourceAsStream("usa.gpkg.zip"), dir);

        gpkg = GeoPackage.open(new File(dir, "usa.gpkg"));
        return (VectorDataset) gpkg.get("states");
    }

    @After
    public void tearDown() {
        gpkg.close();
    }

}
