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
package io.jeo.geom;

import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

import java.util.Iterator;

public class BoundsTest {

    @Test
    public void testScale() {
        Envelope e = new Envelope(0,10,0,10);

        assertEnvelope(Bounds.scale(e, 0.5), 2.5, 7.5, 2.5, 7.5);
        assertEnvelope(Bounds.scale(e, 2), -5.0, 15.0, -5.0, 15.0);
        assertEnvelope(Bounds.scale(e, 1), 0, 10, 0, 10);
    }

    @Test
    public void testTranslate() {
        Envelope e = new Envelope(0,10,0,10);
        assertEnvelope(Bounds.translate(e, 5, 5), 5, 15, 5, 15);
        assertEnvelope(Bounds.translate(e, 5, -5), 5, 15, -5, 5);
        assertEnvelope(Bounds.translate(e, -5, 5), -5, 5, 5, 15);
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
        Envelope r = Bounds.random(e, 0.5f);
        assertTrue(e.contains(r));
        assertEquals(0.5f, r.getWidth() / e.getWidth(), 0.1);
        assertEquals(0.5f, r.getHeight() / e.getHeight(), 0.1);
    }

    @Test
    public void testToString() {
        Envelope e = new Envelope(1,2,3,4);
        assertTrue(Bounds.toString(e).matches("1\\.0+,3\\.0+,2\\.0+,4\\.0+"));
        assertTrue(Bounds.toString(e, " ", false).matches("1\\.0+ 2\\.0+ 3\\.0+ 4\\.0+"));
    }

    @Test
    public void testParse() {
        Envelope e = Bounds.parse("1 2 3 4", true, " ");
        assertEquals(e.getMinX(), 1d, 0.1);
        assertEquals(e.getMinY(), 2d, 0.1);
        assertEquals(e.getMaxX(), 3d, 0.1);
        assertEquals(e.getMaxY(), 4d, 0.1);

        e = Bounds.parse("1.2.3.4", false, "\\.");
        assertEquals(e.getMinX(), 1d, 0.1);
        assertEquals(e.getMinY(), 3d, 0.1);
        assertEquals(e.getMaxX(), 2d, 0.1);
        assertEquals(e.getMaxY(), 4d, 0.1);
    }

    @Test
    public void testTile() {
        Envelope e = new Envelope(-180,180,-90,90);
        Iterator<Envelope> t = Bounds.tile(e, 0.5, 0.5, null).iterator();

        assertTrue(t.hasNext()); assertEquals(new Envelope(-180,0,-90,0), t.next());
        assertTrue(t.hasNext()); assertEquals(new Envelope(0,180,-90,0), t.next());
        assertTrue(t.hasNext()); assertEquals(new Envelope(-180,0,0,90), t.next());
        assertTrue(t.hasNext()); assertEquals(new Envelope(0,180,0,90), t.next());
        assertFalse(t.hasNext());
    }
}
