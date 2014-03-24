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
package org.jeo.data;

import static org.junit.Assert.*;

import org.jeo.geom.Envelopes;
import org.jeo.tile.Tile;
import org.jeo.tile.TileCover;
import org.jeo.tile.TilePyramid;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class TilePyramidTest {

    TilePyramid tp;

    @Before
    public void setUp() {
        tp = TilePyramid.build().grid(2, 1).grid(4, 2).grid(8, 4).pyramid(); 
    }

    @Test
    public void testBounds() {
        assertEnvelope(tp.bounds(new Tile(0, 0, 0, null, null)), -180, -90, 0, 90);
        assertEnvelope(tp.bounds(new Tile(0, 1, 0, null, null)),  0, -90, 180, 90);

        assertEnvelope(tp.bounds(new Tile(1, 0, 0, null, null)), -180.0, -90.0, -90.0, 0.0);
        assertEnvelope(tp.bounds(new Tile(1, 1, 0, null, null)),  -90.0, -90.0, 0.0, 0.0);
        assertEnvelope(tp.bounds(new Tile(1, 2, 0, null, null)),  0.0, -90.0, 90.0, 0.0);
        assertEnvelope(tp.bounds(new Tile(1, 3, 0, null, null)),  90.0, -90.0, 180.0, 0.0);
        assertEnvelope(tp.bounds(new Tile(1, 0, 1, null, null)), -180.0, 0.0, -90.0, 90.0);
        assertEnvelope(tp.bounds(new Tile(1, 1, 1, null, null)), -90.0, 0.0, 0.0, 90.0);
        assertEnvelope(tp.bounds(new Tile(1, 2, 1, null, null)),  0.0, 0.0, 90.0, 90.0);
        assertEnvelope(tp.bounds(new Tile(1, 3, 1, null, null)),  90.0, 0.0, 180.0, 90.0);

        assertEnvelope(tp.bounds(new Tile(0, 2, 0, null, null)), 180.0, -90.0, 360, 90);
        assertEnvelope(tp.bounds(new Tile(0, 0, 1, null, null)), -180.0, 90.0, 0, 270);

        try {
            tp.bounds(new Tile(3, 0, 0, null, null));
            fail();
        }
        catch(IllegalArgumentException e) {
        }
    }

    @Test
    public void testCover() {
        Envelope b = tp.getBounds();

        TileCover cov = tp.cover(b, 1);
        assertEquals(4, cov.getWidth());
        assertEquals(2, cov.getHeight());
        assertEquals(0, cov.getX0());
        assertEquals(0, cov.getY0());
        assertEquals(3, cov.getX1());
        assertEquals(1, cov.getY1());
        
        cov = tp.cover(
            Envelopes.translate(b, -b.getWidth()/2f, -b.getHeight()/2f), 1);
        assertEquals(4, cov.getWidth());
        assertEquals(2, cov.getHeight());
        assertEquals(-2, cov.getX0());
        assertEquals(-1, cov.getY0());
        assertEquals(1, cov.getX1());
        assertEquals(0, cov.getY1());

        cov = tp.cover(Envelopes.scale(b, 0.1), 1);
        assertEquals(2, cov.getWidth());
        assertEquals(2, cov.getHeight());
        assertEquals(1, cov.getX0());
        assertEquals(0, cov.getY0());
        assertEquals(2, cov.getX1());
        assertEquals(1, cov.getY1());
    }

    void assertEnvelope(Envelope e, double x1, double y1, double x2, double y2) {
        assertEquals(x1, e.getMinX(), 0.1);
        assertEquals(x2, e.getMaxX(), 0.1);
        assertEquals(y1, e.getMinY(), 0.1);
        assertEquals(y2, e.getMaxY(), 0.1);
    }
}
