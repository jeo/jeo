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
package org.jeo.data;

import static org.junit.Assert.*;

import org.jeo.TestData;
import org.jeo.tile.TilePyramid;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Abstract test case that exercises all aspects of the {@link TileDataset} interface.
 * <p>
 * This test uses the {@link TestData#ne1()} dataset as a basis for testing and test implementors
 * must override {@link #createTileData()} and return an instance backed by the natural earth data.
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class TileApiTestBase {

    TileDataset data;

    @Before
    public final void setUp() throws Exception {
        init();
        data = createTileData();
    }

    protected void init() throws Exception {
    }

    /**
     * Creates the test data.
     */
    protected abstract TileDataset createTileData() throws Exception;

    @Test
    public void testPyramid() throws Exception {
        TilePyramid tpyr = data.pyramid();
        assertEquals(2, tpyr.getGrids().size());
        assertNotNull(tpyr.grid(0));
        assertNotNull(tpyr.grid(1));
        assertNull(tpyr.grid(2));

        assertEquals(Integer.valueOf(2), tpyr.grid(0).getWidth());
        assertEquals(Integer.valueOf(1), tpyr.grid(0).getHeight());
    }

    @Test
    public void testBounds() throws Exception {
        Envelope bbox = data.bounds();
        assertNotNull(bbox);

        assertEquals(-180.0, bbox.getMinX(), 0.01);
        assertEquals(-90.0, bbox.getMinY(), 0.01);
        assertEquals(180.0, bbox.getMaxX(), 0.01);
        assertEquals(90.0, bbox.getMaxY(), 0.01);
    }

    @Test
    public void testRead() throws Exception {
        assertNotNull(data.read(0, 0, 0));
        assertNull(data.read(0, 0, 1));
    }
}
