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
package org.jeo.geom;

import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class EnvelopesTest {

    @Test
    public void testScale() {
        Envelope e = new Envelope(0,10,0,10);

        assertEnvelope(Envelopes.scale(e, 0.5), 2.5, 7.5, 2.5, 7.5);
        assertEnvelope(Envelopes.scale(e, 2), -5.0, 15.0, -5.0, 15.0);
        assertEnvelope(Envelopes.scale(e, 1), 0, 10, 0, 10);
    }

    @Test
    public void testTranslate() {
        Envelope e = new Envelope(0,10,0,10);
        assertEnvelope(Envelopes.translate(e, 5, 5), 5, 15, 5, 15);
        assertEnvelope(Envelopes.translate(e, 5, -5), 5, 15, -5, 5);
        assertEnvelope(Envelopes.translate(e, -5, 5), -5, 5, 5, 15);
    }

    void assertEnvelope(Envelope e, double x1, double x2, double y1, double y2) {
        assertEquals(x1, e.getMinX(), 1e-6);
        assertEquals(x2, e.getMaxX(), 1e-6);
        assertEquals(y1, e.getMinY(), 1e-6);
        assertEquals(y2, e.getMaxY(), 1e-6);
    }

    @Test
    public void testRandom() {
        Envelope e = new Envelope(0,10,0,10);
        Envelope r = Envelopes.random(e, 0.5f);
        assertTrue(e.contains(r));
        assertEquals(0.5f, r.getWidth() / e.getWidth(), 0.1);
        assertEquals(0.5f, r.getHeight() / e.getHeight(), 0.1);
    }

    @Test
    public void testToString() {
        Envelope e = new Envelope(1,2,3,4);
        assertTrue(Envelopes.toString(e).matches("1\\.0+,3\\.0+,2\\.0+,4\\.0+"));
        assertTrue(Envelopes.toString(e, " ", false).matches("1\\.0+ 2\\.0+ 3\\.0+ 4\\.0+"));
    }
}
