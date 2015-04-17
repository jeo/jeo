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
package io.jeo.util;

import com.vividsolutions.jts.geom.Envelope;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConvertTest {

    @Test
    public void testConvertToEnvelope() {
        Envelope e = Convert.toEnvelope("POINT (0 0)").get();
        assertEquals(0d, e.getMinX(), 0.1);
        assertEquals(0d, e.getMaxX(), 0.1);
        assertEquals(0d, e.getMinY(), 0.1);
        assertEquals(0d, e.getMaxY(), 0.1);

        e = Convert.toEnvelope("1, 2, 3, 4").get();
        assertEquals(1d, e.getMinX(), 0.1);
        assertEquals(3d, e.getMaxX(), 0.1);
        assertEquals(2d, e.getMinY(), 0.1);
        assertEquals(4d, e.getMaxY(), 0.1);
    }
}
