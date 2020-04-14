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
package io.jeo.data;

import java.io.IOException;
import java.util.List;

import io.jeo.Pending;

import io.jeo.geom.Bounds;
import io.jeo.tile.Tile;
import io.jeo.tile.TileDataset;
import io.jeo.tile.TileGrid;

@Pending
public class TileDataView implements Disposable {

    TileDataset tiles;

    public TileDataView(TileDataset tiles) {
        this.tiles = tiles;
    }

    public TileGrid grid(int z) throws IOException {
        return tiles.pyramid().grid(z);
    }

    public TileGrid gridr(int i) throws IOException {
        List<TileGrid> grids = tiles.pyramid().grids();
        return i < grids.size() ? grids.get(i) : null;
    }

    public Cursor<Tile> cursor(Bounds bbox, int width, int height) throws IOException {
        return tiles.pyramid().cover(bbox, width, height).cursor(tiles);
    }
    
    public Cursor<Tile> cursor(Bounds bbox, double resx, double resy) throws IOException {
        return tiles.pyramid().cover(bbox, resx, resy).cursor(tiles);
    }

    @Override
    public void close() {
        if (tiles != null) {
            tiles.close();
            tiles = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }
}
