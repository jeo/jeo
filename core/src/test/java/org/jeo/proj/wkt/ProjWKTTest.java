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
package org.jeo.proj.wkt;

import org.jeo.proj.Proj;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;

public class ProjWKTTest {
    
    @Test
    public void testGeographic() throws Exception {
        CoordinateReferenceSystem crs = Proj.crs("epsg:4326");
        new ProjWKTEncoder().encode(crs, false);
    }

    @Test
    public void testProjected() throws Exception {
        CoordinateReferenceSystem crs = Proj.crs("epsg:3157");
        new ProjWKTEncoder().encode(crs, false);
    }
}
