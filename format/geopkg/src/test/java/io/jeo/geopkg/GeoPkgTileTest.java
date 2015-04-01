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
package io.jeo.geopkg;

import static io.jeo.Tests.unzip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import io.jeo.data.Cursor;
import io.jeo.tile.Tile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeoPkgTileTest extends GeoPkgTestSupport {
    GeoPkgWorkspace geopkg;

    @Before
    public void setUp() throws Exception {
        File dir = unzip(getClass().getResourceAsStream("ne.gpkg.zip"), newTmpDir());
        geopkg = GeoPackage.open(new File(dir, "ne.gpkg"));
    }

    @After
    public void tearDown() throws Exception {
        geopkg.close();
        FileUtils.deleteQuietly(geopkg.file().getParentFile());
    }

    @Test
    public void testTileEntry() throws Exception {
        List<TileEntry> entries = geopkg.tiles();
        assertEquals(1, entries.size());

        TileEntry entry = entries.get(0);
        assertEquals("tiles", entry.getTableName());
    }

    @Test
    public void testRead() throws Exception {
        TileEntry entry = geopkg.tile("tiles");
        assertNotNull(entry);

        Cursor<Tile> r = geopkg.read(entry);
        for (int i = 0; i < 10; i++) {
            assertTrue(r.hasNext());
            assertNotNull(r.next());
        }

        assertFalse(r.hasNext());
        assertNull(r.next());
    }

}
