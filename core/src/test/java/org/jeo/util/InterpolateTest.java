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
package org.jeo.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class InterpolateTest {

    @Test
    public void testLinear() {
        List<Double> vals = Interpolate.linear(0, 10, 5);
        assertSequence(vals, 0,2,4,6,8,10);
    }

    @Test
    public void testExp() {
        List<Double> vals = Interpolate.exp(0d, 10d, 5);
        assertSequence(vals, 0.0, 0.62, 1.61, 3.22, 5.81, 10.0);
    }

    @Test
    public void testLog() {
        List<Double> vals = Interpolate.log(0d, 10d, 5);
        assertSequence(vals, 0.0, 2.63, 4.85, 6.78, 8.48, 10.0);
    }

    void assertSequence(List<? extends Number> vals, Number... seq) {
        assertEquals(seq.length, vals.size());

        for (int i = 0; i < vals.size(); i++) {
            assertEquals(vals.get(i).doubleValue(), seq[i].doubleValue(), 0.01);
        }
    }
}
