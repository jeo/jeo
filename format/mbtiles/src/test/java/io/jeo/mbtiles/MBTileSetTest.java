/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.mbtiles;

import com.vividsolutions.jts.geom.Envelope;
import io.jeo.data.Cursor;
import io.jeo.proj.Proj;
import io.jeo.tile.Tile;
import io.jeo.tile.TileGrid;
import io.jeo.tile.TilePyramid;
import io.jeo.util.Key;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MBTileSetTest {

    private MBTileSet tileset;

    @Before
    public void before() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("io/jeo/mbtiles/test.mbtiles").toURI());
        tileset = MBTiles.open(file.toPath());
    }

    @After
    public void after() {
        tileset.close();
    }

    @Test
    public void getTileFormat() throws Exception {
        assertEquals("image/png", tileset.getTileFormat());
    }

    @Test
    public void file() {
        assertEquals("test.mbtiles", tileset.file().getName());
    }

    @Test
    public void driver() {
        assertEquals(MBTiles.class, tileset.driver().getClass());
    }

    @Test
    public void driverOptions() {
        Map<Key<?>, Object> options = tileset.driverOptions();
        assertTrue(options.containsKey(MBTiles.FILE));
        assertEquals("test.mbtiles", ((File)options.get(MBTiles.FILE)).getName());
    }

    @Test
    public void name() {
        assertEquals("test", tileset.name());
    }

    @Test
    public void title() {
        assertEquals("random", tileset.title());
    }

    @Test
    public void description() {
        assertEquals("Random color tiles", tileset.description());
    }

    @Test
    public void crs() throws IOException {
        assertEquals(Proj.EPSG_900913, tileset.crs());
    }

    @Test
    public void bounds() throws IOException {
        assertEquals(new Envelope(-180,180, -85, 85), tileset.bounds());
    }

    @Test
    public void pyramid() throws IOException {
        TilePyramid pyramid = tileset.pyramid();
        assertEquals(Proj.EPSG_900913, pyramid.crs());
        assertEquals(new Envelope(-180, 180, -85, 85), pyramid.bounds());
        List<TileGrid> grids = pyramid.grids();
        assertEquals(2, grids.size());
        assertEquals(new Integer(0), grids.get(0).z());
        assertEquals(1.40625, grids.get(0).xres(), 0.001);
        assertEquals(0.6640625, grids.get(0).yres(), 0.001);
        assertEquals(new Integer(1), grids.get(0).width());
        assertEquals(new Integer(1), grids.get(0).height());
        assertEquals(new Integer(1), grids.get(1).z());
        assertEquals(0.703125, grids.get(1).xres(), 0.001);
        assertEquals(0.33203125, grids.get(1).yres(), 0.001);
        assertEquals(new Integer(2), grids.get(1).width());
        assertEquals(new Integer(2), grids.get(1).height());
        assertEquals(TilePyramid.Origin.BOTTOM_LEFT, pyramid.origin());
        assertEquals(new Integer(256), pyramid.tileWidth());
        assertEquals(new Integer(256), pyramid.tileHeight());
    }

    @Test
    public void readTile() throws IOException {
        Tile tile = tileset.read(1, 1, 0);
        assertTrue(tile.data().length > 0);
        assertEquals(new Integer(1), tile.z());
        assertEquals(new Integer(1), tile.x());
        assertEquals(new Integer(0), tile.y());
        assertEquals("image/png", tile.mimeType());
    }

    @Test
    public void readTiles() throws IOException {
        Cursor<Tile> tiles = tileset.read(0,1,0,1,0,1);
        assertEquals(5, tiles.count());
        tiles.close();
    }
}
