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
package io.jeo.tile;

import io.jeo.data.Cursor;

import java.io.IOException;

/**
 * A rectangular two dimensional coverage of tile objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class TileCover {

    TileGrid grid;
    int x0,x1,y0,y1;

    Tile[][] tiles;

    /**
     * Creates a new tile cover.
     * 
     * @param grid The tile grid specifying the zoom level at which the cover exists. 
     * @param x0 The horizontal index of the leftmost tile in the coverage.
     * @param y0 The vertical index of the bottommost tile in the coverage.
     * @param x1 The horizontal index of the rightmost tile in the coverage.
     * @param y1 The vertical index of the rightmost tile in the coverage.
     */
    public TileCover(TileGrid grid, int x0, int y0, int x1, int y1) {
        this.grid = grid;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;

        tiles = new Tile[width()][height()];
    }

    /**
     * The grid / zoom level at which the coverage occurs. 
     */
    public TileGrid grid() {
        return grid;
    }

    /**
     * Horizontal index of the leftmost tile in the coverage.
     */
    public int x0() {
        return x0;
    }

    /**
     * Horizontal index of the rightmost tile in the coverage.
     */
    public int x1() {
        return x1;
    }

    /**
     * Vertical index of the bottommost tile in the coverage.
     */
    public int y0() {
        return y0;
    }

    /**
     * Vertical index of the rightmost tile in the coverage.
     */
    public int y1() {
        return y1;
    }

    /**
     * The number of tiles horizontally in the coverage.
     */
    public int width() {
        return x1 - x0 + 1;
    }

    /**
     * The number of tiles vertically in the coverage.
     */
    public int height() {
        return y1 - y0 + 1;
    }

    /**
     * Obtains the cursor for this cover from a tile set.
     * 
     * @param tileset The tile source.
     */
    public Cursor<Tile> cursor(TileDataset tileset) throws IOException {
        return tileset.read(grid.z(), grid.z(), x0, x1, y0, y1);
    }

    /**
     * Pulls all the tiles for the cover from the specified tile source.
     * 
     * @param tileset The tile source.
     */
    public void fill(TileDataset tileset) throws IOException {
        try (Cursor<Tile> cursor = cursor(tileset)) {
            while (cursor.hasNext()) {
                Tile t = cursor.next();
                tiles[t.x()-x0][t.y()-y0] = t;
            }
        }
    }

    /**
     * Returns a tile at the specified offsets into the coverage.
     * <p>
     * The {@link #fill(TileDataset)} method must be called before this method is called. The <tt>x</tt>
     * and <tt>y</tt> values are specified relative to the lower left corner of the tile cover. 
     * Meaning x = 0, y = 0 is the most lower left tile in ths cover.
     * </p> 
     */
    public Tile tile(int x, int y) {
        Tile t = tiles[x][y];
        if (t == null) {
            // "blank" tile
            t = new Tile(grid.z(), x0 + x, y0 + y, null, null);
        }
        return t;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(grid.z()).append(": ")
            .append(x0).append(",").append(y0).append(" - ").append(x1).append(",").append(y1)
            .toString();
    }
}
