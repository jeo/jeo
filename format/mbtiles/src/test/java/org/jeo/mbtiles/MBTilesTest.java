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
package org.jeo.mbtiles;

import java.io.File;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MBTilesTest {

    @Test
    public void open() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("org/jeo/mbtiles/test.mbtiles").toURI());
        MBTileSet mbtileset = MBTiles.open(file);
        assertNotNull(mbtileset);
        assertEquals("test", mbtileset.name());
        assertEquals("random", mbtileset.title());
        mbtileset.close();
    }

    @Test
    public void name() {
        MBTiles mbtiles = new MBTiles();
        assertEquals("MBTiles", mbtiles.name());
    }

    @Test
    public void aliases() {
        MBTiles mbtiles = new MBTiles();
        List<String> aliases = mbtiles.aliases();
        assertEquals(1, aliases.size());
        assertEquals("mbt", aliases.get(0));
    }

    @Test
    public void type() {
        MBTiles mbtiles = new MBTiles();
        assertEquals(MBTileSet.class, mbtiles.type());
    }

}
