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
package org.jeo.geopkg;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.tile.Tile;
import org.jeo.tile.TilePyramid;
import org.jeo.tile.TileDataset;

public class GeoPkgTileSet extends GeoPkgDataset<TileEntry> implements TileDataset {

    GeoPkgTileSet(TileEntry entry, GeoPkgWorkspace geopkg) {
        super(entry, geopkg);
    }

    @Override
    public TilePyramid pyramid() {
        return entry.getTilePyramid();
    }

    @Override
    public Tile read(long z, long x, long y) throws IOException {
        Cursor<Tile> c = geopkg.read(entry, (int)z, (int)z, (int)x, (int)x, (int)y, (int)y);
        try {
            if (c.hasNext()) {
                return c.next();
            }
            return null;
        }
        finally {
            c.close();
        }
    }

    @Override
    public Cursor<Tile> read(long z1, long z2, long x1, long x2, long y1, long y2)
            throws IOException {
        return geopkg.read(entry, (int)z1, (int)z2, (int)x1, (int)x2, (int)y1, (int)y2);
    }
}
