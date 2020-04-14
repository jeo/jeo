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
package io.jeo.map;

import static org.junit.Assert.assertEquals;

import java.util.List;

import io.jeo.util.Interpolate.Method;
import org.junit.Test;

public class RGBTest {

    @Test
    public void testInterpolate() {
        List<RGB> vals = RGB.red.interpolate(RGB.blue, 10, Method.LINEAR);
        assertRGBs(vals, 255,0,0, 255,102,0, 255,204,0, 204,255,0, 102,255,0, 0,255,0, 0,255,102,
            0,255,204, 0,204,255, 0,102,255, 0,0,255);

    }

    void assertRGBs(List<RGB> rgbs, int... values) {
        assertEquals(values.length / 3, rgbs.size());
        for (int i = 0; i < values.length; i += 3) {
            RGB rgb = rgbs.get(i/3);
            assertEquals(rgb.getRed(), values[i]);
            assertEquals(rgb.getGreen(), values[i+1]);
            assertEquals(rgb.getBlue(), values[i+2]);
        }
    }
}
