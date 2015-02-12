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
package org.jeo.nano;

import org.jeo.proj.Proj;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.osgeo.proj4j.CoordinateReferenceSystem;


public class WMTSHandlerTest extends HandlerTestSupport {

    MockServer server;

    @Before
    public void init() {
        handler = new WMTSHandler();
    }

    @Test
    public void testPattern() {
        assertTrue(handler.canHandle(new Request("/wmts", "GET", q("service","wmts")), null));
        assertTrue(handler.canHandle(new Request("/wmts/", "GET", q("service","wmts")), null));
    }

    @Test
    public void testComputeScaleDenominator() throws Exception {
        // these values are taken from well-known scale sets
        // provided in appendix E of the OGC WMTS 1.0.0 specification
        CoordinateReferenceSystem crs = Proj.EPSG_900913;
        // 'table' of scaleDenominator|pixelSize (1st 2 and last 2 zoom levels)
        double[] vals = new double[] {
            559082264.0287178, 156543.0339280410,
            279541132.0143589, 78271.51696402048,
            4265.459167699568, 1.194328566955879,
            2132.729583849784, 0.5971642834779395
        };
        for (int i = 0; i < vals.length; i+=2) {
            assertEquals(vals[i], WMTSHandler.computeScaleDenominator(vals[i+1], crs), 1e-5);
        }

        crs = Proj.EPSG_4326;
        vals = new double[] {
            279541132.0143589, 0.703125,
            139770566.0071794, 0.3515625,
        };
        for (int i = 0; i < vals.length; i+=2) {
            assertEquals(vals[i], WMTSHandler.computeScaleDenominator(vals[i+1], crs), 1e-5);
        }
    }

}
