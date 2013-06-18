package org.jeo.geopkg;

import static org.jeo.Tests.unzip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jeo.data.Cursor;
import org.jeo.data.Tile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeoPkgTileTest extends GeoPkgTestSupport {
    GeoPkgWorkspace geopkg;

    @Before
    public void setUp() throws Exception {
        File dir = unzip(getClass().getResourceAsStream("ne.geopackage.zip"), newTmpDir());
        geopkg = GeoPackage.open(new File(dir, "ne.geopackage"));
    }

    @After
    public void tearDown() throws Exception {
        geopkg.close();
        FileUtils.deleteQuietly(geopkg.getFile().getParentFile());
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
