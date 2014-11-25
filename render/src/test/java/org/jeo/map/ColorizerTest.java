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
package org.jeo.map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColorizerTest {

    @Test
    public void testDiscrete() {
        Colorizer col =
            Colorizer.build().stop(10d, RGB.red).stop(20d, RGB.green).colorizer();
        assertEquals(RGB.black, col.map(0d));
        assertEquals(RGB.red, col.map(10d));
        assertEquals(RGB.red, col.map(15d));
        assertEquals(RGB.green, col.map(20d));
        assertEquals(RGB.green, col.map(30d));
    }

    @Test
    public void testLinear() {
        Colorizer col =
            Colorizer.build().stop(10d, RGB.white, Colorizer.Mode.LINEAR).stop(20d, RGB.black).colorizer();
        assertEquals(RGB.black, col.map(0d));
        assertEquals(RGB.white, col.map(10d));

        assertEquals(RGB.white.interpolate(RGB.black, 0.5), col.map(15d));
        assertEquals(RGB.black, col.map(20d));
        assertEquals(RGB.black, col.map(30d));
    }

    @Test
    public void textExact() {
        Colorizer col =
                Colorizer.build().stop(10d, RGB.red, 2).stop(20d, RGB.green).colorizer();
        assertEquals(RGB.black, col.map(0d));
        assertEquals(RGB.red, col.map(10d));
        assertEquals(RGB.red, col.map(11d));
        assertEquals(RGB.black, col.map(12d));

        assertEquals(RGB.green, col.map(20d));
        assertEquals(RGB.green, col.map(30d));
    }
}
